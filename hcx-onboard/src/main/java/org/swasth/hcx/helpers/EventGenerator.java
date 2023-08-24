package org.swasth.hcx.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.swasth.common.dto.OnboardRequest;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.dto.User;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.UUIDUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;


public class EventGenerator {

    public EventGenerator() {
    }

    public Map<String, Object> getOnboardVerifyEvent(OnboardRequest request, String participantCode) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, PARTICIPANT_VERIFY);
        event.put(APPLICANT_CODE, request.getApplicantCode());
        event.put(VERIFIER_CODE, request.getVerifierCode());
        event.put(TYPE, request.getType());
        event.put(PARTICIPANT_NAME, request.getParticipantName());
        event.put(PRIMARY_EMAIL, request.getPrimaryEmail());
        event.put(PRIMARY_MOBILE, request.getPrimaryMobile());
        event.put(ROLES, request.getRoles());
        event.put(PARTICIPANT_CODE, participantCode);
        event.put(TERMS_AND_CONDITIONS_VERSION, request.getTermsAndConditionsVersion());
        return event;
    }

    public Map<String, Object> getSendLinkEvent(Map<String,Object> request, int regenCount, LocalDate regenDate) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, PARTICIPANT_VERIFICATION_LINK_SEND);
        event.put(PARTICIPANT_NAME, request.getOrDefault(PARTICIPANT_NAME, ""));
        event.put(PRIMARY_EMAIL, request.getOrDefault(PRIMARY_EMAIL, ""));
        event.put(PRIMARY_MOBILE, request.getOrDefault(PRIMARY_MOBILE, ""));
        event.put(PARTICIPANT_CODE, request.getOrDefault(PARTICIPANT_CODE, ""));
        event.put(OTP_REGENERATE_COUNT, regenCount);
        event.put(LAST_REGENERATE_DATE, regenDate.toString());
        return event;
    }

    public Map<String, Object> getVerifyLinkEvent(Map<String,Object> request, int attemptCount, boolean emailOtpVerified, boolean phoneOtpVerified) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, APPLICANT_VERIFY);
        event.put(TYPE, COMMUNICATION_VERIFICATION);
        event.put(PARTICIPANT_CODE, request.getOrDefault(PARTICIPANT_CODE, ""));
        event.put(ATTEMPT_COUNT, attemptCount);
        event.put(EMAIL_VERIFIED, emailOtpVerified);
        event.put(PHONE_VERIFIED, phoneOtpVerified);
        return event;
    }

    public Map<String, Object> getIdentityVerifyEvent(Map<String,Object> request, String result, ResponseError error) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, APPLICANT_VERIFY);
        event.put(TYPE, "identityVerification");
        event.put(APPLICANT_CODE, request.getOrDefault(APPLICANT_CODE, ""));
        event.put(VERIFIER_CODE, request.getOrDefault(VERIFIER_CODE, ""));
        event.put(EMAIL, request.getOrDefault(EMAIL, ""));
        event.put(MOBILE, request.getOrDefault(MOBILE, ""));
        event.put(APPLICANT_NAME, request.getOrDefault(APPLICANT_NAME, ""));
        event.put(ADDITIONALVERIFICATION, request.getOrDefault(ADDITIONALVERIFICATION, new ArrayList<>()));
        event.put(AUDIT_STATUS, result);
        if(error != null){
            event.put("error_details", error);
        }
        return event;
    }

    public Map<String, Object> getApplicantGetInfoEvent(Map<String,Object> request, String applicantCode, String verifiercode, Map<String,Object> response,int status) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, APPLICANT_GET_INFO);
        event.put(APPLICANT_CODE, applicantCode);
        event.put(VERIFIER_CODE, verifiercode);
        event.put(AUDIT_STATUS, status == 200 ? SUCCESSFUL : FAILED);
        event.put(RESPONSE_OBJ, response);
        if (request.containsKey(EMAIL))
            event.put(EMAIL, request.get(EMAIL));
        if (request.containsKey(MOBILE))
            event.put(MOBILE, request.get(MOBILE));
        return event;
    }

    public Map<String, Object> getOnboardUpdateEvent(String email, boolean emailOtpVerified, boolean phoneOtpVerified, String identityStatus) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, PARTICIPANT_ONBOARD_UPDATE);
        event.put(PRIMARY_EMAIL, email);
        event.put(EMAIL_VERIFIED, emailOtpVerified);
        event.put(PHONE_VERIFIED, phoneOtpVerified);
        event.put(IDENTITY_VERIFICATION, identityStatus);
        return event;
    }

    public Map<String, Object> getManualIdentityVerifyEvent(String code, String status) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, PARTICIPANT_VERIFY_IDENTITY);
        event.put(PARTICIPANT_CODE, code);
        event.put(AUDIT_STATUS, status);
        return event;
    }

    public Map<String, Object> getOnboardErrorEvent(String email, String action, ResponseError error) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, action);
        event.put(PRIMARY_EMAIL, email);
        event.put("error_details", error);
        return event;
    }

    public Map<String, Object> getOnboardUserInvite(Map<String,Object> requestBody,String invitedOrgName) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, ONBOARD_USER_INVITE);
        event.put(PARTICIPANT_NAME,invitedOrgName);
        event.put(INVITED_BY,requestBody.getOrDefault(INVITED_BY, ""));
        event.put(EMAIL,requestBody.getOrDefault(EMAIL, ""));
        event.put(ROLE,requestBody.getOrDefault(ROLE, ""));
        event.put(INVITE_STATUS,PENDING);
        return event;
    }

    public Map<String, Object> getOnboardUserInviteAccepted(User user,Map<String,Object> participantDetails) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, ONBOARD_USER_INVITE_ACCEPT);
        event.put(PARTICIPANT_NAME,participantDetails.get(PARTICIPANT_NAME));
        event.put(USER_NAME,user.getUsername());
        event.put(USER_ID,user.getUserId());
        event.put(INVITED_BY,user.getCreatedBy());
        event.put(EMAIL,user.getEmail());
        event.put(TENANT_ROLES,user.getTenantRoles());
        event.put(INVITE_STATUS,ACCEPTED);
        return event;
    }

    public Map<String,Object> getOnboardUserInviteRejected(User user,String participantName){
        Map<String,Object> event = new HashMap<>();
        event.put(EID, ONBOARD);
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(ACTION, ONBOARD_USER_INVITE_REJECT);
        event.put(PARTICIPANT_NAME,participantName);
        event.put(EMAIL,user.getEmail());
        event.put(INVITE_STATUS,REJECTED);
        return  event;
    }

    public String getEmailMessageEvent(String message, String subject, List<String> to, List<String> cc, List<String> bcc) throws JsonProcessingException {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, "MESSAGE");
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(CHANNEL, EMAIL);
        event.put(SUBJECT, subject);
        event.put(MESSAGE, message);
        Map<String,Object> recipients = new HashMap<>();
        recipients.put(TO, to);
        recipients.put(CC, cc);
        recipients.put(BCC, bcc);
        event.put(RECIPIENTS, recipients);
        return JSONUtils.serialize(event);
    }

    public String getSMSMessageEvent(String message, List<String> to) throws JsonProcessingException {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, "MESSAGE");
        event.put(MID, UUIDUtils.getUUID());
        event.put(ETS, System.currentTimeMillis());
        event.put(CHANNEL, SMS);
        event.put(MESSAGE, message);
        Map<String,Object> recipients = new HashMap<>();
        recipients.put(TO, to);
        return  JSONUtils.serialize(event);
    }

}
