package org.swasth.hcx.pojo;

import org.swasth.common.JsonUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.hcx.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestBody {

    private Map<String, Object> payload;
    private Map<String, Object> hcxHeaders;

    public RequestBody(Map<String, Object> body) throws Exception {
        this.payload = format(body);
        String protectedStr = (String) payload.getOrDefault("protected", "");
        this.hcxHeaders = decodeProtected(protectedStr);
    }

    public void validate(List<String> mandatoryHeaders) throws ClientException {
        List<String> missing = mandatoryHeaders.stream().filter(key -> !hcxHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + missing);
        }

        // TODO remaining validation.

    }

    // TODO remove this method. We should restrict accessing it to have a clean code.
    public Map<String, Object> getPayload() {
        return payload;
    }

    // TODO try to change it as private method. We should restrict accessing it to have a clean code.
    public Map<String, Object> getHCXHeaders() {
        return hcxHeaders;
    }

    public String getWorkflowId() {
        return getHeader(Constants.WORKFLOW_ID);
    }

    public String getRequestId() {
        return getHeader(Constants.REQUEST_ID);
    }

    public String getCaseId() {
        return getHeader(Constants.CASE_ID);
    }

    public String getSenderCode() {
        return getHeader(Constants.SENDER_CODE);
    }

    public String getRecipientCode() {
        return getHeader(Constants.RECIPIENT_CODE);
    }

    // TODO write methods for any other required headers to fetch.

    protected String getHeader(String key) {
        return (String) hcxHeaders.getOrDefault(key, null);
    }

    private Map<String, Object> decodeProtected(String protectedStr) throws Exception {
        try {
            return JsonUtils.decodeBase64String(protectedStr, Map.class);
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_PROTECTED, "Error while parsing protected headers");
        }
    }

    private Map<String, Object> format(Map<String, Object> body) throws ClientException {
        String str = (String) body.getOrDefault("payload", "");
        String[] strArray = str.split("\\.");
        if (strArray.length > 0 && strArray.length == Constants.PAYLOAD_LENGTH) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("protected", strArray[0]);
            payload.put("encrypted_key", strArray[1]);
            payload.put("aad", strArray[2]);
            payload.put("iv", strArray[3]);
            payload.put("ciphertext", strArray[4]);
            payload.put("tag", strArray[5]);
            return payload;
        } else {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }
    }
}
