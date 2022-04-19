package org.swasth.apigateway.models;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.swasth.apigateway.exception.ErrorCodes;

import java.util.List;
import java.util.Map;

import static org.swasth.apigateway.constants.Constants.*;

@Data
public class JSONRequest extends BaseRequest{

    public JSONRequest(Map<String, Object> payload,boolean isJSONRequest,String apiAction) throws Exception{
        super(payload,isJSONRequest,apiAction);
    }

    public void validateRedirect(List<String> allowedRoles,Map<String, Object> redirectDetails,List<Map<String, Object>> callAuditData,List<Map<String, Object>> correlationAuditData) throws Exception {
            validateCondition(StringUtils.isEmpty(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirect requests must have valid participant code for field " + REDIRECT_TO);
            validateCondition(getSenderCode().equalsIgnoreCase(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Sender can not redirect request to self");

            validateParticipant(redirectDetails, ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected", getRedirectTo());
            List<String> redirectRoles = (List<String>) redirectDetails.get(ROLES);
            validateCondition(!hasRedirectRole(allowedRoles, redirectRoles), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected participant do not have access to send across callbacks (on_* API calls)");

            validateCondition(!callAuditData.isEmpty(), ErrorCodes.ERR_INVALID_API_CALL_ID, "Already request exists with same api call id:" + getApiCallId());
            validateCondition(correlationAuditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, "The on_action request should contain the same correlation id as in corresponding action request");
            Map<String, Object> auditEvent = (Map<String, Object>) correlationAuditData.get(0);
            if (auditEvent.containsKey(WORKFLOW_ID)) {
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

    private boolean hasRedirectRole(List<String> allowedRoles, List<String> redirectRoles) {
        boolean hasAccess = false;
        for (String allowRole: allowedRoles){
            if(redirectRoles.contains(allowRole)){
                hasAccess = true;
                break;
            }
        }
        return hasAccess;
    }
}
