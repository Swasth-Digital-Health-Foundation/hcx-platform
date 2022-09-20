package org.swasth.apigateway.models;

import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Data
public class JSONRequest extends BaseRequest {

    public JSONRequest(Map<String, Object> payload, boolean isJSONRequest, String apiAction, String hcxCode, String hcxRoles) throws Exception {
        super(payload, isJSONRequest, apiAction, hcxCode, hcxRoles);
    }

    public void validateRedirect(List<String> allowedRoles, Map<String, Object> redirectDetails, List<Map<String, Object>> callAuditData, List<Map<String, Object>> correlationAuditData) throws Exception {
        validateCondition(StringUtils.isEmpty(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, MessageFormat.format(INVALID_REDIRECT_MSG, REDIRECT_TO));
        validateCondition(getHcxSenderCode().equalsIgnoreCase(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, INVALID_REDIRECT_SELF);

        validateParticipant(redirectDetails, ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected", getRedirectTo());
        List<String> redirectRoles = (List<String>) redirectDetails.get(ROLES);
        validateCondition(!hasRole(allowedRoles, redirectRoles), ErrorCodes.ERR_INVALID_REDIRECT_TO, INVALID_REDIRECT_PARTICIPANT);

        validateCondition(!callAuditData.isEmpty(), ErrorCodes.ERR_INVALID_API_CALL_ID, MessageFormat.format(INVALID_API_CALL, getApiCallId()));
        validateCondition(correlationAuditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, ON_ACTION_CORRELATION_ERR_MSG);
        Map<String, Object> auditEvent = (Map<String, Object>) correlationAuditData.get(0);
        if (auditEvent.containsKey(WORKFLOW_ID) && !((String) auditEvent.get(WORKFLOW_ID)).isEmpty()) {
            validateCondition(!getProtocolHeaders().containsKey(WORKFLOW_ID) || !getWorkflowId().equals(auditEvent.get(WORKFLOW_ID)), ErrorCodes.ERR_INVALID_WORKFLOW_ID, ON_ACTION_WORKFLOW_ID);
        }

        for (Object audit : correlationAuditData) {
            Map<String, Object> auditEventData = (Map<String, Object>) audit;
            //Check for only on_* requests from Audit data
            if (((String) auditEventData.get(ACTION)).contains("on_")) {
                if (auditEventData.containsKey(STATUS)) {
                    //Identifying any complete responses
                    validateCondition(COMPLETE_STATUS.equals(auditEvent.get(STATUS)), ErrorCodes.ERR_INVALID_REDIRECT_TO, CLOSED_REDIRECT_MSG);
                }
                //validate the redirected has any one of request initiators
                validateCondition(getRedirectTo().equalsIgnoreCase((String) auditEventData.get(HCX_SENDER_CODE)), ErrorCodes.ERR_INVALID_REDIRECT_TO, REDIRECT_INITIATOR_MSG);
            }
        }
    }

    private boolean hasRole(List<String> allowedRoles, List<String> participantRoles) {
        boolean hasAccess = false;
        for (String allowRole : allowedRoles) {
            if (participantRoles.contains(allowRole)) {
                hasAccess = true;
                break;
            }
        }
        return hasAccess;
    }

    /**
     * This method is to validate notification notify request
     */
    public void validateNotificationReq(Map<String, Object> senderDetails, List<Map<String, Object>> recipientsDetails, List<String> allowedNetworkCodes, boolean isValidSignature) throws Exception {
        validateNotificationParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, SENDER);
        validateCondition(StringUtils.isEmpty(getAlg()) || !getAlg().equalsIgnoreCase(Constants.RS256), ErrorCodes.ERR_INVALID_ALGORITHM, INVALID_ALGO);
        validateCondition(!isValidSignature, ErrorCodes.ERR_INVALID_SIGNATURE, INVALID_JWS);
        validateCondition(getNotificationTimestamp() == null, ErrorCodes.ERR_INVALID_NOTIFICATION_TIMESTAMP, NOTIFICATION_TS_MSG);
        validateCondition(MapUtils.isEmpty(getNotificationHeaders()), ErrorCodes.ERR_INVALID_NOTIFICATION_HEADERS, NOTIFICATION_HEADER_ERR_MSG);
        validateCondition(getProtocolHeaders().containsKey(EXPIRY) && new DateTime(getExpiry()).isBefore(DateTime.now()), ErrorCodes.ERR_INVALID_NOTIFICATION_EXPIRY, NOTIFICATION_EXPIRY);
        validateCondition(StringUtils.isEmpty(getRecipientType()), ErrorCodes.ERR_INVALID_NOTIFICATION_RECIPIENT_TYPE, NOTIFICATION_RECIPIENT_ERR_MSG);
        validateCondition(getRecipients().size() == 0, ErrorCodes.ERR_INVALID_NOTIFICATION_RECIPIENTS, NOTIFICATION_RECIPIENT_LIST);
        validateCondition(StringUtils.isEmpty(getNotificationMessage()), ErrorCodes.ERR_INVALID_NOTIFICATION_MESSAGE, NOTIFICATION_MESSAGE_ERR);
        validateCondition(StringUtils.isEmpty(getTopicCode()), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, NOTIFICATION_TOPIC_ERR);
        validateCondition(!NotificationUtils.isValidCode(getTopicCode()), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, MessageFormat.format(NOTIFICATION_INVALID_TOPIC, getTopicCode()));
        Map<String, Object> notification = NotificationUtils.getNotification(getTopicCode());
        validateCondition(notification.get("status").equals(Constants.INACTIVE), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, NOTIFICATION_STATUS);
        if (notification.get(Constants.CATEGORY).equals(Constants.NETWORK)) {
            validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_SENDERS), (List<String>) senderDetails.get(ROLES)) || !allowedNetworkCodes.contains(senderDetails.get(Constants.PARTICIPANT_CODE)),
                    ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(NOTIFICATION_TRIGGER_ERR, getTopicCode()));
        }

        if (getRecipientType().equalsIgnoreCase(PARTICIPANT_CODE)) { // validate recipient codes
            List<String> fetchedCodes = recipientsDetails.stream().map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
            List<String> invalidRecipients = getRecipients().stream().filter(code -> !fetchedCodes.contains(code)).collect(Collectors.toList());
            if (getRecipients().size() == 1 && !invalidRecipients.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(MISSING_PARTICIPANT, invalidRecipients));
            for (Map<String, Object> recipient : recipientsDetails) {
                validateNotificationParticipant(recipient, ErrorCodes.ERR_INVALID_RECIPIENT, Constants.RECIPIENT);
                validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_RECIPIENTS), (List<String>) recipient.get(ROLES)),
                        ErrorCodes.ERR_INVALID_RECIPIENT, MessageFormat.format(NOTIFICATION_RECIPIENT_ERR, recipient.get(Constants.PARTICIPANT_CODE), getTopicCode()));
            }
        } else if (getRecipientType().equalsIgnoreCase(PARTICIPANT_ROLE)) { // validate recipient roles
            List<String> allowedRecipients = (List<String>) notification.get(Constants.ALLOWED_RECIPIENTS);
            for (String role : getRecipients()) {
                validateCondition(!allowedRecipients.contains(role), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ,
                        MessageFormat.format(NOTIFICATION_NOT_ALLOWED_RECIPIENTS, notification.get(Constants.ALLOWED_RECIPIENTS)));
            }
        }
    }

    public void validateNotificationParticipant(Map<String, Object> details, ErrorCodes code, String participant) throws ClientException {
        if (details.isEmpty()) {
            throw new ClientException(code, MessageFormat.format(MISSING_PARTICIPANT, participant));
        } else if (StringUtils.equals((String) details.get(REGISTRY_STATUS), BLOCKED) || StringUtils.equals((String) details.get(REGISTRY_STATUS), INACTIVE)) {
            throw new ClientException(code, MessageFormat.format(INVALID_REGISTRY_STATUS, participant));
        }
    }

    public String getTopicCode() {
        return getHeader(Constants.TOPIC_CODE);
    }

    public List<String> getRecipients() {
        return getHeaderList(Constants.RECIPIENTS);
    }

    public String getNotificationMessage() {
        return getHeader(MESSAGE);
    }

    public Map<String, Object> getNotificationHeaders() {
        return getHeaderMap(NOTIFICATION_HEADERS);
    }

    public String getAlg() {
        return getHeader(ALG);
    }

    public Long getNotificationTimestamp() {
        return (Long) getProtocolHeaders().getOrDefault("timestamp", null);
    }

    public Long getExpiry() {
        return (Long) getProtocolHeaders().getOrDefault(EXPIRY, null);
    }

    public String getNotificationPayload() {
        return getHeader(PAYLOAD);
    }

    public List<String> getSenderList() {
        return (List<String>) getProtocolHeaders().getOrDefault(SENDER_LIST, new ArrayList<>());
    }

    public String getRecipientType() {
        return getHeader(RECIPIENT_TYPE);
    }

    public String getSenderCode() {
        return getHeader(SENDER_CODE);
    }

    public void validateSubscriptionRequests(String topicCode, List<Map<String, Object>> senderListDetails, Map<String, Object> recipientDetails, List<String> subscriptionMandatoryHeaders, Map<String, Object> notification) throws ClientException {
        for (String subscriptionMandatoryHeader : subscriptionMandatoryHeaders) {
            validateCondition(!getProtocolHeaders().containsKey(subscriptionMandatoryHeader), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(NOTIFICATION_MANDATORY_HEADERS, TOPIC_CODE, SENDER_LIST));
        }

        // validate recipient details who was intended notification recipient post successful subscription
        validateNotificationParticipant(recipientDetails, ErrorCodes.ERR_INVALID_SENDER, SENDER);
        // topicCode is present in the notifications list
        validateCondition(StringUtils.isEmpty(topicCode), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, NOTIFICATION_TOPIC_ERR);
        validateCondition(!NotificationUtils.isValidCode(topicCode), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, MessageFormat.format(NOTIFICATION_INVALID_TOPIC, topicCode));

        validateCondition(notification.get(Constants.AUDIT_STATUS).equals(Constants.INACTIVE), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, NOTIFICATION_STATUS);
        // Whether user has authorised roles as per the provided roles for this topicCode in the notifications list
        validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_RECIPIENTS), (List<String>) recipientDetails.get(ROLES)),
                ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(NOTIFICATION_NOT_ALLOWED, topicCode));
        // All participants in the senderList are valid in the registry
        if (!getSenderList().isEmpty()) {
            List<String> fetchedCodes = senderListDetails.stream().map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
            List<String> invalidSenders = getSenderList().stream().filter(code -> !fetchedCodes.contains(code)).collect(Collectors.toList());
            if (!invalidSenders.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, MessageFormat.format(MISSING_PARTICIPANT, invalidSenders));
            for (Map<String, Object> recipient : senderListDetails) {
                validateNotificationParticipant(recipient, ErrorCodes.ERR_INVALID_SENDER, RECIPIENT);
                validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_SENDERS), (List<String>) recipient.get(ROLES)),
                        ErrorCodes.ERR_INVALID_SENDER, MessageFormat.format(NOTIFICATION_TRIGGER_ERR_MSG, recipient.get(Constants.PARTICIPANT_CODE), getTopicCode()));
            }
        }
    }
}
