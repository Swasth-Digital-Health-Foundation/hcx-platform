package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.NotificationListRequest;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.Subscription;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.postgresql.IDatabaseService;

import javax.annotation.PostConstruct;
import java.io.IOException;
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

    @Value("${notification.path:classpath:notifications.json}")
    private String filename;

    @Value("${registry.hcxcode}")
    private String hcxRegistryCode;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    protected EventHandler eventHandler;

    private List<Map<String, Object>> notificationList = null;

    private List<String> topicCodeList = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        loadNotifications();
    }

    public void processSubscription(Request request, int statusCode) throws Exception {
        isValidCode(request.getNotificationId());
        String query = String.format(insertSubscription, postgresSubscription, UUID.randomUUID(), request.getSenderCode(),
                request.getNotificationId(), statusCode, System.currentTimeMillis(), "API", statusCode,
                System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    public void getSubscriptions(Request request, Response response) throws Exception {
        List<Subscription> subscriptionList = fetchSubscriptions(request.getSenderCode());
        response.setSubscriptions(subscriptionList);
        response.setCount(subscriptionList.size());
    }

    public void notify(Request request, Response response, String kafkaTopic) throws Exception {
        isValidCode(request.getNotificationId());
        eventHandler.processAndSendEvent(kafkaTopic, request);
    }

    public void getNotifications(NotificationListRequest request, Response response) throws Exception {
        for (String key : request.getFilters().keySet()) {
            if(!ALLOWED_NOTIFICATION_FILTER_PROPS.contains(key))
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid notifications filters, allowed properties are: " + ALLOWED_NOTIFICATION_FILTER_PROPS);
        }
        // TODO: add filter limit condition
        List<Map<String, Object>> list = notificationList;
        Set<Map<String, Object>> removeNotificationList = new HashSet<>();
        list.forEach(notification -> request.getFilters().keySet().forEach(key -> {
            if (!notification.get(key).equals(request.getFilters().get(key)))
                removeNotificationList.add(notification);
        }));
        list.removeAll(removeNotificationList);
        response.setNotifications(list);
        response.setCount(list.size());
    }

    private void loadNotifications() throws IOException {
        Resource resource = resourceLoader.getResource(filename);
        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        notificationList = (List<Map<String, Object>>) jsonReader.readValue(resource.getInputStream(), List.class);
        notificationList.forEach(obj -> topicCodeList.add((String) obj.get(TOPIC_CODE)));
    }

    private void isValidCode(String code) throws ClientException {
        if (!topicCodeList.contains(code))
            throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Invalid topic code(" + code + ") is not present in the master list of notifications");
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
