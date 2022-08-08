package org.swasth.hcx.service;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.*;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

import static org.swasth.common.utils.Constants.*;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    @Value("${postgres.subscription.tablename}")
    private String postgresSubscription;

    @Value("${postgres.subscription.insertQuery}")
    private String insertSubscription;

    @Value("${postgres.subscription.subscriptionQuery}")
    private String selectSubscription;

    @Value("${postgres.subscription.subscriptionSelectQuery}")
    private String subscriptionSelectQuery;

    @Value("${postgres.subscription.updateSubscriptionQuery}")
    private String updateSubscriptionQuery;

    @Value("${registry.hcxcode}")
    private String hcxRegistryCode;

    @Value("${kafka.topic.subscription}")
    private String subscriptionTopic;

    @Value("${notification.subscription.expiry}")
    private int subscriptionExpiry;

    @Value("${notification.subscription.allowedFilters}")
    private List<String> allowedSubscriptionFilters;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    protected EventHandler eventHandler;

    @Autowired
    protected EventGenerator eventGenerator;

    @Autowired
    private IEventService kafkaClient;

    @Autowired
    protected AuditIndexer auditIndexer;

    public void getNotifications(NotificationListRequest request, Response response) throws Exception {
        for (String key : request.getFilters().keySet()) {
            if (!ALLOWED_NOTIFICATION_FILTER_PROPS.contains(key))
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid notifications filters, allowed properties are: " + ALLOWED_NOTIFICATION_FILTER_PROPS);
        }
        // TODO: add filter limit condition
        List<Map<String, Object>> list = new ArrayList<>(NotificationUtils.notificationList);
        Set<Map<String, Object>> removeNotificationList = new HashSet<>();
        list.forEach(notification -> request.getFilters().keySet().forEach(key -> {
            if (!notification.get(key).equals(request.getFilters().get(key)))
                removeNotificationList.add(notification);
        }));
        list.removeAll(removeNotificationList);
        response.setNotifications(list);
        response.setCount(list.size());
    }

    /**
     * validates and process the notify request
     */
    public void notify(Request request, Response response, String kafkaTopic) throws Exception {
        if (!request.getSubscriptions().isEmpty())
            isValidSubscriptions(request);
        request.setNotificationRequestId(UUID.randomUUID().toString());
        response.setNotificationRequestId(request.getNotificationRequestId());
        eventHandler.processAndSendEvent(kafkaTopic, request);
    }

    /**
     * checks the given list of subscriptions are valid and active
     */
    private void isValidSubscriptions(Request request) throws Exception {
        List<String> subscriptions = request.getSubscriptions();
        ResultSet resultSet = null;
        try {
            String joined = subscriptions.stream()
                    .map(plain -> StringUtils.wrap(plain, "'"))
                    .collect(Collectors.joining(","));
            String query = String.format("SELECT subscription_id FROM %s WHERE topic_code = '%s' AND sender_code = '%s' AND subscription_status = 1 AND subscription_id IN (%s)",
                    postgresSubscription, request.getTopicCode(), request.getSenderCode(), joined);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            while (resultSet.next()) {
                subscriptions.remove(resultSet.getString("subscription_id"));
            }
            if (subscriptions.size() ==1 && !subscriptions.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid subscriptions list: " + subscriptions);
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    public void processOnSubscription(Request request, Response response) throws Exception {
        String subscriptionId = request.getSubscriptionId();
        int statusCode = request.getSubscriptionStatus();
        //fetch notification recipient based on subscription id
        Subscription subscription = getSubscriptionById(subscriptionId, request.getSenderCode());
        if (subscription == null) {
            throw new ClientException(ErrorCodes.ERR_INVALID_SUBSCRIPTION_ID, "Invalid subscription id: " + subscriptionId);
        }
        //Update the Database
        String subscriptionFromDB = updateSubscription(subscriptionId, statusCode);
        if (subscriptionFromDB.equals(subscriptionId)) {
            LOG.info("Subscription record updated for subscriptionId:" + subscriptionId.replaceAll("[\n\r\t]", "_"));
        } else {
            LOG.info("Subscription record is not updated for subscriptionId:" + subscriptionId.replaceAll("[\n\r\t]", "_"));
            throw new ClientException(ErrorCodes.ERR_INVALID_SUBSCRIPTION_ID, "Unable to update record with subscription id: " + subscriptionId);
        }
        //Send message to kafka topic
        String subscriptionMessage = eventGenerator.generateOnSubscriptionEvent(request.getApiAction(), subscription.getRecipient_code(), request.getSenderCode(), subscriptionId, statusCode);
        kafkaClient.send(subscriptionTopic, request.getSenderCode(), subscriptionMessage);

        //Create audit event
        auditIndexer.createDocument(eventGenerator.generateOnSubscriptionAuditEvent(request.getApiAction(),subscription.getRecipient_code(),subscriptionId,QUEUED_STATUS,request.getSenderCode(),statusCode));
        //Set the response data
        response.setSubscriptionId(subscriptionId);
    }

    public void processSubscription(Request request, int statusCode, Response response) throws Exception {
        List<String> senderList = request.getSenderList();
        Map<String, String> subscriptionMap = insertRecords(request.getTopicCode(), statusCode, senderList, request.getRecipientCode());
        //Push the event to kafka
        String subscriptionMessage = eventGenerator.generateSubscriptionEvent(request.getApiAction(), request.getRecipientCode(), request.getTopicCode(), senderList);
        kafkaClient.send(subscriptionTopic, request.getRecipientCode(), subscriptionMessage);
        //Create audit event
        auditIndexer.createDocument(eventGenerator.generateSubscriptionAuditEvent(request,QUEUED_STATUS,senderList));
        //Set the response data
        List<String> subscriptionList = new ArrayList<>(subscriptionMap.values());
        response.setSubscription_list(subscriptionList);
    }

    private Map<String, String> insertRecords(String topicCode, int statusCode, List<String> senderList, String notificationRecipientCode) throws Exception {
        //subscription_id,topic_code,sender_code,recipient_code,subscription_status,lastUpdatedOn,createdOn,expiry,is_delegated
        Map<String, String> subscriptionMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, subscriptionExpiry);
        senderList.stream().forEach(senderCode -> {
            int status = senderCode.equalsIgnoreCase(hcxRegistryCode) ? statusCode : PENDING_CODE;
            UUID subscriptionId = UUID.randomUUID();
            subscriptionMap.put(senderCode, subscriptionId.toString());
            String query = String.format(insertSubscription, postgresSubscription, subscriptionId, topicCode, senderCode,
                    notificationRecipientCode, status, System.currentTimeMillis(), System.currentTimeMillis(), cal.getTimeInMillis(), false, status, System.currentTimeMillis(), cal.getTimeInMillis(), false);
            try {
                postgreSQLClient.addBatch(query);
            } catch (Exception e) {
                LOG.error("Exception while adding query to batch ", e);
            }
        });
        //Execute the batch
        int[] batchArr = postgreSQLClient.executeBatch();
        LOG.info("Number of records inserted into DB:" + batchArr.length);
        return subscriptionMap;
    }

    /**
     * To update the subscription status, expiry and enabling/disabling delegation.
     */
    public void subscriptionUpdate(Request request, Response response) throws Exception {
        if (StringUtils.isEmpty(request.getTopicCode()) || !NotificationUtils.isValidCode(request.getTopicCode()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Topic code is empty or invalid");
        else if (!request.getPayload().containsKey(SUBSCRIPTION_STATUS) && !request.getPayload().containsKey(IS_DELEGATED) && !request.getPayload().containsKey(EXPIRY))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Mandatory fields are missing: " + SUBSCRIPTION_UPDATE_PROPS);
        else if (request.getPayload().containsKey(EXPIRY) && new DateTime(request.getExpiry()).isBefore(DateTime.now()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Expiry cannot be past date");
        else if (request.getPayload().containsKey(SUBSCRIPTION_STATUS) && !ALLOWED_SUBSCRIPTION_STATUS.contains(request.getSubscriptionStatus()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Subscription status value is invalid");
        else if (request.getPayload().containsKey(IS_DELEGATED) && !(request.getPayload().get(IS_DELEGATED) instanceof Boolean))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Is delegated value is invalid");
        StringBuilder updateProps = new StringBuilder();
        SUBSCRIPTION_UPDATE_PROPS.forEach(prop -> {
            if(request.getPayload().containsKey(prop))
                updateProps.append("," + prop + "=" + "'" + request.getPayload().get(prop) + "'");
        });
        updateProps.deleteCharAt(0);
        ResultSet resultSet = null;
        try {
            String updateQuery = String.format("UPDATE %s SET %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s' RETURNING %s,%s",
                    postgresSubscription, updateProps, SENDER_CODE, request.getSenderCode(), RECIPIENT_CODE,
                    request.getRecipientCode(), TOPIC_CODE, request.getTopicCode(), SUBSCRIPTION_ID, SUBSCRIPTION_STATUS);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(updateQuery);
            if (resultSet.next()) {
                response.setSubscriptionId(resultSet.getString(SUBSCRIPTION_ID));
                response.setSubscriptionStatus(resultSet.getInt(SUBSCRIPTION_STATUS));
            } else {
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Subscription does not exist");
            }
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    public void getSubscriptions(NotificationListRequest request, Response response) throws Exception {
        List<Subscription> subscriptionList = fetchSubscriptions(request);
        response.setSubscriptions(subscriptionList);
        response.setCount(subscriptionList.size());
    }

    private List<Subscription> fetchSubscriptions(NotificationListRequest request) throws Exception {
        ResultSet resultSet = null;
        List<Subscription> subscriptionList = null;
        Subscription subscription = null;
        Map<String, Object> filterMap = request.getFilters();
        try { //subscription_id,subscription_status,topic_code,sender_code,recipient_code,expiry,is_delegated
            String query = String.format(selectSubscription, postgresSubscription, request.getRecipientCode());
            if (!filterMap.isEmpty()) {
                for (String key : filterMap.keySet()) {
                    if (!allowedSubscriptionFilters.contains(key))
                        throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid notifications filters, allowed properties are: " + allowedSubscriptionFilters);
                    query += " AND " + key + " = '" + filterMap.get(key) + "'";
                }
            }
            query += " ORDER BY lastUpdatedOn DESC";
            if (request.getLimit() > 0) {
                query += " LIMIT " + request.getLimit();
            }
            if (request.getOffset() > 0) {
                query += " OFFSET " + request.getOffset();
            }
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            subscriptionList = new ArrayList<>();
            while (resultSet.next()) {
                subscription = new Subscription(resultSet.getString(SUBSCRIPTION_ID), resultSet.getString(TOPIC_CODE), resultSet.getInt(SUBSCRIPTION_STATUS),
                        resultSet.getString(Constants.SENDER_CODE), resultSet.getString(RECIPIENT_CODE), resultSet.getLong(EXPIRY), resultSet.getBoolean(IS_DELEGATED));
                subscriptionList.add(subscription);
            }
            return subscriptionList;
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    public Subscription getSubscriptionById(String subscriptionId, String senderCode) throws Exception {
        ResultSet resultSet = null;
        Subscription subscription = null;
        try { //subscription_id,subscription_status,topic_code,sender_code,recipient_code,expiry,is_delegated
            String query = String.format(subscriptionSelectQuery, postgresSubscription, subscriptionId, senderCode);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            while (resultSet.next()) {
                subscription = new Subscription(resultSet.getString(SUBSCRIPTION_ID), resultSet.getString(TOPIC_CODE), resultSet.getInt(SUBSCRIPTION_STATUS),
                        resultSet.getString(Constants.SENDER_CODE), resultSet.getString(RECIPIENT_CODE), resultSet.getLong(EXPIRY), resultSet.getBoolean(IS_DELEGATED));
            }
            return subscription;
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private String updateSubscription(String subscriptionId, int subscriptionStatus) throws Exception {
        String query = String.format(updateSubscriptionQuery, postgresSubscription, subscriptionStatus, subscriptionId, SUBSCRIPTION_ID);
        ResultSet resultSet = null;
        try {
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            if (resultSet.next())
                return resultSet.getString(SUBSCRIPTION_ID);
        } finally {
            if (resultSet != null) resultSet.close();
        }
        return "";
    }
}
