package org.swasth.apigateway.models;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.utils.DateTimeUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.PayloadUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;


@Data
public class BaseRequest {

    private boolean isJSONRequest;
    private String apiAction = null;
    private Map<String, Object> protocolHeaders;
    private Map<String, Object> payload;
    private List<String> senderRole = new ArrayList<>();
    private List<String> recipientRole = new ArrayList<>();
    private String payloadWithoutSensitiveData = null;
    private String hcxRoles;
    private String hcxCode;

    public BaseRequest() {
    }

    public BaseRequest(Map<String, Object> payload, boolean isJSONRequest, String apiAction, String hcxCode, String hcxRoles) throws Exception {
        this.isJSONRequest = isJSONRequest;
        this.apiAction = apiAction;
        this.payload = payload;
        this.hcxRoles = hcxRoles;
        this.hcxCode = hcxCode;
        try {
            if (apiAction.contains(NOTIFICATION_NOTIFY)) {
                this.protocolHeaders = getProtocolHeaders(apiAction, 0);
                this.protocolHeaders.putAll(JSONUtils.decodeBase64String(getPayloadValues()[1], Map.class));
            } else if (!this.isJSONRequest) {
                this.protocolHeaders = getProtocolHeaders(apiAction, 0);
            } else {
                this.protocolHeaders = payload;
            }
            this.payloadWithoutSensitiveData = PayloadUtils.removeSensitiveData(payload, apiAction);
        } catch (JsonParseException e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, PAYLOAD_PARSE_ERR);
        }
    }

    private Map<String, Object> getProtocolHeaders(String apiAction, int i) throws Exception {
        return JSONUtils.decodeBase64String(validatePayload(apiAction)[i], Map.class);
    }

    public void validate(List<String> mandatoryHeaders, String subject, int timestampRange, Map<String, Object> senderDetails, Map<String, Object> recipientDetails, List<String> allowedParticipantStatus) throws Exception {
        for (Map.Entry<String, ClientException> entry : getResponseParamErrors().entrySet()) {
            validateHeader(protocolHeaders, entry.getKey(), entry.getValue());
        }

        List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !protocolHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            throw new ClientException(ErrorCodes.ERR_MANDATORY_HEADERFIELD_MISSING, MessageFormat.format(MISSING_MANDATORY_HEADERS, missingHeaders));
        }
        for (Map.Entry<String, ClientException> entry : getClientErrors(protocolHeaders).entrySet()) {
            validateHeader(protocolHeaders, entry.getKey(), entry.getValue());
        }

        validateCondition(!DateTimeUtils.validTimestamp(timestampRange, getTimestamp()), ErrorCodes.ERR_INVALID_TIMESTAMP, MessageFormat.format(TIMESTAMP_FUTURE_MSG, timestampRange));
        validateCondition(StringUtils.equals(getHcxSenderCode(), getHcxRecipientCode()), ErrorCodes.ERR_INVALID_SENDER_AND_RECIPIENT, SENDER_RECIPIENT_SAME_MSG);
        validateParticipant(recipientDetails, ErrorCodes.ERR_INVALID_RECIPIENT, "Recipient", getHcxRecipientCode(), allowedParticipantStatus);
        validateParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, "Sender", getHcxSenderCode(), allowedParticipantStatus);
        senderRole = (ArrayList<String>) senderDetails.get(ROLES);
        recipientRole = (ArrayList<String>) recipientDetails.get(ROLES);
        validateCondition(!StringUtils.equals(((ArrayList) senderDetails.get(OS_OWNER)).get(0).toString(), subject), ErrorCodes.ERR_ACCESS_DENIED, CALLER_MISMATCH_MSG);

        if (protocolHeaders.containsKey(DEBUG_FLAG)) {
            validateValues(getDebugFlag(), ErrorCodes.ERR_INVALID_DEBUG_FLAG, DEBUG_FLAG_ERR, DEBUG_FLAG_VALUES, DEBUG_FLAG_VALUES_ERR);
        }

        if (apiAction.contains("on_")) {
            validateCondition(!protocolHeaders.containsKey(STATUS), ErrorCodes.ERR_MANDATORY_HEADERFIELD_MISSING, MessageFormat.format(MANDATORY_STATUS_MISSING, STATUS));
            validateValues(getStatus(), ErrorCodes.ERR_INVALID_STATUS, STATUS_ERR, RESPONSE_STATUS_VALUES, STATUS_VALUES);
        } else {
            if (protocolHeaders.containsKey(STATUS)) {
                validateValues(getStatus(), ErrorCodes.ERR_INVALID_STATUS, STATUS_ERR, REQUEST_STATUS_VALUES, STATUS_ON_ACTION_VALUES);
            }
        }

        if (protocolHeaders.containsKey(ERROR_DETAILS)) {
            validateDetails(getErrorDetails(), ErrorCodes.ERR_INVALID_ERROR_DETAILS, ERROR_DETAILS_MSG, ERROR_DETAILS_VALUES, ERROR_VALUES);
            validateCondition(!RECIPIENT_ERROR_VALUES.contains(((Map<String, Object>) protocolHeaders.get(ERROR_DETAILS)).get("code")), ErrorCodes.ERR_INVALID_ERROR_DETAILS, INVALID_ERROR_CODE);
        }
        if (protocolHeaders.containsKey(DEBUG_DETAILS)) {
            validateDetails(getDebugDetails(), ErrorCodes.ERR_INVALID_DEBUG_DETAILS, DEBUG_DETAILS_ERR, ERROR_DETAILS_VALUES, DEBUG_DETAILS_VALUES_ERR);
        }

    }


    public void validateCondition(Boolean condition, ErrorCodes errorcode, String msg) throws ClientException {
        if (condition) {
            throw new ClientException(errorcode, msg);
        }
    }

    protected void validateParticipant(Map<String, Object> details, ErrorCodes code, String participant, String participantCode, List<String> allowedParticipantStatus) throws ClientException {
        ArrayList<String> roles = (ArrayList) details.get("roles");
        if (details.isEmpty()) {
            throw new ClientException(code, MessageFormat.format(MISSING_PARTICIPANT, participant));
        } else if (!allowedParticipantStatus.contains(details.get(REGISTRY_STATUS))) {
            throw new ClientException(code, MessageFormat.format(INVALID_REGISTRY_STATUS, allowedParticipantStatus, details.get(REGISTRY_STATUS)));
        }
        if (!apiAction.contains(NOTIFICATION_NOTIFY)) {
            if (participantCode.equals(hcxCode)) {
                throw new ClientException(code, MessageFormat.format(HCX_CODE_ERR, participant));
            }
            for (String notAllowRole : roles) {
                if (notAllowRole.equalsIgnoreCase(hcxRoles)) {
                    throw new ClientException(code, MessageFormat.format(HCX_ROLES_ERR, participant));
                }
            }
        }
    }

    protected void validateDetails(Map<String, Object> inputMap, ErrorCodes errorCode, String msg, List<String> rangeValues, String rangeMsg) throws ClientException {
        if (MapUtils.isEmpty(inputMap)) {
            throw new ClientException(errorCode, msg);
        } else if (!inputMap.containsKey("code") || !inputMap.containsKey("message")) {
            throw new ClientException(errorCode, MANDATORY_CODE_MSG);
        }
        for (String key : inputMap.keySet()) {
            if (!rangeValues.contains(key)) {
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

    protected Map<String, ClientException> getClientErrors(Map<String, Object> hcxHeaders) {
        Map<String, ClientException> clientErrors = new HashMap<>();
        clientErrors.put(HCX_SENDER_CODE, new ClientException(ErrorCodes.ERR_INVALID_SENDER, SENDER_CODE_ERR_MSG));
        clientErrors.put(HCX_RECIPIENT_CODE, new ClientException(ErrorCodes.ERR_INVALID_RECIPIENT, RECIPIENT_CODE_ERR_MSG));
        clientErrors.put(TIMESTAMP, new ClientException(ErrorCodes.ERR_INVALID_TIMESTAMP, TIMESTAMP_INVALID_MSG));
        if (hcxHeaders.containsKey(WORKFLOW_ID)) {
            clientErrors.put(WORKFLOW_ID, new ClientException(ErrorCodes.ERR_INVALID_WORKFLOW_ID, INVALID_WORKFLOW_ID_ERR_MSG));
        }
        return clientErrors;
    }

    protected Map<String, ClientException> getResponseParamErrors() {
        Map<String, ClientException> errors = new HashMap<>();
        errors.put(CORRELATION_ID, new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID, INVALID_CORRELATION_ID_ERR_MSG));
        errors.put(API_CALL_ID, new ClientException(ErrorCodes.ERR_INVALID_API_CALL_ID, INVALID_API_CALL_ID_ERR_MSG));
        return errors;
    }

    public void validateUsingAuditData(List<String> allowedEntitiesForForward, List<String> allowedRolesForForward, Map<String, Object> senderDetails, Map<String, Object> recipientDetails, List<Map<String, Object>> correlationAuditData, List<Map<String, Object>> callAuditData, List<Map<String, Object>> participantCtxAuditData, String path, JWERequest jweRequest, int correlationDataCloseDays, List<Map<String, Object>> correlationFilteredData) throws Exception {
        validateCondition(!callAuditData.isEmpty(), ErrorCodes.ERR_INVALID_API_CALL_ID, API_CALL_SAME_MSG);
        //validate correlation id belongs to same cycle or not
        if (!correlationAuditData.isEmpty()) {
            if (!correlationAuditData.get(0).get(ACTION).toString().contains(getEntity(apiAction))) {
                throw new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID, INVALID_CORRELATION_ID_CYCLE);
            }
        }

        // validate request cycle is not closed
        for (Map<String, Object> audit : correlationAuditData) {
            String action = (String) audit.get(ACTION);
            String entity = getEntity(action);
            validateCondition(!OPERATIONAL_ENTITIES.contains(entity) && action.contains("on_") && ((List<String>) audit.get(RECIPIENT_ROLE)).contains(PROVIDER) && audit.get(STATUS).equals(COMPLETE_STATUS) && !isWithinLastDays((String) audit.get(TIMESTAMP), correlationDataCloseDays), ErrorCodes.ERR_INVALID_CORRELATION_ID, CLOSED_CYCLE_MSG);
        }
        if (!correlationFilteredData.isEmpty()) {
            List<Map<String, Object>> filteredList = filteredList(correlationFilteredData, correlationDataCloseDays);
            if (filteredList.isEmpty() && correlationFilteredData.get(0).get(HCX_SENDER_CODE).toString().equals(jweRequest.getHcxSenderCode()) && correlationAuditData.get(0).get(CORRELATION_ID).toString().contains(jweRequest.getCorrelationId())) {
                throw new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID, CORRELATION_ID_DUPLICATE);
            }
        }
        if (apiAction.contains("on_")) {
            validateCondition(participantCtxAuditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, INVALID_ON_ACTION);
            validateWorkflowId(participantCtxAuditData.get(0));
        }
        List<String> senderRoles = (List<String>) senderDetails.get(ROLES);
        List<String> recipientRoles = (List<String>) recipientDetails.get(ROLES);
        // forward flow validations
        boolean isForwardReq = isForwardRequest(allowedRolesForForward, senderRoles, recipientRoles, correlationAuditData);
        if (isForwardReq) {
            validateCondition(!allowedEntitiesForForward.contains(getEntity(apiAction)), ErrorCodes.ERR_INVALID_FORWARD_REQ, INVALID_FORWARD);
            validateWorkflowId(correlationAuditData.get(0));
            if (!apiAction.contains("on_")) {
                for (Map<String, Object> audit : correlationAuditData) {
                    validateCondition(getHcxRecipientCode().equals(audit.get(HCX_SENDER_CODE)), ErrorCodes.ERR_INVALID_FORWARD_REQ, FORWARD_REQ_ERR_MSG);
                }
            }
        } else if (!EXCLUDE_ENTITIES.contains(getEntity(path)) && !apiAction.contains("on_") && checkParticipantRole(allowedRolesForForward, senderRoles) && recipientRoles.contains(PROVIDER)) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, INVALID_API_CALL);
        }
        // validation to check if participant is forwarding the request to provider
        if (isForwardReq && !apiAction.contains("on_") && recipientRoles.contains(PROVIDER)) {
            throw new ClientException(ErrorCodes.ERR_INVALID_FORWARD_REQ, INVALID_FORWARD_TO_PROVIDER);
        }
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

    protected String getDebugFlag() {
        return getHeader(DEBUG_FLAG);
    }

    public String getStatus() {
        return getHeader(STATUS);
    }

    protected String getHeader(String key) {
        return (String) protocolHeaders.getOrDefault(key, "");
    }

    public void setHeaders(Map<String, Object> headers) {
        protocolHeaders.putAll(headers);
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    protected List<String> getHeaderList(String key) {
        return (List<String>) protocolHeaders.getOrDefault(key, new ArrayList<>());
    }

    protected Map<String, Object> getHeaderMap(String key) {
        return (Map<String, Object>) protocolHeaders.getOrDefault(key, null);
    }

    private void setHeaderMap(String key, Object value) {
        protocolHeaders.put(key, value);
    }

    public Map<String, Object> getErrorDetails() {
        return getHeaderMap(ERROR_DETAILS);
    }

    public void setErrorDetails(Map<String, Object> errorDetails) {
        setHeaderMap(ERROR_DETAILS, errorDetails);
    }

    public Map<String, Object> getDebugDetails() {
        return getHeaderMap(DEBUG_DETAILS);
    }

    public String getRedirectTo() {
        return getHeader(REDIRECT_TO);
    }
    public String getPayloadWithoutSensitiveData() {
        return payloadWithoutSensitiveData;
    }

    private String[] validatePayload(String apiAction) throws Exception {
        try {
            String[] payloadValues = getPayloadValues();
            if (apiAction.contains(NOTIFICATION_NOTIFY)) {
                validatePayloadValues(payloadValues, NOTIFICATION_PAYLOAD_LENGTH);
            } else {
                validatePayloadValues(payloadValues, PROTOCOL_PAYLOAD_LENGTH);
            }
            return payloadValues;
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, PAYLOAD_PARSE_ERR);
        }
    }

    private String[] getPayloadValues() {
        return ((String) getPayload().get(PAYLOAD)).split("\\.");
    }

    private void validatePayloadValues(String[] payloadValues, int payloadLength) throws ClientException {
        if (payloadValues.length != payloadLength)
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, MALFORMED_PAYLOAD);
        for (String value : payloadValues) {
            if (value == null || value.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_PAYLOAD);
        }
    }

    public boolean isForwardRequest(List<String> allowedRolesForForward, List<String> senderRoles, List<String> recipientRoles, List<Map<String, Object>> auditData) throws ClientException {
        if (checkParticipantRole(allowedRolesForForward, senderRoles) && checkParticipantRole(allowedRolesForForward, recipientRoles)) {
            if (!auditData.isEmpty())
                return true;
            else
                throw new ClientException(ErrorCodes.ERR_INVALID_FORWARD_REQ, INVALID_FWD_CORRELATION_ID);
        }
        return false;
    }

    private void validateWorkflowId(Map<String, Object> auditEvent) throws ClientException {
        if (auditEvent.containsKey(WORKFLOW_ID) && !StringUtils.isEmpty((String) auditEvent.get(WORKFLOW_ID))) {
            validateCondition(!protocolHeaders.containsKey(WORKFLOW_ID) || !getWorkflowId().equals(auditEvent.get(WORKFLOW_ID)), ErrorCodes.ERR_INVALID_WORKFLOW_ID, INVALID_WORKFLOW_ID);
        }
    }

    private boolean checkParticipantRole(List<String> allowedRolesForForward, List<String> roles) {
        for (String role : roles) {
            if (allowedRolesForForward.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public String getEntity(String path) {
        if (path.contains("status")) {
            return "status";
        } else if (path.contains("search")) {
            return "search";
        } else {
            String[] str = path.split("/");
            return str[str.length - 2];
        }
    }

    private List<Map<String, Object>> filteredList(List<Map<String, Object>> correlationFilteredData, int days) {
        return correlationFilteredData.stream()
                .filter(map -> COMPLETE_STATUS.equals(map.get(STATUS)))
                .filter(map -> {
                    try {
                        return isWithinLastDays((String) map.get(TIMESTAMP), days);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    private boolean isWithinLastDays(String timestamp,int days) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date currentDate = new Date();
        Date date = sdf.parse(timestamp);
        long differenceInMillis = currentDate.getTime() - date.getTime();
        long daysDifference = TimeUnit.DAYS.convert(differenceInMillis, TimeUnit.MILLISECONDS);
        return daysDifference > days;
    }
}
