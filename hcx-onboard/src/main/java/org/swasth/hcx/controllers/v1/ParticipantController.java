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
import org.swasth.hcx.services.ParticipantService;

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

    @Value("${email.failedIdentityMsg}")
    private String failedIdentityMsg;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ParticipantService participantService;

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> participantVerify(@RequestHeader HttpHeaders header, @RequestBody ArrayList<Map<String, Object>> body) {
        try {
            return participantService.verify(header, body);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_OTP_SEND)
    public ResponseEntity<Object> sendOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            return participantService.sendOTP(requestBody);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_ONBOARD_UPDATE)
    public ResponseEntity<Object> onboardUpdate(@RequestBody Map<String, Object> requestBody) throws SQLException {
        try {
            return participantService.onboardUpdate(requestBody);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_VERIFY_IDENTITY)
    public ResponseEntity<Object> participantIdentityVerify(@RequestBody Map<String, Object> requestBody) {
        String applicantEmail = (String) requestBody.get(PRIMARY_EMAIL);
        try {
            return participantService.identityVerify(requestBody);
        } catch (Exception e) {
            emailService.sendMail(applicantEmail, failedIdentitySub, failedIdentityMsg);
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(APPLICANT_VERIFY)
    public ResponseEntity<Object> applicantVerify(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            return participantService.applicantVerify(header,requestBody);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
    @PostMapping(APPLICANT_GET_INFO)
    public ResponseEntity<Object> participantGetInfo(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            return participantService.getInfo(header, requestBody);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}
