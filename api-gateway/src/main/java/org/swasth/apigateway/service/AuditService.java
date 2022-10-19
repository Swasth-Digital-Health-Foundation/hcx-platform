package org.swasth.apigateway.service;

import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.ServerException;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.UUIDUtils;

import java.text.MessageFormat;
import java.util.*;

import static org.swasth.common.response.ResponseMessage.AUDIT_LOG_FETCH_MSG;
import static org.swasth.common.response.ResponseMessage.AUDIT_SERVICE_ERROR;
import static org.swasth.common.utils.Constants.*;


@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Value("${hcx-api.basePath}")
    private String hcxApiUrl;

    @Value("${version.internal}")
    private String internalVersion;

    @Autowired
    private AuditIndexer auditIndexer;

    public List<Map<String, Object>> getAuditLogs(Map<String, String> filters) throws Exception {
        String url = hcxApiUrl + "/" + internalVersion + Constants.AUDIT_SEARCH;
        HttpResponse response;
        try {
            response = HttpUtils.post(url, JSONUtils.serialize(Collections.singletonMap("filters", filters)));
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.SERVICE_UNAVAILABLE, MessageFormat.format(AUDIT_SERVICE_ERROR, e.getMessage()));
        }
        List<Map<String, Object>> details;
        if (response.getStatus() == HttpStatus.OK.value()) {
            details = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
            System.out.println("Audit filters: " + filters + " Audit data count: " + details.size() + " Audit data: " + details);
        } else {
            throw new Exception(MessageFormat.format(AUDIT_LOG_FETCH_MSG, response.getStatus()));
        }
        return details;
    }

    public void createAuditLog(BaseRequest request) throws Exception {
        auditIndexer.createDocument(createAuditEvent(request));
    }

    public void updateAuditLog(Map<String, Object> event) throws Exception {
        auditIndexer.createDocument(event);
    }

    public Map<String, Object> createAuditEvent(BaseRequest request) {
        Map<String, Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(HCX_RECIPIENT_CODE, request.getHcxRecipientCode());
        event.put(HCX_SENDER_CODE, request.getHcxSenderCode());
        event.put(API_CALL_ID, request.getApiCallId());
        event.put(CORRELATION_ID, request.getCorrelationId());
        event.put(WORKFLOW_ID, request.getWorkflowId());
        event.put(TIMESTAMP, request.getTimestamp());
        event.put(ERROR_DETAILS, request.getErrorDetails());
        event.put(DEBUG_DETAILS, request.getDebugDetails());
        event.put(MID, UUIDUtils.getUUID());
        event.put(ACTION, request.getApiAction());
        event.put(STATUS, ERROR_STATUS);
        event.put(REQUESTED_TIME, System.currentTimeMillis());
        event.put(UPDATED_TIME, System.currentTimeMillis());
        event.put(ETS, System.currentTimeMillis());
        event.put(SENDER_ROLE, request.getSenderRole());
        event.put(RECIPIENT_ROLE, request.getRecipientRole());
        event.put(PAYLOAD, request.getPayloadWithoutSensitiveData());
        return event;
    }

}
