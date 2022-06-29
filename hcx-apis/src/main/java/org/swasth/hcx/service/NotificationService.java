package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.*;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.postgresql.IDatabaseService;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

@Service
public class NotificationService extends BaseController {

    @Value("${postgres.subscription.tablename}")
    private String postgresSubscription;

    @Value("${postgres.subscription.insertQuery}")
    private String insertSubscription;

    @Value("${postgres.subscription.subscriptionQuery}")
    private String selectSubscription;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${notification.path:classpath:Notifications.json}")
    private String filename;

    @Value("${registry.hcxcode}")
    private String hcxRegistryCode;

    @Autowired
    private IDatabaseService postgreSQLClient;

    public List<Notification> notificationList = null;

    public ResponseEntity<Object> processSubscription(Map<String, Object> requestBody, int statusCode) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            //TODO load from MasterDataUtils
            if(notificationList == null) loadNotifications();
            if (!isValidNotificationId(request.getNotificationId())) {
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_ID, "Invalid NotificationId." + request.getNotificationId() + " is not present in the master list of notifications");
            }
            setResponseParams(request, response);
            //If recipient is HCX, then status is 200 and insert into subscription table
            if(hcxRegistryCode.equalsIgnoreCase(request.getRecipientCode())){
                String serviceMode = env.getProperty(SERVICE_MODE);
                String id = UUID.randomUUID().toString();
                //TODO modify the hardcoded mode here from the query
                String query = String.format(insertSubscription, postgresSubscription, id, request.getSenderCode(), request.getNotificationId(), statusCode, System.currentTimeMillis(),"API",statusCode,System.currentTimeMillis());
                if (StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
                    //Insert record into subscription table in postgres database
                    postgreSQLClient.execute(query);
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{ // For other recipients, status is 202 and process the request asynchronously
                //TODO write logic to create kafka topic for pipeline jobs to process in next sprint
                return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
            }
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    public ResponseEntity<Object> getSubscriptions(Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        Request request = null;
        List<Subscription> subscriptionList = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            subscriptionList = fetchSubscriptions(request.getSenderCode());
            response.setSubscriptions(subscriptionList);
            response.setCount(subscriptionList.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    public List<Subscription> fetchSubscriptions(String participantCode) throws Exception {
        ResultSet resultSet = null;
        List<Subscription> subscriptionList = null;
        Subscription subscription = null;
        try {
            String query = String.format(selectSubscription, postgresSubscription, participantCode);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(query);
            subscriptionList = new ArrayList<>();
            while (resultSet.next()) {
                subscription = new Subscription();
                String notificationId = resultSet.getString("notificationId");
                String recipientId = resultSet.getString("recipientId");
                String mode = resultSet.getString("mode");
                int statusCode = resultSet.getInt("status");
                String status = statusCode == 1 ? ACTIVE : IN_ACTIVE;
                subscription.setNotificationId(notificationId);
                subscription.setSubscriptionId(notificationId + ":" + recipientId);
                subscription.setStatus(status);
                subscription.setMode(mode);
                subscriptionList.add(subscription);
            }
            return subscriptionList;
        }finally {
            if (resultSet != null) resultSet.close();
        }
    }

    public ResponseEntity<Object> getNotifications(Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        NotificationListRequest request = null;
        try {
            checkSystemHealth();
            if (!requestBody.containsKey(FILTERS)) {
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Notification filters property is missing");
            }
            request = new NotificationListRequest(requestBody);
            for (String key : request.getFilters().keySet()) {
                if (!ALLOWED_NOTIFICATION_FILTER_PROPS.contains(key)) {
                    throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid notifications filters, allowed properties are: " + ALLOWED_NOTIFICATION_FILTER_PROPS);
                }
            }
            if (notificationList == null)
                loadNotifications();
            // TODO: add filter limit condition
            List<Map<String, Object>> filteredNotificationList = (List) notificationList;
            Set<Map<String, Object>> removeNotificationList = new HashSet<>();
            NotificationListRequest finalRequest = request;
            filteredNotificationList.stream().forEach(notification -> finalRequest.getFilters().keySet().forEach(key -> {
                if (!notification.get(key).equals(finalRequest.getFilters().get(key)))
                    removeNotificationList.add(notification);
            }));
            filteredNotificationList.removeAll(removeNotificationList);
            response.setNotifications(filteredNotificationList);
            response.setCount(filteredNotificationList.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    public void loadNotifications() throws IOException {
        Resource resource = resourceLoader.getResource(filename);
        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        notificationList =  (List<Notification>) jsonReader.readValue(resource.getInputStream(), List.class);
    }

    private boolean isValidNotificationId(String notificationId){
        boolean isValid = false;
        List<LinkedHashMap<String, Object>> linkedHashMapList = (List) notificationList;
        for (LinkedHashMap<String, Object> notification: linkedHashMapList) {
            if(notificationId.equals(notification.get(TOPIC_CODE))){
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    public ResponseEntity<Object> notify(Map<String, Object> requestBody, String kafkaTopic) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            request.setApiAction(Constants.NOTIFICATION_REQUEST);
            //TODO load from MasterDataUtils
            if(notificationList == null)
                loadNotifications();
            if (!isValidNotificationId(request.getNotificationId())) {
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_ID, "Invalid NotificationId." + request.getNotificationId() + " is not present in the master list of notifications");
            }
            setResponseParams(request, response);
            processAndSendEvent(kafkaTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }
}
