package org.swasth.common.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;

import java.util.*;

import static org.swasth.common.utils.Constants.*;


public class EventGenerator {
    private final JWTUtils jwtUtils = new JWTUtils();
    private List<String> protocolHeaders;
    private List<String> joseHeaders;
    private List<String> redirectHeaders;
    private List<String> errorHeaders;
    private List<String> notificationHeaders;
    private String tag;

    public EventGenerator(String tag) {
        this.tag = tag;
    }

    public EventGenerator(List<String> protocolHeaders, List<String> joseHeaders, List<String> redirectHeaders, List<String> errorHeaders, List<String> notificationHeaders, String tag) {
        this(protocolHeaders, joseHeaders, redirectHeaders, errorHeaders, notificationHeaders);
        this.tag = tag;
    }

    public EventGenerator(List<String> protocolHeaders, List<String> joseHeaders, List<String> redirectHeaders, List<String> errorHeaders, List<String> notificationHeaders) {
        this.protocolHeaders = protocolHeaders;
        this.joseHeaders = joseHeaders;
        this.redirectHeaders = redirectHeaders;
        this.errorHeaders = errorHeaders;
        this.notificationHeaders = notificationHeaders;
    }

    public String generatePayloadEvent(Request request) throws JsonProcessingException {
        Map<String, Object> event = new HashMap<>();
        event.put(MID, request.getMid());
        event.put(PAYLOAD, request.getPayload());
        return JSONUtils.serialize(event);
    }

    public Map<String, Object> createAuditLog(String id, String objectType, Map<String, Object> cdata, Map<String, Object> edata) {
        Map<String, Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(ETS, System.currentTimeMillis());
        event.put(MID, UUID.randomUUID().toString());
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(ID, id);
        objectMap.put(TYPE, objectType);
        event.put(OBJECT, objectMap);
        event.put(CDATA, cdata);
        event.put(EDATA, edata);
        return event;
    }

    public String generateMetadataEvent(Request request) throws Exception {
        Map<String, Object> event = new HashMap<>();
        if (request.getPayload().containsKey(PAYLOAD) && !request.getApiAction().equals(NOTIFICATION_NOTIFY)) {
            Map<String, Object> protectedHeaders = request.getHcxHeaders();
            Map<String, Object> filterJoseHeaders = new HashMap<>();
            Map<String, Object> filterProtocolHeaders = new HashMap<>();
            joseHeaders.forEach(key -> {
                if (protectedHeaders.containsKey(key))
                    filterJoseHeaders.put(key, protectedHeaders.get(key));
            });
            protocolHeaders.forEach(key -> {
                if (protectedHeaders.containsKey(key))
                    filterProtocolHeaders.put(key, protectedHeaders.get(key));
            });
            //Add request.queued status and rewrite the status for action APIs and for on_* API calls use the status sent by the participant
            if (!request.getApiAction().contains("on_"))
                filterProtocolHeaders.put(STATUS, QUEUED_STATUS);
            event.put(MID, request.getMid());
            event.put(ETS, System.currentTimeMillis());
            event.put(ACTION, request.getApiAction());
            Map<String, Object> headers = new HashMap<>();
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
                event.put(TOPIC_CODE, request.getTopicCode());
                event.put(NOTIFICATION_DATA, request.getNotificationData());
                event.put(MESSAGE, request.getNotificationMessage());
                event.put(PAYLOAD, request.getPayload());
            } else {
                headers = null;
            }
            Map<String, Object> protectedHeaders = request.getHcxHeaders();
            Map<String, Object> filterHeaders = new HashMap<>();
            if (headers != null) {
                headers.forEach(key -> {
                    if (protectedHeaders.containsKey(key))
                        filterHeaders.put(key, protectedHeaders.get(key));
                });
                Map<String, Object> headerMap = new HashMap<>();
                if (!filterHeaders.containsKey(STATUS)) {
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

    public Map<String, Object> generateAuditEvent(Request request) {
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
        event.put(MID, request.getMid());
        event.put(ACTION, request.getApiAction());
        if (StringUtils.isEmpty(request.getStatus()))
            event.put(STATUS, QUEUED_STATUS);
        else
            event.put(STATUS, request.getStatus());
        event.put(REQUEST_TIME, System.currentTimeMillis());
        event.put(UPDATED_TIME, System.currentTimeMillis());
        event.put(ETS, System.currentTimeMillis());
        event.put(PAYLOAD, request.getPayloadWithoutSensitiveData());
        event.put(SENDER_ROLE, request.getSenderRole());
        event.put(RECIPIENT_ROLE, request.getRecipientRole());
        event.put(SENDER_NAME, request.getSenderName());
        event.put(RECIPIENT_NAME, request.getRecipientName());
        event.put(SENDER_PRIMARY_EMAIL, request.getSenderPrimaryEmail());
        event.put(RECIPIENT_PRIMARY_EMAIL, request.getRecipientPrimaryEmail());
        getTag(request,event);
        return event;
    }

    public String generateOnSubscriptionEvent(String apiAction, String recipientCode, String senderCode, String subscriptionId, String status) throws JsonProcessingException {
        Map<String, Object> event = new HashMap<>();
        event.put(MID, UUID.randomUUID().toString());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, apiAction);
        event.put(AUDIT_STATUS, QUEUED_STATUS);
        event.put(PAYLOAD, createOnSubscriptionPayload(subscriptionId, status));
        event.put(HCX_RECIPIENT_CODE, recipientCode);
        event.put(HCX_SENDER_CODE, senderCode);
        return JSONUtils.serialize(event);
    }

    public Map<String, Object> generateOnSubscriptionAuditEvent(Request request, String recipientCode, String subscriptionId, String status, String subscriptionStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(MID, request.getMid());
        event.put(ACTION, request.getApiAction());
        event.put(SUBSCRIPTION_ID, subscriptionId);
        event.put(SUBSCRIPTION_STATUS, subscriptionStatus);
        event.put(HCX_SENDER_CODE, request.getSenderCode());
        event.put(HCX_RECIPIENT_CODE, recipientCode);
        event.put(ETS, System.currentTimeMillis());
        event.put(STATUS, status);
        return event;
    }

    private Map<String, Object> createOnSubscriptionPayload(String subscriptionId, String status) {
        Map<String, Object> event = new HashMap<>();
        event.put(SUBSCRIPTION_ID, subscriptionId);
        event.put(SUBSCRIPTION_STATUS, status);
        return event;
    }

    private Map<String, Object> createSubscriptionPayload(String topicCode, List<String> senderList, Map<String, String> subscriptionMap) {
        Map<String, Object> event = new HashMap<>();
        event.put(TOPIC_CODE, topicCode);
        event.put(SENDER_LIST, senderList);
        event.put(SUBSCRIPTION_MAP, subscriptionMap);
        return event;
    }

    public Map<String, Object> generateSubscriptionAuditEvent(Request request, String status, List<String> senderList) {
        Map<String, Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(MID, request.getMid());
        event.put(ACTION, request.getApiAction());
        event.put(TOPIC_CODE, request.getTopicCode() == null ? "" : request.getTopicCode());
        event.put(SENDER_LIST, senderList);
        event.put(HCX_RECIPIENT_CODE, request.getRecipientCode());
        event.put(ETS, System.currentTimeMillis());
        event.put(STATUS, status);
        return event;
    }

    public String generateSubscriptionEvent(Request request, Map<String, String> subscriptionMap) throws JsonProcessingException {
        Map<String, Object> event = new HashMap<>();
        event.put(MID, request.getMid());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, request.getApiAction());
        event.put(AUDIT_STATUS, QUEUED_STATUS);
        event.put(PAYLOAD, createSubscriptionPayload(request.getTopicCode(), request.getSenderList(), subscriptionMap));
        event.put(HCX_SENDER_CODE, request.getRecipientCode());
        return JSONUtils.serialize(event);
    }

    public Map<String, Object> generateSubscriptionUpdateAuditEvent(Request request, Response response) {
        Map<String, Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(MID, request.getMid());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, request.getApiAction());
        event.put(TOPIC_CODE, request.getTopicCode() == null ? "" : request.getTopicCode());
        event.put(HCX_RECIPIENT_CODE, request.getRecipientCode());
        event.put(HCX_SENDER_CODE, request.getSenderCode());
        event.put(SUBSCRIPTION_ID, response.getSubscriptionId());
        event.put(SUBSCRIPTION_STATUS, response.getSubscriptionStatus());
        if (request.getHcxHeaders().containsKey(EXPIRY))
            event.put(EXPIRY, request.getExpiry());
        if (request.getHcxHeaders().containsKey(IS_DELEGATED))
            event.put(IS_DELEGATED, request.getIsDelegated());
        event.put(STATUS, QUEUED_STATUS);
        return event;
    }

    public void getTag(Request request, Map<String,Object> event){
        Set<String> tagSet = new HashSet<>();
        tagSet.add(request.getSenderTag().toString());
        tagSet.add(request.getRecipientTag().toString());
        if (!StringUtils.isEmpty(tag)) {
            tagSet.add(tag);
        }
        String tags = tagSet.toString().replace("[","").replace("]","").replace(" ","");
        if(!tags.isEmpty()) {
            event.put(TAGS, tags);
        }
    }

    public String createNotifyEvent(String topicCode, String senderCode, String recipientType, List<String> recipients, long expiry, String message, String privateKey) throws Exception {
        Map<String, Object> notificationHeaders = new HashMap<>();
        notificationHeaders.put(SENDER_CODE, senderCode);
        notificationHeaders.put(NOTIFICATION_TIMESTAMP, System.currentTimeMillis());
        notificationHeaders.put(RECIPIENT_TYPE, recipientType);
        notificationHeaders.put(RECIPIENTS, recipients);
        notificationHeaders.put(NOTIFICATION_CORRELATION_ID, UUID.randomUUID().toString());
        notificationHeaders.put(EXPIRY, expiry);

        Map<String, Object> protocolHeaders = new HashMap<>();
        protocolHeaders.put(ALG, RS256);
        protocolHeaders.put(NOTIFICATION_HEADERS, notificationHeaders);

        Map<String, Object> payload = new HashMap<>();
        payload.put(TOPIC_CODE, topicCode);
        payload.put(MESSAGE, message);

        Map<String, Object> event = new HashMap<>();
        event.put(MID, UUID.randomUUID().toString());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, NOTIFICATION_NOTIFY);
        event.put(TOPIC_CODE, topicCode);
        event.put(MESSAGE, message);
        event.put(PAYLOAD, jwtUtils.generateJWS(protocolHeaders, payload, privateKey));
        event.put(HEADERS, Collections.singletonMap(PROTOCOL, protocolHeaders));

        return JSONUtils.serialize(event);
    }

    public Map<String,Object> createNotifyAuditEvent(Request request){
        Map<String,Object> audit = new HashMap<>();
        audit.put(EID, AUDIT);
        audit.put(MID, UUID.randomUUID().toString());
        audit.put(ACTION, NOTIFICATION_NOTIFY);
        audit.put(ETS, System.currentTimeMillis());
        audit.put(TOPIC_CODE, request.getTopicCode());
        audit.put(SENDER_CODE, request.getHcxSenderCode() );
        audit.put(NOTIFICATION_CORRELATION_ID, request.getCorrelationId());
        audit.put(NOTIFICATION_TIMESTAMP, System.currentTimeMillis());
        audit.put(STATUS, request.getStatus());
        audit.put(RECIPIENT_TYPE,request.getRecipientType());
        audit.put(RECIPIENTS,request.getRecipients());
        audit.put(MESSAGE,request.getNotificationMessage());
        audit.put(ERROR_DETAILS, request.getErrorDetails());
        audit.put(DEBUG_DETAILS, request.getDebugDetails());
        return audit;
    }
}