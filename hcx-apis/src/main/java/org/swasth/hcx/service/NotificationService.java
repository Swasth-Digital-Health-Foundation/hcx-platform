package org.swasth.hcx.service;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.NotificationListRequest;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.Subscription;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.swasth.common.response.ResponseMessage.*;
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

    @Value("${kafka.topic.onsubscription}")
    private String onSubscriptionTopic;

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
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(INVALID_NOTIFICATION_FILTERS, ALLOWED_NOTIFICATION_FILTER_PROPS));
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
    public void notify(Request request, String kafkaTopic) throws Exception {
        if (request.getRecipientType().equalsIgnoreCase(SUBSCRIPTION))
            isValidSubscriptions(request);
        eventHandler.processAndSendEvent(kafkaTopic, request);
    }

    /**
     * checks the given list of subscriptions are valid and active
     */
    private void isValidSubscriptions(Request request) throws Exception {
        List<String> subscriptions = request.getRecipients();
        ResultSet resultSet = null;
        try {
            String joined = subscriptions.stream()
                    .map(plain -> StringUtils.wrap(plain, "'"))
                    .collect(Collectors.joining(","));
            String query = String.format("SELECT subscription_id FROM %s WHERE topic_code = '%s' AND sender_code = '%s' AND subscription_status = 'Active' AND subscription_id IN (%s)",
                    postgresSubscription, request.getTopicCode(), request.getSenderCode(), joined);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            while (resultSet.next()) {
                subscriptions.remove(resultSet.getString(SUBSCRIPTION_ID));
            }
            if (subscriptions.size() == 1 && !subscriptions.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(INVALID_SUBSCRIPTION_LIST, subscriptions));
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    public void processOnSubscription(Request request, Response response) throws Exception {
        String subscriptionId = request.getSubscriptionId();
        String statusCode = request.getSubscriptionStatus();
        //fetch notification recipient based on subscription id
        Subscription subscription = getSubscriptionById(subscriptionId, request.getSenderCode());
        if (subscription == null) {
            throw new ClientException(ErrorCodes.ERR_INVALID_SUBSCRIPTION_ID, MessageFormat.format(
                    INVALID_SUBSCRIPTION_ID, subscriptionId));
        }
        //Update the Database
        String subscriptionFromDB = updateSubscriptionById(subscriptionId, statusCode);
        if (subscriptionFromDB.equals(subscriptionId)) {
            LOG.info("Subscription record updated for subscriptionId:" + subscriptionId.replaceAll("[\n\r\t]", "_"));
        } else {
            LOG.info("Subscription record is not updated for subscriptionId:" + subscriptionId.replaceAll("[\n\r\t]", "_"));
            throw new ClientException(ErrorCodes.ERR_INVALID_SUBSCRIPTION_ID, MessageFormat.format(
                    UPDATE_MESSAGE_SUBSCRIPTION_ID, subscriptionId));
        }
        //Send message to kafka topic
        String subscriptionMessage = eventGenerator.generateOnSubscriptionEvent(request.getApiAction(), subscription.getRecipient_code(), request.getSenderCode(), subscriptionId, statusCode);
        kafkaClient.send(onSubscriptionTopic, request.getSenderCode(), subscriptionMessage);

        //Create audit event
        auditIndexer.createDocument(eventGenerator.generateOnSubscriptionAuditEvent(request, subscription.getRecipient_code(), subscriptionId, QUEUED_STATUS, statusCode));
        //Set the response data
        response.setSubscriptionId(subscriptionId);
    }

    public void processSubscription(Request request, String statusCode, Response response) throws Exception {
        //Key as senderCode and value as subscriptionId generated for each sender code
        Map<String, String> subscriptionMap = insertRecords(request.getTopicCode(), statusCode, request.getSenderList(), request.getRecipientCode());
        //Push the event to kafka
        String subscriptionMessage = eventGenerator.generateSubscriptionEvent(request, subscriptionMap);
        kafkaClient.send(subscriptionTopic, request.getRecipientCode(), subscriptionMessage);
        //Create audit event
        auditIndexer.createDocument(eventGenerator.generateSubscriptionAuditEvent(request, QUEUED_STATUS, request.getSenderList()));
        //Set the response data
        List<String> subscriptionList = new ArrayList<>(subscriptionMap.values());
        response.setSubscription_list(subscriptionList);
    }

    private Map<String, String> insertRecords(String topicCode, String statusCode, List<String> senderList, String notificationRecipientCode) throws Exception {
        //subscription_id,topic_code,sender_code,recipient_code,subscription_status,lastUpdatedOn,createdOn,expiry,is_delegated
        Map<String, String> subscriptionMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, subscriptionExpiry);
        UUID subRequestId = UUID.randomUUID();
        String status = INACTIVE;
        for (String senderCode : senderList) {
            if (statusCode.equalsIgnoreCase(ACTIVE))
                status = senderCode.equalsIgnoreCase(hcxRegistryCode) ? statusCode : PENDING;
            UUID subscriptionId = UUID.randomUUID();
            String query = String.format(insertSubscription, postgresSubscription, subscriptionId, subRequestId, topicCode, senderCode,
                    notificationRecipientCode, status, System.currentTimeMillis(), System.currentTimeMillis(), cal.getTimeInMillis(), false, status, System.currentTimeMillis(), cal.getTimeInMillis(), false);
                ResultSet updateResult = (ResultSet) postgreSQLClient.executeQuery(query);
                if (updateResult.next()) {
                    subscriptionMap.put(senderCode, updateResult.getString(SUBSCRIPTION_ID));
                }
        }
        LOG.info("Records inserted/Updated into DB");
        return subscriptionMap;
    }

    /**
     * To update the subscription status, expiry and enabling/disabling delegation.
     */
    public void subscriptionUpdate(Request request, Response response) throws Exception {
        if (StringUtils.isEmpty(request.getTopicCode()) || !NotificationUtils.isValidCode(request.getTopicCode()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, INVALID_TOPIC_CODE);
        else if (!request.getPayload().containsKey(SUBSCRIPTION_STATUS) && !request.getPayload().containsKey(IS_DELEGATED) && !request.getPayload().containsKey(EXPIRY))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, NOTHING_TO_UPDATE);
        else if (request.getPayload().containsKey(EXPIRY) && new DateTime(request.getExpiry()).isBefore(DateTime.now()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, EXPIRY_CANNOT_BE_PAST_DATE);
        else if (request.getPayload().containsKey(SUBSCRIPTION_STATUS) && !ALLOWED_SUBSCRIPTION_STATUS.contains(request.getSubscriptionStatus()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, SUBSCRIPTION_STATUS_VALUE_IS_INVALID);
        else if (request.getPayload().containsKey(IS_DELEGATED) && !(request.getPayload().get(IS_DELEGATED) instanceof Boolean))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, IS_DELEGATED_VALUE_IS_INVALID);
        StringBuilder updateProps = new StringBuilder();
        String selectQuery = String.format("SELECT %s FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s'",
                SUBSCRIPTION_STATUS, postgresSubscription, SENDER_CODE, request.getSenderCode(), RECIPIENT_CODE,
                request.getRecipientCode(), TOPIC_CODE, request.getTopicCode());
        ResultSet selectResult = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        if (selectResult.next()) {
            String prevStatus = selectResult.getString(SUBSCRIPTION_STATUS);
            formatDBCondition(request, updateProps, ",", "=");
            updateProps.deleteCharAt(0);
            String updateQuery = String.format("UPDATE %s SET %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s' RETURNING %s,%s",
                    postgresSubscription, updateProps, SENDER_CODE, request.getSenderCode(), RECIPIENT_CODE,
                    request.getRecipientCode(), TOPIC_CODE, request.getTopicCode(), SUBSCRIPTION_ID, SUBSCRIPTION_STATUS);
            ResultSet updateResult = (ResultSet) postgreSQLClient.executeQuery(updateQuery);
            if (updateResult.next()) {
                response.setSubscriptionId(updateResult.getString(SUBSCRIPTION_ID));
                response.setSubscriptionStatus(updateResult.getString(SUBSCRIPTION_STATUS));
            }
            Map<String, Object> updatedProps = new HashMap<>();
            SUBSCRIPTION_UPDATE_PROPS.forEach(prop -> {
                if (request.getPayload().containsKey(prop)) updatedProps.put(prop, request.getPayload().get(prop));
            });
            eventHandler.createAudit(eventGenerator.createAuditLog(response.getSubscriptionId(), NOTIFICATION, getCData(request),
                    getEData(response.getSubscriptionStatus(), prevStatus, updatedProps)));
            auditIndexer.createDocument(eventGenerator.generateSubscriptionUpdateAuditEvent(request, response));
        } else {
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, SUBSCRIPTION_DOES_NOT_EXIST);
        }
    }

    private void formatDBCondition(Request request, StringBuilder updateProps, String fieldSeparator, String operation) {
        updateProps.delete(0, updateProps.length());
        SUBSCRIPTION_UPDATE_PROPS.forEach(prop -> {
            if (request.getPayload().containsKey(prop))
                updateProps.append(fieldSeparator + prop + operation + "'" + request.getPayload().get(prop) + "'");
        });
    }

    private Map<String, Object> getCData(Request request) {
        Map<String, Object> data = new HashMap<>();
        data.put(ACTION, request.getApiAction());
        data.put(SENDER_CODE, request.getSenderCode());
        data.put(RECIPIENT_CODE, request.getRecipientCode());
        return data;
    }

    private Map<String, Object> getEData(String status, String prevStatus, Map<String, Object> props) {
        Map<String, Object> data = new HashMap<>();
        data.put(AUDIT_STATUS, status);
        data.put(PROPS, props);
        if (!status.equalsIgnoreCase(prevStatus))
            data.put(PREV_STATUS, prevStatus);
        return data;
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
                        throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(INVALID_SUBSCRIPTION_FILTERS, allowedSubscriptionFilters));
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
                subscription = new Subscription(resultSet.getString(SUBSCRIPTION_ID), resultSet.getString(SUBSCRIPTION_REQUEST_ID), resultSet.getString(TOPIC_CODE), resultSet.getString(SUBSCRIPTION_STATUS),
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
                subscription = new Subscription(resultSet.getString(SUBSCRIPTION_ID), resultSet.getString(SUBSCRIPTION_REQUEST_ID), resultSet.getString(TOPIC_CODE), resultSet.getString(SUBSCRIPTION_STATUS),
                        resultSet.getString(Constants.SENDER_CODE), resultSet.getString(RECIPIENT_CODE), resultSet.getLong(EXPIRY), resultSet.getBoolean(IS_DELEGATED));
            }
            return subscription;
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private String updateSubscriptionById(String subscriptionId, String subscriptionStatus) throws Exception {
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
