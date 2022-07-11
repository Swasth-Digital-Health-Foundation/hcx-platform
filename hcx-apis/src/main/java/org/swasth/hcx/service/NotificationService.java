package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.util.*;

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

    @Value("${registry.hcxcode}")
    private String hcxRegistryCode;

    @Value("${kafka.topic.subscription}")
    private String subscriptionTopic;

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

    public void processSubscription(Request request, int statusCode, Response response) throws Exception {
        List<String> senderList = request.getSenderList();
        Map<String, String> subscriptionMap = insertRecords(request.getTopicCode(), statusCode, senderList, request.getNotificationRecipientCode());
        //Iterate through the list and push the kafka event
        pushKafka(request, senderList, subscriptionMap);
        //Set the response data
        List<String> subscriptionList = new ArrayList<>(subscriptionMap.values());
        response.setSubscription_list(subscriptionList);
    }

    private void pushKafka(Request request, List<String> senderList, Map<String, String> subscriptionMap) {
        senderList.stream().forEach(senderCode -> {
            try {
                if (!senderCode.equalsIgnoreCase(hcxRegistryCode)) {
                    String subscriptionMessage = eventGenerator.generateSubscriptionEvent(request.getMid(), request.getApiAction(), request.getNotificationRecipientCode(), senderCode);
                    kafkaClient.send(subscriptionTopic, senderCode, subscriptionMessage);
                    auditIndexer.createDocument(eventGenerator.generateSubscriptionAuditEvent(request, subscriptionMap.get(senderCode), QUEUED_STATUS, senderCode));
                } else {
                    LOG.info("Subscribe/Unsubscribe request received with HCX as recipient with code:" + senderCode);
                    auditIndexer.createDocument(eventGenerator.generateSubscriptionAuditEvent(request, subscriptionMap.get(senderCode), DISPATCHED_STATUS, senderCode));
                }
            } catch (JsonProcessingException e) {
                LOG.error("JsonProcessingException while generating subscription event", e);
            } catch (Exception e) {
                LOG.error("Exception while generating subscription event", e);
            }
        });
    }

    private Map<String, String> insertRecords(String topicCode, int statusCode, List<String> senderList, String notificationRecipientCode) throws Exception {
        //subscription_id,topic_code,sender_code,recipient_code,subscription_status,lastUpdatedOn,createdOn,expiry,is_delegated
        Map<String, String> subscriptionMap = new HashMap<>();
        senderList.stream().forEach(senderCode -> {
            int status = senderCode.equalsIgnoreCase(hcxRegistryCode) ? statusCode : PENDING_CODE;
            UUID subscriptionId = UUID.randomUUID();
            subscriptionMap.put(senderCode, subscriptionId.toString());
            String query = String.format(insertSubscription, postgresSubscription, subscriptionId, topicCode, senderCode,
                    notificationRecipientCode, status, System.currentTimeMillis(), System.currentTimeMillis(), 0, false, status, System.currentTimeMillis(), null, false);
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

    public void getSubscriptions(NotificationListRequest request, Response response) throws Exception {
        List<Subscription> subscriptionList = fetchSubscriptions(request.getRecipientCode());
        response.setSubscriptions(subscriptionList);
        response.setCount(subscriptionList.size());
    }

    /**
     * validates and process the notify request
     */
    public void notify(Request request, Response response, String kafkaTopic) throws Exception {
        if (!request.getSubscriptions().isEmpty())
            isValidSubscriptions(request);
        request.setNotificationId(UUID.randomUUID().toString());
        response.setNotificationId(request.getNotificationId());
        eventHandler.processAndSendEvent(kafkaTopic, request);
    }

    public void getNotifications(NotificationListRequest request, Response response) throws Exception {
        for (String key : request.getFilters().keySet()) {
            if (!ALLOWED_NOTIFICATION_FILTER_PROPS.contains(key))
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid notifications filters, allowed properties are: " + ALLOWED_NOTIFICATION_FILTER_PROPS);
        }
        // TODO: add filter limit condition
        List<Map<String, Object>> list = NotificationUtils.notificationList;
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
     * checks the given list of subscriptions are valid and active
     */
    private void isValidSubscriptions(Request request) throws Exception {
        List<String> subscriptions = request.getSubscriptions();
        ResultSet resultSet = null;
        try {
            String query = String.format("SELECT subscription_id FROM %s WHERE topic_code = '%s' AND sender_code = '%s' subscription_status = 1 AND subscription_id IN %s",
                    postgresSubscription, request.getTopicCode(), request.getSenderCode(), subscriptions.toString().replace("[", "(").replace("]", ")"));
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            while (resultSet.next()) {
                subscriptions.remove(resultSet.getString("subscription_id"));
            }
            if (!subscriptions.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid subscriptions list: " + subscriptions);
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private List<Subscription> fetchSubscriptions(String recipientCode) throws Exception {
        ResultSet resultSet = null;
        List<Subscription> subscriptionList = null;
        Subscription subscription = null;
        try { //subscription_id,subscription_status,topic_code,sender_code,recipient_code,expiry,is_delegated
            String query = String.format(selectSubscription, postgresSubscription, recipientCode);
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
}
