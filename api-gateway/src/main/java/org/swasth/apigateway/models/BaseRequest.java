package org.swasth.apigateway.models;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.utils.DateTimeUtils;
import org.swasth.apigateway.utils.JSONUtils;
import org.swasth.apigateway.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

import static org.swasth.apigateway.constants.Constants.*;

@Data
public class BaseRequest {

    private boolean isJSONRequest;
    private String apiAction = null;
    private Map<String,Object> protocolHeaders;
    private Map<String,Object> payload;
    private List<String> senderRole = new ArrayList<>();
    private List<String> recipientRole = new ArrayList<>();
    private String payloadWithoutEncryptionKey = null;

    public BaseRequest(){}

    public BaseRequest(Map<String, Object> payload,boolean isJSONRequest,String apiAction) throws Exception{
        this.isJSONRequest = isJSONRequest;
        this.apiAction = apiAction;
        this.payload = payload;
        try {
            if(this.isJSONRequest)
                this.protocolHeaders = payload;
            else
                this.protocolHeaders = JSONUtils.decodeBase64String(validateRequestBody(payload)[0], Map.class);
            this.payloadWithoutEncryptionKey = removeEncryptionKey(payload);
        } catch (JsonParseException e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Error while parsing protected headers");
        }
    }

    public void validate(List<String> mandatoryHeaders,String subject, int timestampRange,Map<String,Object> senderDetails,Map<String,Object> recipientDetails) throws Exception {
        for (Map.Entry<String, ClientException> entry : getResponseParamErrors().entrySet()) {
            validateHeader(protocolHeaders, entry.getKey(), entry.getValue());
        }
        validateCondition(!Utils.isUUID(getApiCallId()), ErrorCodes.ERR_INVALID_API_CALL_ID, "API call id should be a valid UUID");
        validateCondition(!Utils.isUUID(getCorrelationId()), ErrorCodes.ERR_INVALID_CORRELATION_ID, "Correlation id should be a valid UUID");

        List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !protocolHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            throw new ClientException(ErrorCodes.ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + missingHeaders);
        }
        for (Map.Entry<String, ClientException> entry : getClientErrors(protocolHeaders).entrySet()) {
            validateHeader(protocolHeaders, entry.getKey(), entry.getValue());
        }

        validateCondition(!DateTimeUtils.validTimestamp(timestampRange, getTimestamp()), ErrorCodes.ERR_INVALID_TIMESTAMP, "Timestamp cannot be more than " + timestampRange + " hours in the past or future time");
        validateCondition(protocolHeaders.containsKey(WORKFLOW_ID) && !Utils.isUUID(getWorkflowId()), ErrorCodes.ERR_INVALID_WORKFLOW_ID, "Workflow id should be a valid UUID");
        validateCondition(StringUtils.equals(getSenderCode(), getRecipientCode()), ErrorCodes.ERR_INVALID_SENDER_AND_RECIPIENT, "sender and recipient code cannot be the same");
        validateParticipant(recipientDetails, ErrorCodes.ERR_INVALID_RECIPIENT, "recipient");
        validateParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, "sender");
        senderRole = (ArrayList<String>) senderDetails.get(ROLES);
        recipientRole = (ArrayList<String>) recipientDetails.get(ROLES);
        validateCondition(!StringUtils.equals(((ArrayList) senderDetails.get(OS_OWNER)).get(0).toString(), subject), ErrorCodes.ERR_ACCESS_DENIED, "Caller id and sender code is not matched");

        if (protocolHeaders.containsKey(DEBUG_FLAG)) {
            validateValues(getDebugFlag(), ErrorCodes.ERR_INVALID_DEBUG_FLAG, "Debug flag cannot be null, empty and other than 'String'", DEBUG_FLAG_VALUES, "Debug flag cannot be other than Error, Info or Debug");
        }

        if (apiAction.contains("on_")) {
            validateCondition(!protocolHeaders.containsKey(STATUS), ErrorCodes.ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + STATUS);
            validateValues(getStatus(), ErrorCodes.ERR_INVALID_STATUS, "Status cannot be null, empty and other than 'String'", RESPONSE_STATUS_VALUES, "Status value for on_* API calls can be only: ");
        } else {
            if (protocolHeaders.containsKey(STATUS)) {
                validateValues(getStatus(), ErrorCodes.ERR_INVALID_STATUS, "Status cannot be null, empty and other than 'String'", REQUEST_STATUS_VALUES, "Status value for *action API calls can be only: ");
            }
        }

        if (protocolHeaders.containsKey(ERROR_DETAILS)) {
            validateDetails(getErrorDetails(), ErrorCodes.ERR_INVALID_ERROR_DETAILS, "Error details cannot be null, empty and other than 'JSON Object'", ERROR_DETAILS_VALUES, "Error details should contain only: ");
            validateCondition(!RECIPIENT_ERROR_VALUES.contains(((Map<String,Object>) protocolHeaders.get(ERROR_DETAILS)).get("code")),ErrorCodes.ERR_INVALID_ERROR_DETAILS,"Invalid Error Code");
        }
        if (protocolHeaders.containsKey(DEBUG_DETAILS)) {
            validateDetails(getDebugDetails(), ErrorCodes.ERR_INVALID_DEBUG_DETAILS, "Debug details cannot be null, empty and other than 'JSON Object'", ERROR_DETAILS_VALUES, "Debug details should contain only: ");
        }

    }

    protected void validateCondition(Boolean condition, ErrorCodes errorcode, String msg) throws ClientException {
        if(condition){
            throw new ClientException(errorcode, msg);
        }
    }

    protected void validateParticipant(Map<String,Object> details, ErrorCodes code, String participant) throws ClientException {
        if(details.isEmpty()){
            throw new ClientException(code, participant + " does not exist in registry");
        } else if(StringUtils.equals((String) details.get("status"), BLOCKED) || StringUtils.equals((String) details.get("status"), INACTIVE)){
            throw new ClientException(code, participant + "  is blocked or inactive as per the registry");
        }
    }

    protected void validateDetails(Map<String, Object> inputMap, ErrorCodes errorCode, String msg, List<String> rangeValues, String rangeMsg) throws ClientException {
        if (MapUtils.isEmpty(inputMap)) {
            throw new ClientException(errorCode, msg);
        } else if (!inputMap.containsKey("code") || !inputMap.containsKey("message")) {
            throw new ClientException(errorCode, "Mandatory fields code or message is missing");
        }
        for(String key: inputMap.keySet()){
            if(!rangeValues.contains(key)){
                throw new ClientException(errorCode, rangeMsg + rangeValues);
            }
        }
    }

    protected void validateValues(String inputStr, ErrorCodes errorCode, String msg, List<String> statusValues, String rangeMsg) throws ClientException {
        if (StringUtils.isEmpty(inputStr)) {
            throw new ClientException(errorCode, msg);
        } else if (!statusValues.contains(inputStr)) {
            throw new ClientException(errorCode, rangeMsg + statusValues);
        }
    }

    protected void validateHeader(Map<String, Object> headers, String key, ClientException ex) throws ClientException {
        if (headers.containsKey(key)) {
            Object value = headers.get(key);
            if (!(value instanceof String) || StringUtils.isEmpty((String) value))
                throw ex;
        } else throw ex;
    }

    protected Map<String, ClientException> getClientErrors(Map<String, Object> hcxHeaders){
        Map<String, ClientException> clientErrors = new HashMap<>();
        clientErrors.put(SENDER_CODE, new ClientException(ErrorCodes.ERR_INVALID_SENDER, "Sender code cannot be null, empty and other than 'String'"));
        clientErrors.put(RECIPIENT_CODE, new ClientException(ErrorCodes.ERR_INVALID_RECIPIENT, "Recipient code cannot be null, empty and other than 'String'"));
        clientErrors.put(TIMESTAMP, new ClientException(ErrorCodes.ERR_INVALID_TIMESTAMP, "Invalid timestamp"));
        if(hcxHeaders.containsKey(WORKFLOW_ID)) {
            clientErrors.put(WORKFLOW_ID, new ClientException(ErrorCodes.ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null, empty and other than 'String'"));
        }
        return clientErrors;
    }

    protected Map<String, ClientException> getResponseParamErrors(){
        Map<String, ClientException> errors = new HashMap<>();
        errors.put(CORRELATION_ID, new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID, "Correlation id cannot be null, empty and other than 'String'"));
        errors.put(API_CALL_ID, new ClientException(ErrorCodes.ERR_INVALID_API_CALL_ID, "Api call id cannot be null, empty and other than 'String'"));
        return errors;
    }

    public void validateUsingAuditData(List<String> allowedEntitiesForForward, List<String> allowedRolesForForward, Map<String,Object> senderDetails, Map<String,Object> recipientDetails, List<Map<String,Object>> correlationAuditData, List<Map<String,Object>> callAuditData, List<Map<String,Object>> participantCtxAuditData, String path ) throws Exception {
        validateCondition(!callAuditData.isEmpty(), ErrorCodes.ERR_INVALID_API_CALL_ID, "Request exist with same api call id");
        // validate request cycle is not closed
        for(Map<String,Object> audit: correlationAuditData){
            String action = (String) audit.get(ACTION);
            String entity = getEntity(action);
            validateCondition(!OPERATIONAL_ENTITIES.contains(entity) && action.contains("on_") && ((List<String>) audit.get(RECIPIENT_ROLE)).contains(PROVIDER) && audit.get(STATUS).equals(COMPLETE_STATUS), ErrorCodes.ERR_INVALID_CORRELATION_ID, "Invalid request, cycle is closed for correlation id");
        }
        if(apiAction.contains("on_")) {
            validateCondition(participantCtxAuditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, "Invalid on_action request, corresponding action request does not exist");
            validateWorkflowId(participantCtxAuditData.get(0));
        }
        List<String> senderRoles = (List<String>) senderDetails.get(ROLES);
        List<String> recipientRoles = (List<String>) recipientDetails.get(ROLES);
        // forward flow validations
        if(isForwardRequest(allowedRolesForForward, senderRoles, recipientRoles, correlationAuditData)){
            validateCondition(!allowedEntitiesForForward.contains(getEntity(apiAction)), ErrorCodes.ERR_INVALID_FORWARD_REQ, "Entity is not allowed for forwarding");
            validateWorkflowId(correlationAuditData.get(0));
            if(!apiAction.contains("on_")){
                for (Map<String, Object> audit : correlationAuditData) {
                    validateCondition(getRecipientCode().equals(audit.get(SENDER_CODE)), ErrorCodes.ERR_INVALID_FORWARD_REQ, "Request cannot be forwarded to the forward initiators");
                }
            }
        } else if( !EXCLUDE_ENTITIES.contains(getEntity(path)) && !apiAction.contains("on_") && checkParticipantRole(allowedRolesForForward, senderRoles) && recipientRoles.contains(PROVIDER)) {
            throw new ClientException("Invalid recipient");
        }
    }

    protected String[] validateRequestBody(Map<String, Object> requestBody) throws Exception {
        try {
            String[] payloadValues = ((String) requestBody.get(PAYLOAD)).split("\\.");
            if(payloadValues.length != PAYLOAD_LENGTH)
                throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid payload");
            for(String value: payloadValues){
                if(value == null || value.isEmpty())
                    throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid payload");
            }
            return payloadValues;
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid payload");
        }
    }

    public boolean isForwardRequest(List<String> allowedRolesForForward, List<String> senderRoles, List<String> recipientRoles, List<Map<String,Object>> auditData) throws ClientException {
        if(checkParticipantRole(allowedRolesForForward, senderRoles) && checkParticipantRole(allowedRolesForForward, recipientRoles)){
            if(!auditData.isEmpty())
                return true;
            else
                throw new ClientException(ErrorCodes.ERR_INVALID_FORWARD_REQ, "The request contains invalid correlation id");
        }
        return false;
    }

    private void validateWorkflowId(Map<String, Object> auditEvent) throws ClientException {
        if (auditEvent.containsKey(WORKFLOW_ID)) {
            validateCondition(!protocolHeaders.containsKey(WORKFLOW_ID) || !getWorkflowId().equals(auditEvent.get(WORKFLOW_ID)), ErrorCodes.ERR_INVALID_WORKFLOW_ID, "The request contains invalid workflow id");
        }
    }

    private boolean checkParticipantRole(List<String> allowedRolesForForward, List<String> roles){
        for(String role: roles){
            if (allowedRolesForForward.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public String getEntity(String path){
        if (path.contains("status")) {
            return "status";
        } else if (path.contains("search")) {
            return "search";
        } else {
            String[] str = path.split("/");
            return str[str.length-2];
        }
    }

    private String removeEncryptionKey(Map<String, Object> payload) throws JsonProcessingException {
        if(isJSONRequest()) {
            return JSONUtils.serialize(payload);
        } else {
            List<String> modifiedPayload = new ArrayList<>(Arrays.asList(payload.get(PAYLOAD).toString().split("\\.")));
            modifiedPayload.remove(1);
            String[] payloadValues = modifiedPayload.toArray(new String[modifiedPayload.size()]);
            StringBuilder sb = new StringBuilder();
            for(String value: payloadValues) {
                sb.append(value).append(".");
            }
            return sb.deleteCharAt(sb.length()-1).toString();
        }
    }

    public String getWorkflowId() { return getHeader(WORKFLOW_ID); }

    public String getApiCallId() { return getHeader(API_CALL_ID); }

    public String getCorrelationId() { return getHeader(CORRELATION_ID); }

    public String getSenderCode() { return getHeader(SENDER_CODE); }

    public String getRecipientCode() { return getHeader(RECIPIENT_CODE); }

    public String getTimestamp() { return getHeader(TIMESTAMP); }

    protected String getDebugFlag() { return getHeader(DEBUG_FLAG); }

    public String getStatus() { return getHeader(STATUS); }

    private String getHeader(String key) { return (String) protocolHeaders.getOrDefault(key, null); }

    private Map<String,Object> getHeaderMap(String key){ return (Map<String,Object>) protocolHeaders.getOrDefault(key,null); }
    private void setHeaderMap(String key, Object value){ protocolHeaders.put(key, value); }

    public Map<String,Object> getErrorDetails(){ return getHeaderMap(ERROR_DETAILS); }
    public void setErrorDetails(Map<String,Object> errorDetails){ setHeaderMap(ERROR_DETAILS, errorDetails); }

    public Map<String,Object> getDebugDetails(){ return getHeaderMap(DEBUG_DETAILS); }

    public String getRedirectTo() { return getHeader(REDIRECT_TO); }

    public Map<String,Object> getPayload(){ return payload; }

    public List<String> getSenderRole() { return senderRole; }
    public List<String> getRecipientRole() { return recipientRole; }

    public String getPayloadWithoutEncryptionKey() { return payloadWithoutEncryptionKey; }

}
