package org.swasth.hcx.controllers.v1;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.services.EmailService;
import org.swasth.hcx.services.ParticipantService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    @Value("${email.failedIdentitySub}")
    private String failedIdentitySub;
    @Autowired
    private EmailService emailService;

    @Autowired
    private ParticipantService service;

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> verify(@RequestHeader HttpHeaders header, @RequestBody ArrayList<Map<String, Object>> body) throws Exception {
        try {
            return service.verify(header, body);
        } catch (Exception e) {
            String email = ((Map<String,Object>) body.get(0).getOrDefault(PARTICIPANT, new HashMap<>())).getOrDefault(PRIMARY_EMAIL, "").toString();
            return exceptionHandler(email, PARTICIPANT_VERIFY, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_OTP_SEND)
    public ResponseEntity<Object> sendOTP(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.sendOTP(requestBody);
        } catch (Exception e) {
            String email =  requestBody.getOrDefault(PRIMARY_EMAIL, "").toString();
            return exceptionHandler(email, PARTICIPANT_OTP_SEND, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_ONBOARD_UPDATE)
    public ResponseEntity<Object> onboardUpdate(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.onboardUpdate(requestBody);
        } catch (Exception e) {
            return exceptionHandler(service.getEmail(requestBody.getOrDefault(JWT_TOKEN, "").toString()), PARTICIPANT_ONBOARD_UPDATE, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_VERIFY_IDENTITY)
public ResponseEntity<Object> identityVerify(@RequestBody Map<String, Object> requestBody) throws Exception {
        String applicantEmail = requestBody.getOrDefault(PRIMARY_EMAIL, "").toString();
        try {
            return service.manualIdentityVerify(requestBody);
        } catch (Exception e) {
            emailService.sendMail(applicantEmail,failedIdentitySub,service.commonTemplate("identity-fail.ftl"));
            return exceptionHandler(applicantEmail, PARTICIPANT_VERIFY_IDENTITY, new Response(), e);

        }
    }

    @PostMapping(APPLICANT_VERIFY)
    public ResponseEntity<Object> applicantVerify(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            return service.applicantVerify(requestBody);
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
}