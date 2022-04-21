package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    protected void processAndSendEvent(String metadataTopic, Request request) throws Exception {
        request.setMid(UUID.randomUUID().toString());
        String serviceMode = env.getProperty(SERVICE_MODE);
        String payloadTopic = env.getProperty(KAFKA_TOPIC_PAYLOAD);
        String key = request.getSenderCode();
        String payloadEvent = eventGenerator.generatePayloadEvent(request);
        String metadataEvent = eventGenerator.generateMetadataEvent(request);
        String query = String.format("INSERT INTO %s (mid,data,action,status,retrycount,lastupdatedon) VALUES ('%s','%s','%s','%s',%d,%d)", postgresTableName, request.getMid(), JSONUtils.serialize(request.getPayload()), request.getApiAction(), QUEUED_STATUS, 0, System.currentTimeMillis());
        System.out.println("Mode: " + serviceMode + " :: mid: " + request.getMid() + " :: Event: " + metadataEvent);
        if(StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
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

    protected void setResponseParams(Request request, Response response){
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

}
