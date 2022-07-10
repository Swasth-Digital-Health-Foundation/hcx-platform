package org.swasth.common.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.swasth.common.dto.Request;
import org.swasth.common.utils.JSONUtils;

import java.util.*;

import static org.swasth.common.utils.Constants.*;


public class EventGenerator {

    private List<String> protocolHeaders;
    private List<String> joseHeaders;
    private List<String> redirectHeaders;
    private List<String> errorHeaders;
    private List<String> notificationHeaders;

    public EventGenerator(List<String> protocolHeaders, List<String> joseHeaders, List<String> redirectHeaders, List<String> errorHeaders,List<String> notificationHeaders) {
        this.protocolHeaders = protocolHeaders;
        this.joseHeaders = joseHeaders;
        this.redirectHeaders = redirectHeaders;
        this.errorHeaders = errorHeaders;
        this.notificationHeaders = notificationHeaders;
    }

    public String generatePayloadEvent(Request request) throws JsonProcessingException {
        Map<String,Object> event = new HashMap<>();
        event.put(MID, request.getMid());
        event.put(PAYLOAD, request.getPayload());
        return JSONUtils.serialize(event);
    }

    public String generateMetadataEvent(Request request) throws Exception {
        Map<String,Object> event = new HashMap<>();
        if (request.getPayload().containsKey(PAYLOAD)) {
            Map<String,Object> protectedHeaders = request.getHcxHeaders();
            Map<String,Object> filterJoseHeaders = new HashMap<>();
            Map<String,Object> filterProtocolHeaders = new HashMap<>();
            joseHeaders.forEach(key -> {
                if (protectedHeaders.containsKey(key))
                    filterJoseHeaders.put(key, protectedHeaders.get(key));
            });
            protocolHeaders.forEach(key -> {
                if (protectedHeaders.containsKey(key))
                    filterProtocolHeaders.put(key, protectedHeaders.get(key));
            });
            //Add request.queued status and rewrite the status for action APIs and for on_* API calls use the status sent by the participant
            if(!request.getApiAction().contains("on_"))
                filterProtocolHeaders.put(STATUS, QUEUED_STATUS);
            event.put(MID, request.getMid());
            event.put(ETS, System.currentTimeMillis());
            event.put(ACTION, request.getApiAction());
            Map<String,Object> headers = new HashMap<>();
            headers.put(JOSE, filterJoseHeaders);
            headers.put(PROTOCOL, filterProtocolHeaders);
            event.put(HEADERS, headers);
        } else {
            List<String> headers;
            if (REDIRECT_STATUS.equalsIgnoreCase(request.getStatus())) {
                headers = redirectHeaders;
            } else if (ERROR_STATUS.equalsIgnoreCase(request.getStatus())) {
                headers = errorHeaders;
            } else if (NOTIFICATION_NOTIFY.equalsIgnoreCase(request.getApiAction())) {
                headers = notificationHeaders;
                event.put(TRIGGER_TYPE, TRIGGER_VALUE);
            } else {
                headers = null;
            }

            Map<String, Object> protectedHeaders = request.getHcxHeaders();
            Map<String, Object> filterHeaders = new HashMap<>();
            if(headers != null) {
                headers.forEach(key -> {
                    if (protectedHeaders.containsKey(key))
                        filterHeaders.put(key, protectedHeaders.get(key));
                });
                Map<String, Object> headerMap = new HashMap<>();
                if(!filterHeaders.containsKey(STATUS)){
                    filterHeaders.put(STATUS, QUEUED_STATUS);
                }
                headerMap.put(PROTOCOL, filterHeaders);
                event.put(HEADERS, headerMap);
            }
            event.put(MID, request.getMid());
            event.put(ETS, System.currentTimeMillis());
            event.put(ACTION, request.getApiAction());
        }
        return JSONUtils.serialize(event);
    }

    public Map<String,Object> generateAuditEvent(Request request) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(RECIPIENT_CODE, request.getRecipientCode());
        event.put(HCX_SENDER_CODE, request.getHcxSenderCode());
        event.put(API_CALL_ID, request.getApiCallId());
        event.put(CORRELATION_ID, request.getCorrelationId());
        event.put(WORKFLOW_ID, request.getWorkflowId());
        event.put(TIMESTAMP, request.getTimestamp());
        event.put(ERROR_DETAILS, request.getErrorDetails());
        event.put(DEBUG_DETAILS, request.getDebugDetails());
        event.put(MID, request.getMid());
        event.put(ACTION, request.getApiAction());
        if(request.getStatus() == null)
            event.put(STATUS, QUEUED_STATUS);
        else
            event.put(STATUS, request.getStatus());
        event.put(REQUEST_TIME, System.currentTimeMillis());
        event.put(UPDATED_TIME, System.currentTimeMillis());
        event.put(ETS, System.currentTimeMillis());
        event.put(SENDER_ROLE, new ArrayList<>());
        event.put(RECIPIENT_ROLE, new ArrayList<>());
        event.put(PAYLOAD, request.getPayloadWithoutSensitiveData());
        return  event;
    }

}
