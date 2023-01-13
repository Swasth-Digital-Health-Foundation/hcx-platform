package org.swasth.hcx.controllers.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.OTPVerificationException;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.services.EmailService;
import org.swasth.hcx.services.ParticipantService;
import org.swasth.hcx.services.SMSService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${email.otpSub}")
    private String otpSub;

    @Value("${email.otpMsg}")
    private String otpMsg;

    @Value("${email.otpVerifyMsg}")
    private String otpVerifyMsg;

    @Value("${email.otpVerifySub}")
    private String otpVerifySub;

    @Value("${email.otpFailedMsg}")
    private String otpFailedMsg;

    @Value("${email.otpFailedSub}")
    private String otpFailedSub;

    @Value("${email.onboardingFailedMsg}")
    private String onboadingFailedMsg;

    @Value("${email.onboardingFailedSub}")
    private String onboardingFailedSub;


    @Value("${email.otpFailedWithoutLinkMsg}")
    private String otpFailedWithoutLinkMsg;

    @Value("${email.successIdentitySub}")
    private String successIdentitySub;

    @Value("${email.successIdentityMsg}")
    private String successIdentityMsg;

    @Value("${email.failedIdentitySub}")
    private String failedIdentitySub;

    @Value("${email.failedIdentityMsg}")
    private String failedIdentityMsg;

    @Value("${email.registryUpdateSub}")
    private String registryUpdateSub;

    @Value("${email.registryUpdateMsg}")
    private String registryUpdateMsg;

    @Value("${email.registryUpdateFailedSub}")
    private String registryUpdateFailedSub;

    @Value("${email.registryUpdateFailedMsg}")
    private String registryUpdateFailedMsg;

    @Value("${email.prefillUrl}")
    private String prefillUrl;

    @Value("${email.updateRegistryUrl}")
    private String updateRegistryUrl;

    @Value("${hcx-api.basePath}")
    private String hcxAPIBasePath;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardingOtpTable;

    @Value("${postgres.onboardingTable}")
    private String onboardingTable;

    @Value("${otp.expiry}")
    private int otpExpiry;

    @Value("${otp.maxAttempt}")
    private int otpMaxAttempt;

    @Value("${env}")
    private String env;

    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private ParticipantService participantService;

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> participantVerify(@RequestHeader HttpHeaders header, @RequestBody ArrayList<Map<String, Object>> body) {
        String email = "";
        try {
            return participantService.participantVerify(header, body);
        } catch (Exception e) {
            if (!(e instanceof OTPVerificationException)) {
                String onboardingErrorMsg = onboadingFailedMsg;
                onboardingErrorMsg = onboardingErrorMsg.replace("ERROR_MSG", " " + e.getMessage());
              //  emailService.sendMail(email, onboardingFailedSub, onboardingErrorMsg);
            }
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
        String email = "";
        try {
            return participantService.onboardUpdate(requestBody);
        } catch (Exception e) {
           // emailService.sendMail(email, registryUpdateFailedSub, registryUpdateFailedMsg.replace("ERROR_MSG", e.getMessage().toLowerCase()));
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_VERIFY_IDENTITY)
    public ResponseEntity<Object> participantIdentityVerify(@RequestBody Map<String, Object> requestBody) {
        String applicantEmail = (String) requestBody.get(PRIMARY_EMAIL);
        try {
            return participantService.participantIdentityVerify(requestBody);
        } catch (Exception e) {
           // emailService.sendMail(applicantEmail, failedIdentitySub, failedIdentityMsg);
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_GET_INFO)
    public ResponseEntity<Object> participantGetInfo(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            return participantService.participantGetInfo(header, requestBody);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping(PARTICIPANT_GET_STATE)
    public ParticipantResponse getState(@PathVariable String participantCode) throws Exception {
        if (!participantCode.isEmpty()) {
            return participantService.getParticipantState(participantCode);
        }
        return new ParticipantResponse(new HashMap<>());
    }
}
