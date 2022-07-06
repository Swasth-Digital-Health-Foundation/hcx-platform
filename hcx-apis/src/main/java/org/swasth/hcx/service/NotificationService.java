package org.swasth.hcx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.NotificationListRequest;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.Subscription;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.NotificationUtils;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

@Service
public class NotificationService {

    @Value("${postgres.subscription.tablename}")
    private String postgresSubscription;

    @Value("${postgres.subscription.insertQuery}")
    private String insertSubscription;

    @Value("${postgres.subscription.subscriptionQuery}")
    private String selectSubscription;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    protected EventHandler eventHandler;

    public void processSubscription(Request request, int statusCode) throws Exception {
        if (!NotificationUtils.isValidCode(request.getTopicCode()))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Invalid topic code(" + request.getTopicCode() + ") is not present in the master list of notifications");
        String query = String.format(insertSubscription, postgresSubscription, UUID.randomUUID(), request.getHcxSenderCode(),
                request.getTopicCode(), statusCode, System.currentTimeMillis(), "API", statusCode,
                System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    public void getSubscriptions(Request request, Response response) throws Exception {
        List<Subscription> subscriptionList = fetchSubscriptions(request.getHcxSenderCode());
        response.setSubscriptions(subscriptionList);
        response.setCount(subscriptionList.size());
    }

    /**
     * validates and process the notify request
     */
    public void notify(Request request, Response response, String kafkaTopic) throws Exception {
        if(!request.getSubscriptions().isEmpty())
          isValidSubscriptions(request.getSubscriptions());
        request.setNotificationId(UUID.randomUUID().toString());
        response.setNotificationId(request.getNotificationId());
        eventHandler.processAndSendEvent(kafkaTopic, request);
    }

    public void getNotifications(NotificationListRequest request, Response response) throws Exception {
        for (String key : request.getFilters().keySet()) {
            if(!ALLOWED_NOTIFICATION_FILTER_PROPS.contains(key))
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
    private void isValidSubscriptions(List<String> subscriptions) throws Exception {
        ResultSet resultSet = null;
        try {
            String query = String.format("SELECT subscriptionId FROM %s WHERE status = 1 AND subscriptionID IN %s", postgresSubscription, subscriptions.toString().replace("[","(").replace("]",")"));
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            while(resultSet.next()){
              subscriptions.remove(resultSet.getString("subscriptionId"));
            }
            if(!subscriptions.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid subscriptions list: " + subscriptions);
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private List<Subscription> fetchSubscriptions(String participantCode) throws Exception {
        ResultSet resultSet = null;
        List<Subscription> subscriptionList = null;
        Subscription subscription = null;
        try {
            String query = String.format(selectSubscription, postgresSubscription, participantCode);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            subscriptionList = new ArrayList<>();
            while (resultSet.next()) {
                String notificationId = resultSet.getString("notificationId");
                subscription = new Subscription(notificationId + ":" + resultSet.getString("recipientId"),
                        notificationId, resultSet.getInt("status") == 1 ? ACTIVE : IN_ACTIVE,
                        resultSet.getString("mode"));
                subscriptionList.add(subscription);
            }
            return subscriptionList;
        }finally {
            if (resultSet != null) resultSet.close();
        }
    }
}
