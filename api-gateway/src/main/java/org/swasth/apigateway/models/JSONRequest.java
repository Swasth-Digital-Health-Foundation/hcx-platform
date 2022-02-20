package org.swasth.apigateway.models;

import lombok.Data;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.swasth.apigateway.constants.Constants.*;

@Data
public class JSONRequest extends BaseRequest{

    public JSONRequest(RegistryService registryService, AuditService auditService, Map<String, Object> payload,boolean isJSONRequest,String apiAction) throws Exception{
        super(registryService, auditService, payload,isJSONRequest,apiAction);
    }

    public void validateRedirectRequest(List<String> allowedApis,List<String> allowedRoles) throws Exception {
        if (allowedApis.contains(getApiAction())) {
            validateValues(getApiAction(), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Invalid redirect request," + getApiAction() + " is not allowed for redirect", allowedApis, "Allowed APIs are: ");

            validateCondition(getSenderCode().equalsIgnoreCase(getRedirectTo()), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Sender can not redirect request to self");

            Map<String, Object> redirectDetails = getDetails(getRedirectTo());
            validateParticipant(redirectDetails, ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected");
            List<String> redirectRoles = (List<String>) redirectDetails.get(ROLES);
            validateCondition(!hasRedirectRole(allowedRoles, redirectRoles), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirected participant do not have access to send across on_* API calls");

            validateCondition(!getAuditData(Collections.singletonMap(API_CALL_ID, getApiCallId())).isEmpty(), ErrorCodes.ERR_INVALID_API_CALL_ID, "Already request exists with same api call id:" + getApiCallId());
            List<Object> auditData = getAuditData(Collections.singletonMap(CORRELATION_ID, getCorrelationId()));
            validateCondition(auditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, "The on_action request should contain the same correlation id as in corresponding action request");
            //TODO Do we need to check for all audit entries or this is fine
            Map<String, Object> auditEvent = (Map<String, Object>) auditData.get(0);
            if (auditEvent.containsKey(WORKFLOW_ID)) {
                validateCondition(!getWorkflowId().equals(auditEvent.get(WORKFLOW_ID)), ErrorCodes.ERR_INVALID_WORKFLOW_ID, "The on_action request should contain the same workflow id as in corresponding action request");
            }

            for (Object audit : auditData) {
                Map<String, Object> auditEventData = (Map<String, Object>) audit;
                //Check for only on_* requests from Audit data
                if (((String) auditEventData.get(ACTION)).contains("on_")) {
                    if (auditEventData.containsKey(STATUS)) {
                        //Identifying open redirect requests
                        validateCondition(REDIRECT_STATUS.equals(auditEvent.get(STATUS)), ErrorCodes.ERR_INVALID_REDIRECT_TO, "The redirect request has been closed with status as complete");
                    }
                    //Validate
                    validateCondition(getRedirectTo().equalsIgnoreCase((String) auditEventData.get(SENDER_CODE)), ErrorCodes.ERR_INVALID_REDIRECT_TO, "Redirect request can not be redirected to one of the initiators");
                }
            }
        } else {
            throw new ClientException(ErrorCodes.ERR_INVALID_REDIRECT_TO, getApiAction()+" is not configured for redirect calls");
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
