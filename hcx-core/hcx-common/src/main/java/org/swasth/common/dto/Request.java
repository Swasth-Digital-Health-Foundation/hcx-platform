package org.swasth.common.dto;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.DateTimeUtils;
import org.swasth.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.swasth.common.utils.Constants.*;

public class Request{

    private final Map<String, Object> payload;
    protected final Map<String, Object> hcxHeaders;

    public Request(Map<String, Object> body) throws Exception {
        this.payload = body;
        this.hcxHeaders = JSONUtils.decodeBase64String(((String) body.get(Constants.PAYLOAD)).split("\\.")[0], Map.class);
    }

    public void validate(List<String> mandatoryHeaders, HeaderAudit auditData, String apiAction, int timestampRange) throws ClientException {

        List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !hcxHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + missingHeaders);
        }
        for (Map.Entry<String, ClientException> entry : getClientErrors(hcxHeaders).entrySet()) {
            validateHeader(hcxHeaders, entry.getKey(), entry.getValue());
        }

        if(ON_ACTION_APIS.contains(apiAction)) {
            validateCondition(!getCorrelationId().equals(auditData.getCorrelation_id()), ErrorCodes.CLIENT_ERR_MISSING_CORRELATION_ID_RES, "Response contains invalid correlation id");
            if(!auditData.getWorkflow_id().isEmpty()) {
                validateCondition(!getWorkflowId().equals(auditData.getWorkflow_id()), ErrorCodes.CLIENT_ERR_MISSING_WORKFLOW_ID_RES, "Response contains invalid correlation id");
            }
        }
        validateCondition(!DateTimeUtils.validTimestamp(timestampRange, getTimestamp()), ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Timestamp cannot be more than " + timestampRange + " hours in the past or future time");

        if (hcxHeaders.containsKey(DEBUG_FLAG)) {
            validateValues(getDebugFlag(), ErrorCodes.CLIENT_ERR_INVALID_DEBUG_ID, "Debug flag cannot be null, empty and other than 'String'", DEBUG_FLAG_VALUES, ErrorCodes.CLIENT_ERR_DEBUG_ID_OUTOFRANGE, "Debug flag cannot be other than Error, Info or Debug");
        }
        validateValues(getStatus(), ErrorCodes.CLIENT_ERR_INVALID_STATUS, "Status cannot be null, empty and other than 'String'", STATUS_VALUES, ErrorCodes.CLIENT_ERR_STATUS_OUTOFRANGE, "Status value can be only: " + STATUS_VALUES);

        if (hcxHeaders.containsKey(ERROR_DETAILS)) {
            validateDetails(getErrorDetails(), ErrorCodes.CLIENT_ERR_INVALID_ERROR_DETAILS, "Error details cannot be null, empty and other than 'JSON Object'", ErrorCodes.CLIENT_ERR_ERROR_DETAILS_OUTOFRANGE, "Error details should contain only: ");
        }

        if (hcxHeaders.containsKey(DEBUG_DETAILS)) {
            validateDetails(getDebugDetails(), ErrorCodes.CLIENT_ERR_INVALID_DEBUG_DETAILS, "Debug details cannot be null, empty and other than 'JSON Object'", ErrorCodes.CLIENT_ERR_DEBUG_DETAILS_OUTOFRANGE, "Debug details should contain only: ");
        }

    }

    private void validateCondition(Boolean condition, ErrorCodes errorcode, String msg) throws ClientException {
        if(condition){
            throw new ClientException(errorcode, msg);
        }
    }

    private void validateDetails(Map<String, Object> inputMap, ErrorCodes errorCode, String msg, ErrorCodes rangeCode, String rangeMsg) throws ClientException {
        if (MapUtils.isEmpty(inputMap)) {
            throw new ClientException(errorCode, msg);
        } else if (!inputMap.keySet().containsAll(ERROR_DETAILS_VALUES)) {
            throw new ClientException(rangeCode, rangeMsg + ERROR_DETAILS_VALUES);
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

    private Map<String, ClientException> getClientErrors(Map<String, Object> hcxHeaders){
        Map<String, ClientException> clientErrors = new HashMap<>();
        clientErrors.put(TIMESTAMP, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Invalid timestamp"));
        if(hcxHeaders.containsKey(WORKFLOW_ID)) {
            clientErrors.put(WORKFLOW_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null, empty and other than 'String'"));
        }
        return clientErrors;
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

    public String getRecipientCode() { return getHeader(RECIPIENT_CODE); }

    public String getTimestamp() { return getHeader(TIMESTAMP); }

    public String getDebugFlag() { return getHeader(DEBUG_FLAG); }

    public String getStatus() { return getHeader(STATUS); }

    public Map<String,Object> getHcxHeaders() {
        return hcxHeaders;
    }

    protected String getHeader(String key) {
        return (String) hcxHeaders.getOrDefault(key, null);
    }

    protected Map<String,Object> getHeaderMap(String key){
        return (Map<String,Object>) hcxHeaders.getOrDefault(key,null);
    }

    private Map<String,Object> getErrorDetails(){ return getHeaderMap(ERROR_DETAILS); }

    private Map<String,Object> getDebugDetails(){ return getHeaderMap(DEBUG_DETAILS); }

}

