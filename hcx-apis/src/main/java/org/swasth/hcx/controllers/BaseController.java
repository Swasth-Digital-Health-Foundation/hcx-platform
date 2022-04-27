package org.swasth.hcx.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.dto.Subscription;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        }
        request.setStatus(ERROR_STATUS);
        auditIndexer.createDocument(eventGenerator.generateAuditEvent(request));
        return new ResponseEntity<>(errorResponse(response, errorCode, e), status);
    }

    protected void setNotificationResponse(Request request, Response response, int statusCode) {
        String status = statusCode == 1 ? ACTIVE : IN_ACTIVE;
        response.setStatus(status);
        response.setSubscriptionId(request.getNotificationId() + ":" + request.getParticipantCode());
    }

    public ResponseEntity<Object> processNotification(Map<String, Object> requestBody, int statusCode) throws Exception {
        Response response = new Response();
        Request request = null;
        try {
            checkSystemHealth();
            request = new Request(requestBody);
            setNotificationResponse(request, response, statusCode);
            String serviceMode = env.getProperty(SERVICE_MODE);
            String id = UUID.randomUUID().toString();
            String query = String.format(insertSubscription, postgresSubscription, id, request.getParticipantCode(), request.getNotificationId(), statusCode, System.currentTimeMillis());
            if (StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
                //Insert record into subscription table in postgres database
                postgreSQLClient.execute(query);
            }
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
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
            subscriptionList = fetchSubscriptions(request.getParticipantCode());
            response.setSubscriptions(subscriptionList);
            response.setSubscriptionCount(subscriptionList.size());
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
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
                String notificationId = resultSet.getString("notificationid");
                String recipientId = resultSet.getString("recipientid");
                int statusCode = resultSet.getInt("status");
                String status = statusCode == 1 ? ACTIVE : IN_ACTIVE;
                subscription.setNotificationId(notificationId);
                subscription.setSubscriptionId(notificationId + ":" + recipientId);
                subscription.setStatus(status);
                subscriptionList.add(subscription);
            }
            return subscriptionList;
        }finally {
            if (resultSet != null) resultSet.close();
        }
    }

}
