package org.swasth.common.dto;

import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.PayloadUtils;

import java.util.Map;
import java.util.UUID;

import static org.swasth.common.utils.Constants.*;

public class Request {

    private final Map<String, Object> payload;
    protected Map<String, Object> hcxHeaders = null;
    private String mid = UUID.randomUUID().toString();
    private String apiAction;
    private final String payloadWithoutSensitiveData;

    public Request(Map<String, Object> body, String apiAction) throws Exception {
        this.apiAction = apiAction;
        this.payload = body;
        try {
            if (body.containsKey(PAYLOAD)) {
                hcxHeaders = JSONUtils.decodeBase64String(((String) body.get(PAYLOAD)).split("\\.")[0], Map.class);
            } else //FIXME  No need of this check here, as all JSON request bodies will have proper body structure post validations if (body.containsKey(STATUS))
                hcxHeaders = body;
            this.payloadWithoutSensitiveData = PayloadUtils.removeSensitiveData(body);
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid Payload");
        }
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

    public String getSenderCode() {
        return getHeader(SENDER_CODE);
    }

    public String getRecipientCode() {
        return getHeader(RECIPIENT_CODE);
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
        return (String) hcxHeaders.getOrDefault(key, null);
    }

    protected Map<String, Object> getHeaderMap(String key) {
        return (Map<String, Object>) hcxHeaders.getOrDefault(key, null);
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

    public String getNotificationId() { return getHeader(NOTIFICATION_ID);}
    public Map<String, Object> getNotificationData(){ return getHeaderMap(NOTIFICATION_DATA);}

}

