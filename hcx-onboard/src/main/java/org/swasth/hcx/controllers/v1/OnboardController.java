package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.services.EmailService;
import org.swasth.hcx.services.OnboardService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class OnboardController extends BaseController {

    @Value("${email.failed-identity-sub}")
    private String failedIdentitySub;
    @Autowired
    private EmailService emailService;

    @Autowired
    private OnboardService service;

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> verify(@RequestHeader HttpHeaders header, @RequestBody ArrayList<Map<String, Object>> body) throws Exception {
        try {
            return service.verify(header, body);
        } catch (Exception e) {
            String email = ((Map<String,Object>) body.get(0).getOrDefault(PARTICIPANT, new HashMap<>())).getOrDefault(PRIMARY_EMAIL, "").toString();
            return exceptionHandler(email, PARTICIPANT_VERIFY, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_VERIFICATION_LINK_SEND)
    public ResponseEntity<Object> sendVerificationLink(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.sendVerificationLink(requestBody);
        } catch (Exception e) {
            String email =  requestBody.getOrDefault(PRIMARY_EMAIL, "").toString();
            return exceptionHandler(email, PARTICIPANT_VERIFICATION_LINK_SEND, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_ONBOARD_UPDATE)
    public ResponseEntity<Object> onboardUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.onboardUpdate(header, requestBody);
        } catch (Exception e) {
            return exceptionHandler(service.getEmail(requestBody.getOrDefault(JWT_TOKEN, "").toString()), PARTICIPANT_ONBOARD_UPDATE, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_VERIFY_IDENTITY)
    public ResponseEntity<Object> identityVerify(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        String applicantEmail = requestBody.getOrDefault(PRIMARY_EMAIL, "").toString();
        try {
            return service.manualIdentityVerify(headers, requestBody);
        } catch (Exception e) {
            emailService.sendMail(applicantEmail, failedIdentitySub, service.commonTemplate("identity-fail.ftl"));
            return exceptionHandler(applicantEmail, PARTICIPANT_VERIFY_IDENTITY, new Response(), e);

        }
    }

    @PostMapping(APPLICANT_VERIFY)
    public ResponseEntity<Object> applicantVerify(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.applicantVerify(headers, requestBody);
        } catch (Exception e) {
            return exceptionHandler("", APPLICANT_VERIFY, new Response(), e);
        }
    }

    @PostMapping(APPLICANT_GET_INFO)
    public ResponseEntity<Object> getinfo(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.getInfo(requestBody);
        } catch (Exception e) {
            return exceptionHandler("", APPLICANT_GET_INFO, new Response(), e);
        }
    }

    @PostMapping(APPLICANT_SEARCH)
    public ResponseEntity<Object> applicantSearch(@RequestHeader HttpHeaders header, @RequestParam(required = false) String fields, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.applicantSearch(requestBody, fields, header);
        } catch (Exception e) {
            return exceptionHandler("", APPLICANT_SEARCH, new Response(), e);
        }
    }

    @PostMapping(ONBOARD_USER_INVITE)
    public ResponseEntity<Object> userInvite(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return getSuccessResponse(service.userInvite(requestBody,headers));
        } catch (Exception e) {
            return exceptionHandler("", ONBOARD_USER_INVITE, new Response(), e);
        }
    }

    @PostMapping(ONBOARD_USER_INVITE_ACCEPT)
    public ResponseEntity<Object> userInviteAccept(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return getSuccessResponse(service.userInviteAccept(headers, requestBody));
        } catch (Exception e) {
            return exceptionHandler("", ONBOARD_USER_INVITE_ACCEPT, new Response(), e);
        }
    }

    @PostMapping(ONBOARD_USER_INVITE_REJECT)
    public ResponseEntity<Object> userInviteReject(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return getSuccessResponse(service.userInviteReject(requestBody));
        } catch (Exception e) {
            return exceptionHandler("", ONBOARD_USER_INVITE_REJECT, new Response(), e);
        }
    }

    @PostMapping(ONBOARD_APPLICANT_PASSWORD_GENERATE)
    public ResponseEntity<Object> generatePassword(@RequestBody Map<String, Object> requestBody, @RequestHeader HttpHeaders headers) throws Exception {
        try {
            String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
            service.validateAdminRole(headers, participantCode);
            return getSuccessResponse(service.generateAndSetPassword(headers, participantCode));
        } catch (Exception e) {
            return exceptionHandler("", ONBOARD_APPLICANT_PASSWORD_GENERATE, new Response(), e);
        }
    }
}