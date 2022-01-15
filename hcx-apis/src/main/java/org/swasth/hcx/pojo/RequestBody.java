package org.swasth.hcx.pojo;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.common.JsonUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.hcx.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestBody {

    private final Map<String, Object> payload;
    protected final Map<String, Object> hcxHeaders;

    public RequestBody(Map<String, Object> body) throws Exception {
        this.payload = format(body);
        String protectedStr = (String) payload.getOrDefault(Constants.PROTECTED, "");
        this.hcxHeaders = decodeProtected(protectedStr);
    }

    public void validate(List<String> mandatoryHeaders,List<String> payloadKeys) throws ClientException {

        //TODO check and remove this validation payload properties
        List<String> missingPayloadProps = payloadKeys.stream().filter(key -> !payload.containsKey(key)).collect(Collectors.toList());
        if (!missingPayloadProps.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "Payload mandatory properties are missing: " + missingPayloadProps);
        }

        List<String> missing = mandatoryHeaders.stream().filter(key -> !hcxHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + missing);
        }
        for (Map.Entry<String, ClientException> entry : getClientErrors().entrySet()) {
            validateHeader(hcxHeaders, entry.getKey(), entry.getValue());
        }

        if (hcxHeaders.containsKey(Constants.DEBUG_FLAG)) {
            validateValues(getDebugFlag(), ErrorCodes.CLIENT_ERR_INVALID_DEBUG_ID, "Debug flag cannot be null, empty and other than 'String'", Constants.DEBUG_FLAG_VALUES, ErrorCodes.CLIENT_ERR_DEBUG_ID_OUTOFRANGE, "Debug flag cannot be other than Error, Info or Debug");
        }
        validateValues(getStatus(), ErrorCodes.CLIENT_ERR_INVALID_STATUS, "Status cannot be null, empty and other than 'String'", Constants.STATUS_VALUES, ErrorCodes.CLIENT_ERR_STATUS_OUTOFRANGE, "Status value can be only: " + Constants.STATUS_VALUES);

        if (hcxHeaders.containsKey(Constants.ERROR_DETAILS)) {
            validateDetails(getErrorDetails(), ErrorCodes.CLIENT_ERR_INVALID_ERROR_DETAILS, "Error details cannot be null, empty and other than 'JSON Object'", ErrorCodes.CLIENT_ERR_ERROR_DETAILS_OUTOFRANGE, "Error details should contain only: ");
        }

        if (hcxHeaders.containsKey(Constants.DEBUG_DETAILS)) {
            validateDetails(getDebugDetails(), ErrorCodes.CLIENT_ERR_INVALID_DEBUG_DETAILS, "Debug details cannot be null, empty and other than 'JSON Object'", ErrorCodes.CLIENT_ERR_DEBUG_DETAILS_OUTOFRANGE, "Debug details should contain only: ");
        }

    }

    private void validateDetails(Map<String, Object> inputMap, ErrorCodes errorCode, String msg, ErrorCodes rangeCode, String rangeMsg) throws ClientException {
            if (MapUtils.isEmpty(inputMap)) {
                throw new ClientException(errorCode, msg);
            } else if (!inputMap.keySet().containsAll(Constants.ERROR_DETAILS_VALUES)) {
                throw new ClientException(rangeCode, rangeMsg + Constants.ERROR_DETAILS_VALUES);
            }
    }

    private void validateValues(String inputStr, ErrorCodes errorCode, String msg, List<String> statusValues, ErrorCodes rangeCode, String rangeMsg) throws ClientException {
        if (StringUtils.isEmpty(inputStr)) {
            throw new ClientException(errorCode, msg);
        } else if (!statusValues.contains(inputStr)) {
            throw new ClientException(rangeCode, rangeMsg);
        }
    }

    private void validateHeader(Map<String, Object> headers, String key, ClientException ex) throws ClientException {
        if (headers.containsKey(key)) {
            Object value = headers.get(key);
            if (!(value instanceof String) || StringUtils.isEmpty((String) value))
                throw ex;
        } else throw ex;
    }

    private Map<String, ClientException> getClientErrors(){
        Map<String, ClientException> clientErrors = new HashMap<>();
        clientErrors.put(Constants.SENDER_CODE, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender code cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.RECIPIENT_CODE, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient code cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.WORKFLOW_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.REQUEST_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_ID, "Request id cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.TIMESTAMP, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Invalid timestamp"));
        //clientErrors.put(Constants.CASE_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CASE_ID, "Case id cannot be null, empty and other than 'String'"));
        return clientErrors;
    }

    // TODO remove this method. We should restrict accessing it to have a clean code.
    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getWorkflowId() {
        return getHeader(Constants.WORKFLOW_ID);
    }

    public String getRequestId() {
        return getHeader(Constants.REQUEST_ID);
    }

    public String getSenderCode() {
        return getHeader(Constants.SENDER_CODE);
    }

    public String getRecipientCode() { return getHeader(Constants.RECIPIENT_CODE); }

    public String getDebugFlag() { return getHeader(Constants.DEBUG_FLAG); }

    public String getStatus() { return getHeader(Constants.STATUS); }

    protected String getHeader(String key) {
        return (String) hcxHeaders.getOrDefault(key, null);
    }

    protected Map<String,Object> getHeaderMap(String key){
        return (Map<String,Object>) hcxHeaders.getOrDefault(key,null);
    }

    private Map<String,Object> getErrorDetails(){ return getHeaderMap(Constants.ERROR_DETAILS); }

    private Map<String,Object> getDebugDetails(){ return getHeaderMap(Constants.DEBUG_DETAILS); }

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
            payload.put(Constants.PROTECTED, strArray[0]);
            payload.put(Constants.ENCRYPTED_KEY, strArray[1]);
            payload.put(Constants.AAD, strArray[2]);
            payload.put(Constants.IV, strArray[3]);
            payload.put(Constants.CIPHERTEXT, strArray[4]);
            payload.put(Constants.TAG, strArray[5]);
            return payload;
        } else {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }
    }

}
