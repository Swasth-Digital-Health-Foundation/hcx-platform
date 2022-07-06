package org.swasth.apigateway.models;

import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.swasth.apigateway.constants.Constants.*;

@Data
public class JSONRequest extends BaseRequest{

    public JSONRequest(Map<String, Object> payload,boolean isJSONRequest,String apiAction,String hcxCode, String hcxRoles) throws Exception{
        super(payload,isJSONRequest,apiAction,hcxCode,hcxRoles);
    }

    public void validateRedirect(List<String> allowedRoles,Map<String, Object> redirectDetails,List<Map<String, Object>> callAuditData,List<Map<String, Object>> correlationAuditData) throws Exception {
            validateCondition(StringUtils.isEmpty(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirect requests must have valid participant code for field " + REDIRECT_TO);
            validateCondition(getSenderCode().equalsIgnoreCase(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Sender can not redirect request to self");

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
                    validateCondition(getRedirectTo().equalsIgnoreCase((String) auditEventData.get(SENDER_CODE)), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirect request can not be redirected to one of the initiators");
                }
            }
    }

    private boolean hasRole(List<String> allowedRoles, List<String> participantRoles) {
        boolean hasAccess = false;
        for (String allowRole: allowedRoles){
            if(participantRoles.contains(allowRole)){
                hasAccess = true;
                break;
            }
        }
        return hasAccess;
    }

    /**
     * This method is to validate notification notify request
     */
    public void validateNotificationReq(List<String> headers, Map<String,Object> senderDetails, List<Map<String,Object>> recipientsDetails, List<String> allowedNetworkCodes) throws ClientException {
        validateNotificationParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, SENDER);
        for(String header: getProtocolHeaders().keySet()){
            validateCondition(!headers.contains(header), ErrorCodes.ERR_INVALID_PAYLOAD, "Notification request contains invalid field: " + header);
        }
        validateCondition(StringUtils.isEmpty(getTopicCode()), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Notification topic code cannot be null, empty and other than 'String'");
        validateCondition(!NotificationUtils.isValidCode(getTopicCode()), ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, "Invalid topic code(" + getTopicCode() + ") is not present in the master list of notifications");
        validateCondition(MapUtils.isEmpty(getNotificationData()), ErrorCodes.ERR_INVALID_NOTIFICATION_DATA, "Notification Data cannot be null, empty and other than 'JSON Object'");
        Map<String,Object> notification = NotificationUtils.getNotification(getTopicCode());
        validateCondition(notification.get("status").equals(Constants.IN_ACTIVE), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Notification status is inactive");
        if(notification.get(Constants.CATEGORY).equals(Constants.NETWORK)){
            validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_SENDERS), (List<String>) senderDetails.get(Constants.ROLES)) || !allowedNetworkCodes.contains(senderDetails.get(Constants.PARTICIPANT_CODE)),
                    ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Participant is not authorized to trigger the notification: " + getTopicCode());
        }
        // validate recipient codes
        if(!recipientsDetails.isEmpty()){
            List<String> invalidRecipients = recipientsDetails.stream().filter(obj -> getRecipientCodes().contains((String) obj.get(Constants.PARTICIPANT_CODE)))
                    .map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
            if(!invalidRecipients.isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid recipient codes: " + invalidRecipients);
            for(Map<String,Object> recipient: recipientsDetails){
                validateNotificationParticipant(recipient, ErrorCodes.ERR_INVALID_RECIPIENT, Constants.RECIPIENT);
                validateCondition(!hasRole((List<String>) notification.get(Constants.ALLOWED_RECIPIENTS), (List<String>) recipient.get(Constants.ROLES)),
                        ErrorCodes.ERR_INVALID_RECIPIENT, recipient.get(Constants.PARTICIPANT_CODE) + " is not a allowed recipient of this notification: " + getTopicCode());
            }
        }
        // validate recipient roles
        if(!getRecipientRoles().isEmpty()){
            List<String> allowedRecipients = (List<String>) notification.get(Constants.ALLOWED_RECIPIENTS);
            for (String role : getRecipientRoles()) {
                validateCondition(!allowedRecipients.contains(role), ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, "Invalid recipient roles");
            }
        }
    }

    public void validateNotificationParticipant(Map<String,Object> details, ErrorCodes code, String participant) throws ClientException {
        if(details.isEmpty()){
            throw new ClientException(code, participant + " does not exist in registry");
        } else if(StringUtils.equals((String) details.get(REGISTRY_STATUS), BLOCKED) || StringUtils.equals((String) details.get(REGISTRY_STATUS), INACTIVE)){
            throw new ClientException(code, participant + "  is blocked or inactive as per the registry");
        }
    }

    public String getTopicCode() { return getHeader(Constants.TOPIC_CODE);}

    public List<String> getRecipientCodes() { return getHeaderList(Constants.RECIPIENT_CODES);}

    public List<String> getRecipientRoles() { return getHeaderList(Constants.RECIPIENT_ROLES);}

    public List<String> getSubscriptions() { return getHeaderList(Constants.SUBSCRIPTIONS);}

    public Map<String,Object> getNotificationData() { return getHeaderMap(Constants.NOTIFICATION_DATA);}
}
