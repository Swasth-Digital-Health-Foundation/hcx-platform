package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.OnboardRequest;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.*;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.common.exception.OTPVerificationException;
import org.swasth.hcx.services.EmailService;
import org.swasth.hcx.services.SMSService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;

import static org.swasth.common.response.ResponseMessage.INVALID_PARTICIPANT_CODE;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {


    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${registry.basePath}")
    private String registryUrl;

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

    @Value("${email.prefillUrl}")
    private String prefillUrl;

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

    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> participantVerify(@RequestHeader HttpHeaders header, @RequestBody ArrayList<Map<String,Object>> body) {
        String email = "";
        try {
            OnboardRequest request = new OnboardRequest(body);
            Map<String,Object> requestBody = request.getBody();
            logger.info("Participant verification :: " + requestBody);
            Map<String, Object> participant = (Map<String, Object>) requestBody.getOrDefault(PARTICIPANT, new HashMap<>());
            email = (String) participant.getOrDefault(PRIMARY_EMAIL, "");
            Map<String,Object> output = new HashMap<>();
            if (requestBody.containsKey(JWT_TOKEN)) {
                String jwtToken = (String) requestBody.get(JWT_TOKEN);
                Map<String, Object> payload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
                onboardParticipant(header, participant, (String) payload.get(SPONSOR_CODE), (String) payload.get(APPLICANT_CODE), (String) requestBody.get(JWT_TOKEN), output);
            } else if (requestBody.containsKey(PAYOR_CODE)) {
                onboardParticipant(header, participant, (String) requestBody.get(PAYOR_CODE), "", "", output);
            } else if (requestBody.containsKey(EMAIL_OTP)) {
                email = (String) requestBody.get(PRIMARY_EMAIL);
                verifyOTP(requestBody, output);
            } else {
                createParticipantAndSendOTP(header, participant, "", output);
            }
            return getSuccessResponse(new Response(output));
        } catch (Exception e) {
            e.printStackTrace();
            if (!(e instanceof OTPVerificationException)) {
                onboadingFailedMsg = onboadingFailedMsg.replace("ERROR_MSG", " " + e.getMessage());
                //emailService.sendMail(email, onboardingFailedSub, onboadingFailedMsg);
            }
            return exceptionHandler(new Response(), e);
        }
    }

    private void onboardParticipant(HttpHeaders header, Map<String, Object> participant, String sponsorCode, String applicantCode, String jwtToken, Map<String,Object> output) throws Exception {
        Map<String,Object> sponsorDetails = getParticipant(PARTICIPANT_CODE, sponsorCode);
        if(!jwtUtils.isValidSignature(jwtToken, (String) sponsorDetails.get(SIGNING_CERT_PATH)))
            throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
        // TODO: remove dummy getinfo and implement getinfo as post method
        //HttpResponse<String> response = HttpUtils.get(sponsorDetails.get(ENDPOINT_URL) + PARTICIPANT_GET_INFO + applicantCode);
        String email = (String) participant.get(PRIMARY_EMAIL);
        Map<String, Object> resp = participant;
        if(!StringUtils.equalsIgnoreCase((String) participant.get(PARTICIPANT_NAME), (String) resp.get(PARTICIPANT_NAME)) ||
                !StringUtils.equalsIgnoreCase(email, (String) resp.get(PRIMARY_EMAIL))){
            output.put(IDENTITY_VERIFIED, false);
            updateIdentityVerificationStatus(email, applicantCode, sponsorCode, REJECTED);
            throw new ClientException(ErrorCodes.ERR_INVALID_IDENTITY, "Identity verification failed, participant name or email is not matched with details in sponsor system");
        } else {
            output.put(IDENTITY_VERIFIED, true);
            updateIdentityVerificationStatus(email, applicantCode, sponsorCode, ACCEPTED);
            createParticipantAndSendOTP(header, participant, sponsorCode, output);
        }
    }

    private void updateIdentityVerificationStatus(String applicantEmail, String applicantCode, String sponsorCode, String status) throws Exception {
        String query = String.format("INSERT INTO %s (applicant_email,applicant_code,sponsor_code,status,createdOn,updatedOn) VALUES ('%s','%s','%s','%s',%d,%d)",
                onboardingTable, applicantEmail, applicantCode, sponsorCode, status, System.currentTimeMillis(), System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    private void createParticipantAndSendOTP(HttpHeaders header, Map<String, Object> participant, String sponsorCode, Map<String,Object> output) throws Exception {
        participant.put(ENDPOINT_URL, "http://testurl/v0.7");
        participant.put(ENCRYPTION_CERT, "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> createResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_CREATE, JSONUtils.serialize(participant), headersMap);
        ParticipantResponse pcptResponse = JSONUtils.deserialize(createResponse.getBody(), ParticipantResponse.class);
        if (createResponse.getStatus() != 200) {
            throw new ClientException(pcptResponse.getError().getCode(), pcptResponse.getError().getMessage());
        }
        String participantCode = (String) JSONUtils.deserialize(createResponse.getBody(), Map.class).get(PARTICIPANT_CODE);
        String query = String.format("INSERT INTO %s (participant_code,primary_email,primary_mobile,email_otp,phone_otp,createdOn," +
                        "updatedOn,expiry,phone_otp_verified,email_otp_verified,status,attempt_count) VALUES ('%s','%s','%s','%s','%s',%d,%d,%d,%b,%b,'%s',%d)", onboardingOtpTable, participantCode,
                participant.get(PRIMARY_EMAIL), participant.get(PRIMARY_MOBILE), "", "", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), false, false, PENDING, 0);
        postgreSQLClient.execute(query);
        sendOTP(participant);
        output.put(PARTICIPANT_CODE, participantCode);
        logger.info("OTP has been sent successfully :: participant code : " + participantCode + " :: primary email : " + participant.get(PRIMARY_EMAIL));
    }

    @PostMapping(PARTICIPANT_OTP_SEND)
    public ResponseEntity<Object> sendOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            String phoneOtp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            smsService.sendOTP((String) requestBody.get(PRIMARY_MOBILE), phoneOtp);
            prefillUrl = prefillUrl.replace(PRIMARY_EMAIL, (String) requestBody.get(PRIMARY_EMAIL))
                    .replace(PRIMARY_MOBILE, (String) requestBody.get(PRIMARY_MOBILE));
            String emailOtp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            otpMsg = otpMsg.replace("RANDOM_CODE", emailOtp).replace("USER_LINK", prefillUrl);
            emailService.sendMail((String) requestBody.get(PRIMARY_EMAIL), otpSub, otpMsg);
            String query = String.format("UPDATE %s SET phone_otp='%s',email_otp='%s',updatedOn=%d,expiry=%d WHERE primary_email='%s'",
                    onboardingOtpTable, phoneOtp, emailOtp, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry, requestBody.get(PRIMARY_EMAIL));
            postgreSQLClient.execute(query);
            return getSuccessResponse(new Response());
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    public void verifyOTP(@RequestBody Map<String, Object> requestBody, Map<String,Object> output) throws Exception {
        ResultSet resultSet = null;
        boolean emailOtpVerified = false;
        boolean phoneOtpVerified = false;
        int attemptCount = 0;
        String status = FAILED;
        String email = (String) requestBody.get(PRIMARY_EMAIL);
        String participantCode = "";
        try {
            String selectQuery = String.format("SELECT * FROM %s WHERE primary_email='%s'", onboardingOtpTable, requestBody.get(PRIMARY_EMAIL));
            resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
            if (resultSet.next()) {
                attemptCount = resultSet.getInt(ATTEMPT_COUNT);
                participantCode = resultSet.getString(PARTICIPANT_CODE);
                if(resultSet.getString("status").equals(SUCCESSFUL)) {
                    status = SUCCESSFUL;
                    throw new ClientException(ErrorCodes.ERR_INVALID_OTP, "OTP has already verified.");
                }
                if (resultSet.getLong(EXPIRY) > System.currentTimeMillis()) {
                    if(attemptCount < otpMaxAttempt) {
                        if (resultSet.getString(EMAIL_OTP).equals(requestBody.get(EMAIL_OTP))) emailOtpVerified = true; else throw new ClientException("Email OTP is invalid, please try again!");
                        if (resultSet.getString(PHONE_OTP).equals(requestBody.get(PHONE_OTP))) phoneOtpVerified = true; else throw new ClientException("Phone OTP is invalid, please try again!");
                    } else {
                        throw new ClientException(ErrorCodes.ERR_INVALID_OTP, "OTP retry limit has reached, please re-generate otp and try again!");
                    }
                } else {
                    throw new ClientException(ErrorCodes.ERR_INVALID_OTP, "OTP has expired, please re-generate otp and try again!");
                }
            } else {
                throw new ClientException(ErrorCodes.ERR_INVALID_OTP, "Participant record does not exist");
            }
            updateOtpStatus(true, true, attemptCount, SUCCESSFUL, email);
            emailService.sendMail(email, otpVerifySub, otpVerifyMsg.replace("REGISTRY_CODE", " " + participantCode));
            output.put(EMAIL_OTP_VERIFIED, true);
            output.put(PHONE_OTP_VERIFIED, true);
            logger.info("OTP verification is successful :: primary email : " + email);
        } catch (ClientException e) {
            e.printStackTrace();
            updateOtpStatus(emailOtpVerified, phoneOtpVerified, attemptCount, status, email);
            emailService.sendMail(email, otpFailedSub, otpFailedMsg.replace("ERROR_MSG", " " + e.getMessage()));
            throw new OTPVerificationException(e.getErrCode(), e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private void updateOtpStatus(boolean emailOtpVerified, boolean phoneOtpVerified, int attemptCount, String status, String email) throws Exception {
        String updateQuery = String.format("UPDATE %s SET email_otp_verified=%b,phone_otp_verified=%b,status='%s',updatedOn=%d,attempt_count=%d WHERE primary_email='%s'",
                onboardingOtpTable, emailOtpVerified, phoneOtpVerified, status, System.currentTimeMillis(), attemptCount + 1, email);
        postgreSQLClient.execute(updateQuery);
    }

    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        HttpResponse<String> searchResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_SEARCH, "{ \"filters\": { \"" + key + "\": { \"eq\": \" " + value + "\" } } }", new HashMap<>());
        ParticipantResponse participantResponse = JSONUtils.deserialize(searchResponse.getBody(), ParticipantResponse.class);
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
    }

}
