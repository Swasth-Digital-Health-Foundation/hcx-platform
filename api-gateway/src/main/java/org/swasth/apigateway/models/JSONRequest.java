package org.swasth.apigateway.models;

import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.swasth.common.utils.Constants.*;

@Data
public class JSONRequest extends BaseRequest {

    public JSONRequest(Map<String, Object> payload, boolean isJSONRequest, String apiAction, String hcxCode, String hcxRoles) throws Exception {
        super(payload, isJSONRequest, apiAction, hcxCode, hcxRoles);
    }

    public void validateRedirect(List<String> allowedRoles, Map<String, Object> redirectDetails, List<Map<String, Object>> callAuditData, List<Map<String, Object>> correlationAuditData) throws Exception {
        validateCondition(StringUtils.isEmpty(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirect requests must have valid participant code for field " + REDIRECT_TO);
        validateCondition(getHcxSenderCode().equalsIgnoreCase(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Sender can not redirect request to self");

        validateParticipant(redirectDetails, ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected", getRedirectTo());
        List<String> redirectRoles = (List<String>) redirectDetails.get(ROLES);
        validateCondition(!hasRole(allowedRoles, redirectRoles), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected participant do not have access to send across callbacks (on_* API calls)");

        validateCondition(!callAuditData.isEmpty(), ErrorCodes.ERR_INVALID_API_CALL_ID, "Already request exists with same api call id:" + getApiCallId());
        validateCondition(correlationAuditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, "The on_action request should contain the same correlation id as in corresponding action request");
        Map<String, Object> auditEvent = (Map<String, Object>) correlationAuditData.get(0);
        if (auditEvent.containsKey(WORKFLOW_ID) && !((String) auditEvent.get(WORKFLOW_ID)).isEmpty()) {
            validateCondition(!getProtocolHeaders().containsKey(WORKFLOW_ID) || !getWorkflowId().equals(auditEvent.get(WORKFLOW_ID)), ErrorCodes.ERR_INVALID_WORKFLOW_ID, "The on_action request should contain the same workflow id as in corresponding action request");
        }

        for (Object audit : correlationAuditData) {
            Map<String, Object> auditEventData = (Map<String, Object>) audit;
            //Check for only on_* requests from Audit data
            if (((String) auditEventData.get(ACTION)).contains("on_")) {
                if (auditEventData.containsKey(STATUS)) {
                    //Identifying any complete responses
                    validateCondition(COMPLETE_STATUS.equals(auditEvent.get(STATUS)), ErrorCodes.ERR_INVALID_REDIRECT_TO, "The redirected request has been closed with status as response.complete");
                }
                //validate the redirected has any one of request initiators
                validateCondition(getRedirectTo().equalsIgnoreCase((String) auditEventData.get(HCX_SENDER_CODE)), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirect request can not be redirected to one of the initiators");
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
    public void validateNotificationReq(Map<String, Object> senderDetails, List<Map<String, Object>> recipientsDetails, List<String> allowedNetworkCodes) throws ClientException {
        validateNotificationParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, SENDER);
        validateCondition(StringUtils.isEmpty(getTopicCode()), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Notification topic code cannot be null, empty and other than 'String'");
        validateCondition(!NotificationUtils.isValidCode(getTopicCode()), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Invalid topic code(" + getTopicCode() + ") is not present in the master list of notifications");
        validateCondition(MapUtils.isEmpty(getNotificationData()), ErrorCodes.ERR_INVALID_NOTIFICATION_DATA, "Notification Data cannot be null, empty and other than 'JSON Object'");
        Map<String, Object> notification = NotificationUtils.getNotification(getTopicCode());
        validateCondition(notification.get("status").equals(Constants.INACTIVE), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Notification status is inactive");
        if (notification.get(Constants.CATEGORY).equals(Constants.NETWORK)) {
            validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_SENDERS), (List<String>) senderDetails.get(ROLES)) || !allowedNetworkCodes.contains(senderDetails.get(Constants.PARTICIPANT_CODE)),
                    ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Participant is not authorized to trigger this notification: " + getTopicCode());
        }
        // validate recipient codes
        if (!getRecipientCodes().isEmpty()) {
            List<String> fetchedCodes = recipientsDetails.stream().map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
            List<String> invalidRecipients = getRecipientCodes().stream().filter(code -> !fetchedCodes.contains(code)).collect(Collectors.toList());
            if (getRecipientCodes().size() == 1 && !invalidRecipients.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Recipients does not exist in the registry: " + invalidRecipients);
            for (Map<String, Object> recipient : recipientsDetails) {
                validateNotificationParticipant(recipient, ErrorCodes.ERR_INVALID_RECIPIENT, Constants.RECIPIENT);
                validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_RECIPIENTS), (List<String>) recipient.get(ROLES)),
                        ErrorCodes.ERR_INVALID_RECIPIENT, recipient.get(Constants.PARTICIPANT_CODE) + " is not a allowed recipient of this notification: " + getTopicCode());
            }
        }
        // validate recipient roles
        if (!getRecipientRoles().isEmpty()) {
            List<String> allowedRecipients = (List<String>) notification.get(Constants.ALLOWED_RECIPIENTS);
            for (String role : getRecipientRoles()) {
                validateCondition(!allowedRecipients.contains(role), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ,
                        "Recipient roles are out of range, allowed recipients for this notification: " + notification.get(Constants.ALLOWED_RECIPIENTS));
            }
        }
    }

    public void validateNotificationParticipant(Map<String, Object> details, ErrorCodes code, String participant) throws ClientException {
        if (details.isEmpty()) {
            throw new ClientException(code, participant + " does not exist in registry");
        } else if (StringUtils.equals((String) details.get(REGISTRY_STATUS), BLOCKED) || StringUtils.equals((String) details.get(REGISTRY_STATUS), INACTIVE)) {
            throw new ClientException(code, participant + "  is blocked or inactive as per the registry");
        }
    }

    public String getTopicCode() {
        return getHeader(Constants.TOPIC_CODE);
    }

    public List<String> getRecipientCodes() {
        return getHeaderList(Constants.RECIPIENT_CODES);
    }

    public List<String> getRecipientRoles() {
        return getHeaderList(Constants.RECIPIENT_ROLES);
    }

    public List<String> getSubscriptions() {
        return getHeaderList(Constants.SUBSCRIPTIONS);
    }

    public Map<String, Object> getNotificationData() {
        return getHeaderMap(Constants.NOTIFICATION_DATA);
    }

    public List<String> getSenderList() {
        return (List<String>) getProtocolHeaders().getOrDefault(SENDER_LIST, new ArrayList<>());
    }

    public void validateSubscriptionRequests(String topicCode, List<Map<String, Object>> senderListDetails, Map<String, Object> recipientDetails, List<String> subscriptionMandatoryHeaders,Map<String, Object> notification) throws ClientException {
        for (String subscriptionMandatoryHeader : subscriptionMandatoryHeaders) {
            validateCondition(!getProtocolHeaders().containsKey(subscriptionMandatoryHeader), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Notification request does not have mandatory headers: " + TOPIC_CODE +" , "+SENDER_LIST);
        }

        // validate recipient details who was intended notification recipient post successful subscription
        validateNotificationParticipant(recipientDetails, ErrorCodes.ERR_INVALID_SENDER, SENDER);
        // topicCode is present in the notifications list
        validateCondition(StringUtils.isEmpty(topicCode), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Notification topic code cannot be null, empty and other than 'String'");
        validateCondition(!NotificationUtils.isValidCode(topicCode), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Invalid topic code(" + topicCode + ") is not present in the master list of notifications");

        validateCondition(notification.get(Constants.AUDIT_STATUS).equals(Constants.INACTIVE), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Notification status is inactive");
        // Whether user has authorised roles as per the provided roles for this topicCode in the notifications list
        validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_RECIPIENTS), (List<String>) recipientDetails.get(ROLES)),
                ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Participant is not allowed to receive this notification: " + topicCode);
        // All participants in the senderList are valid in the registry
        if (!getSenderList().isEmpty()) {
            List<String> fetchedCodes = senderListDetails.stream().map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
            List<String> invalidSenders = getSenderList().stream().filter(code -> !fetchedCodes.contains(code)).collect(Collectors.toList());
            if (!invalidSenders.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Senders does not exist in the registry: " + invalidSenders);
            for (Map<String, Object> recipient : senderListDetails) {
                validateNotificationParticipant(recipient, ErrorCodes.ERR_INVALID_SENDER, RECIPIENT);
                validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_SENDERS), (List<String>) recipient.get(ROLES)),
                        ErrorCodes.ERR_INVALID_SENDER, recipient.get(Constants.PARTICIPANT_CODE) + " is not a allowed to trigger this notification: " + getTopicCode());
            }
        }
    }
}
