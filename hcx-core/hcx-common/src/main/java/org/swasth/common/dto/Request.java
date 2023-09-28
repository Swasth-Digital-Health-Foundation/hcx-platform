package org.swasth.common.dto;

import org.apache.commons.lang3.StringUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.PayloadUtils;
import org.swasth.common.utils.UUIDUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

public class Request {

    private  Map<String, Object> payload;
    protected Map<String, Object> hcxHeaders = null;
    private String mid = UUIDUtils.getUUID();
    private String apiAction;
    private String payloadWithoutSensitiveData;
    private Token token;

    public Request(Map<String, Object> body, String apiAction) throws Exception {
        this.apiAction = apiAction;
        this.payload = body;
        try {
            initializeHeaders(body, apiAction);
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Error while parsing the payload");
        }
    }
    public Request(Map<String, Object> body, String apiAction, String token) throws Exception {
        this(body, apiAction);
        this.token = new Token(token);
    }

    private void initializeHeaders(Map<String, Object> body, String apiAction) throws Exception {
        if (NOTIFICATION_NOTIFY.equals(apiAction)) {
            hcxHeaders = getHeadersFromPayload();
            hcxHeaders.putAll(JSONUtils.decodeBase64String(getPayloadValues()[1], Map.class));
            hcxHeaders.putAll(getNotificationHeaders());
            if (StringUtils.isEmpty((String) hcxHeaders.getOrDefault(CORRELATION_ID, ""))) {
                hcxHeaders.put(CORRELATION_ID, UUIDUtils.getUUID());
            } else {
                hcxHeaders.put(CORRELATION_ID, hcxHeaders.get(CORRELATION_ID));
            }
        } else if (body.containsKey(PAYLOAD)) {
            hcxHeaders = getHeadersFromPayload();
        } else {
            hcxHeaders = body;
        }
        this.payloadWithoutSensitiveData = PayloadUtils.removeSensitiveData(body, apiAction);
    }
    private Map<String,Object> getHeadersFromPayload() throws Exception {
        return JSONUtils.decodeBase64String(getPayloadValues()[0], Map.class);
    }

    private String[] getPayloadValues() {
        return ((String) getPayload().get(PAYLOAD)).split("\\.");
    }

    // TODO remove this method. We should restrict accessing it to have a clean code.
    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getWorkflowId() {
        return getHeader(WORKFLOW_ID);
    }

    public String getApiCallId() {
        return getHeader(API_CALL_ID);
    }

    public String getCorrelationId() {
        return getHeader(CORRELATION_ID);
    }

    public String getHcxSenderCode() {
        return getHeader(HCX_SENDER_CODE);
    }

    public String getHcxRecipientCode() {
        return getHeader(HCX_RECIPIENT_CODE);
    }

    public String getTimestamp() {
        return getHeader(TIMESTAMP);
    }

    public String getDebugFlag() {
        return getHeader(DEBUG_FLAG);
    }

    public String getStatus() {
        return getHeader(STATUS);
    }

    public void  setStatus(String status) { setHeaderMap(STATUS, status);}

    public Map<String, Object> getHcxHeaders() {
        return hcxHeaders;
    }

    protected String getHeader(String key) {
        return (String) hcxHeaders.getOrDefault(key, "");
    }

    public void setHeaders(Map<String,Object> headers) {
        hcxHeaders.putAll(headers);
    }

    protected List<String> getHeaderList(String key) {
        return (List<String>) hcxHeaders.getOrDefault(key, new ArrayList<>());
    }

    protected Map<String, Object> getHeaderMap(String key) {
        return (Map<String, Object>) hcxHeaders.getOrDefault(key, new HashMap<>());
    }

    private void setHeaderMap(String key, Object value) {
            hcxHeaders.put(key, value);
    }

    public Map<String, Object> getErrorDetails() {
        return getHeaderMap(ERROR_DETAILS);
    }

    public Map<String, Object> getDebugDetails() {
        return getHeaderMap(DEBUG_DETAILS);
    }

    public String getMid() { return mid; }

    public void setApiAction(String apiAction) {
        this.apiAction = apiAction;
    }

    public String getApiAction() { return apiAction; }

    public String getPayloadWithoutSensitiveData() { return payloadWithoutSensitiveData; }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTopicCode() { return getHeader(Constants.TOPIC_CODE);}

    public String getSenderCode() { return getHeader(SENDER_CODE); }

    public String getRecipientType() { return getHeader(RECIPIENT_TYPE); }

    public List<String> getRecipients() { return getHeaderList(Constants.RECIPIENTS); }

    public Map<String,Object> getNotificationHeaders() { return getHeaderMap(NOTIFICATION_HEADERS); }

    public Map<String,Object> getNotificationData() { return getHeaderMap(Constants.NOTIFICATION_DATA); }

    public List<String> getSenderList() { return getHeaderList(SENDER_LIST); }

    public String getRecipientCode() { return getHeader(RECIPIENT_CODE); }

    public String getSubscriptionStatus() { return (String) payload.getOrDefault(SUBSCRIPTION_STATUS, null);}
    
    public String getNotificationMessage() { return getHeader(MESSAGE); }

    public boolean getIsDelegated(){ return (boolean) payload.getOrDefault(IS_DELEGATED, null);}

    public Long getExpiry(){ return (Long) payload.getOrDefault(EXPIRY, null); }

    public String getSubscriptionId() { return (String) payload.get(SUBSCRIPTION_ID); }
    public String getSenderName() {
        return (String) senderDetails().getOrDefault(PARTICIPANT_NAME,"");
    }

    public String getRecipientName() {
        return (String) recipientDetails().getOrDefault(PARTICIPANT_NAME,"");
    }

    public String getSenderPrimaryEmail() {
        return (String) senderDetails().getOrDefault(PRIMARY_EMAIL,"");
    }

    public String getRecipientPrimaryEmail() {
        return (String) recipientDetails().getOrDefault(PRIMARY_EMAIL,"");
    }
    public void setErrorDetails(Map<String,Object> errorDetails){
        setHeaderMap(ERROR_DETAILS, errorDetails);
    }
    public List<String> getSenderRole() { return (List<String>) senderDetails().getOrDefault(ROLES,new ArrayList<>());}

    public List<String> getRecipientRole() { return (List<String>) recipientDetails().getOrDefault(ROLES,new ArrayList<>());}

    public Map<String, Object> senderDetails() { return (Map<String, Object>) payload.getOrDefault(SENDERDETAILS,new HashMap<>()); }
    public Map<String, Object> recipientDetails() {
        return (Map<String, Object>) payload.getOrDefault(RECIPIENTDETAILS,new HashMap<>());
    }
    public List<String> getSenderTag() { return (List<String>) senderDetails().getOrDefault(TAGS,new ArrayList<>());}

    public List<String> getRecipientTag() { return (List<String>) recipientDetails().getOrDefault(TAGS,new ArrayList<>());}

    public byte[] getPayloadSize() {
        return payload.getOrDefault(PAYLOAD, "").toString().getBytes();
    }
    public String getUserId() {
        if (StringUtils.equals(token.getEntityType(), "api-access")) {
            return token.getUserId();
        }
        return "";
    }

}

