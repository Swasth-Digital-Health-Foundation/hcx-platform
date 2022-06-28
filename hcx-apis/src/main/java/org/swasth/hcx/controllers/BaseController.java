package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.*;
import org.swasth.common.exception.*;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

public class BaseController {

    @Autowired
    protected Environment env;

    @Autowired
    private IEventService kafkaClient;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    protected HeaderAuditService auditService;

    @Autowired
    private AuditIndexer auditIndexer;

    @Autowired
    private EventGenerator eventGenerator;

    @Value("${postgres.tablename}")
    private String postgresTableName;

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

    protected List<Notification> notificationList = null;

    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e) {
        ResponseError error = new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    protected void processAndSendEvent(String metadataTopic, Request request) throws Exception {
        String serviceMode = env.getProperty(SERVICE_MODE);
        String payloadTopic = env.getProperty(KAFKA_TOPIC_PAYLOAD);
        String key = request.getSenderCode();
        String payloadEvent = eventGenerator.generatePayloadEvent(request);
        String metadataEvent = eventGenerator.generateMetadataEvent(request);
        String query = String.format("INSERT INTO %s (mid,data,action,status,retrycount,lastupdatedon) VALUES ('%s','%s','%s','%s',%d,%d)", postgresTableName, request.getMid(), JSONUtils.serialize(request.getPayload()), request.getApiAction(), QUEUED_STATUS, 0, System.currentTimeMillis());
        System.out.println("Mode: " + serviceMode + " :: mid: " + request.getMid() + " :: Event: " + metadataEvent);
        if (StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
            postgreSQLClient.execute(query);
            kafkaClient.send(payloadTopic, key, payloadEvent);
            kafkaClient.send(metadataTopic, key, metadataEvent);
            auditIndexer.createDocument(eventGenerator.generateAuditEvent(request));
        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String kafkaTopic) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            request.setApiAction(apiAction);
            setResponseParams(request, response);
            processAndSendEvent(kafkaTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    protected void checkSystemHealth() throws ServiceUnavailbleException {
        if (!HealthCheckManager.allSystemHealthResult)
            throw new ServiceUnavailbleException(ErrorCodes.ERR_SERVICE_UNAVAILABLE, "Service is unavailable");
    }

    protected void setResponseParams(Request request, Response response) {
        response.setCorrelationId(request.getCorrelationId());
        response.setApiCallId(request.getApiCallId());
    }

    protected ResponseEntity<Object> exceptionHandler(Request request, Response response, Exception e) throws Exception {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((ClientException) e).getErrCode();
        } else if (e instanceof ServiceUnavailbleException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = ((ServiceUnavailbleException) e).getErrCode();
        } else if (e instanceof ServerException) {
            errorCode = ((ServerException) e).getErrCode();
        } else if (e instanceof AuthorizationException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (e instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        if (request != null) {
            request.setStatus(ERROR_STATUS);
            auditIndexer.createDocument(eventGenerator.generateAuditEvent(request));
        }
        return new ResponseEntity<>(errorResponse(response, errorCode, e), status);
    }

    protected ResponseEntity<Object> processSubscription(Map<String, Object> requestBody, int statusCode) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            //TODO load from MasterDataUtils
            if(notificationList == null) loadNotifications();
            if (!isValidNotificaitonId(request.getNotificationId())) {
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

    protected ResponseEntity<Object> getSubscriptions(Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        Request request = null;
        List<Subscription> subscriptionList = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            subscriptionList = fetchSubscriptions(request.getSenderCode());
            response.setSubscriptions(subscriptionList);
            response.setSubscriptionCount(subscriptionList.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
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

    protected ResponseEntity<Object> getNotifications(Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            if (notificationList == null)
                 loadNotifications();
            response.setNotifications(notificationList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    protected void loadNotifications() throws IOException {
        Resource resource = resourceLoader.getResource(filename);
        ObjectMapper jsonReader = new ObjectMapper(new JsonFactory());
        notificationList =  (List<Notification>) jsonReader.readValue(resource.getInputStream(), List.class);
    }

    protected boolean isValidNotificaitonId(String notificationId){
        boolean isValid = false;
        List<LinkedHashMap<String, Object>> linkedHashMapList = (List) notificationList;
        for (LinkedHashMap<String, Object> notification: linkedHashMapList) {
            if(notificationId.equals(notification.get("id"))){
                isValid = true;
                break;
            }
        }
        return isValid;
    }

}
