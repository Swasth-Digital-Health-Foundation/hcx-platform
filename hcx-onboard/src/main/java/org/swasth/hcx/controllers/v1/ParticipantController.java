package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.*;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.services.EmailService;
import org.swasth.hcx.services.SMSService;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    //TODO Remove this unnecessary code post moving changes into service layer
    public ResponseEntity<Object> responseHandler(HttpResponse<String> response, String participantCode) throws Exception {
        if (response.getStatus() == HttpStatus.OK.value()) {
            if (response.getBody().isEmpty()) {
                return getSuccessResponse("");
            } else {
                if (response.getBody().startsWith("["))
                    return getSuccessResponse(new ParticipantResponse(JSONUtils.deserialize(response.getBody(), ArrayList.class)));
                else
                    return getSuccessResponse(new ParticipantResponse(participantCode));
            }
        } else if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            throw new ClientException(getErrorMessage(response));
        } else if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            throw new AuthorizationException(getErrorMessage(response));
        } else if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            throw new ResourceNotFoundException(getErrorMessage(response));
        } else {
            throw new ServerException(getErrorMessage(response));
        }
    }

    private ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

    @PostMapping(PARTICIPANT_VERIFY)
    public ResponseEntity<Object> participantVerify(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        String email = "";
        try {
            // verification for provider with jwt token
            if (requestBody.containsKey(JWT_TOKEN)) {
                String jwtToken = (String) requestBody.get(JWT_TOKEN);
                Map<String, Object> payload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
                Map<String,Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
                email = (String) participant.get(PRIMARY_EMAIL);
                onboardParticipant(header, requestBody, (String) payload.get(SPONSOR_CODE), (String) payload.get(APPLICANT_CODE));
            } // verification for provider with payor code
            else if (requestBody.containsKey(PAYOR_CODE)) {
                Map<String,Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
                email = (String) participant.get(PRIMARY_EMAIL);
                onboardParticipant(header, requestBody, (String) requestBody.get(PAYOR_CODE), (String) requestBody.get(PRIMARY_EMAIL));
            } // otp verification
            else if (requestBody.containsKey(EMAIL_OTP)) {
                email = (String) requestBody.get(PRIMARY_EMAIL);
                verifyOTP(requestBody);
            } // verification for all participants
            else {
                Map<String,Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
                email = (String) participant.get(PRIMARY_EMAIL);
                createParticipantAndSendOTP(header, participant, "");
            }
            return getSuccessResponse(new Response());
        } catch (Exception e) {
            emailService.sendMail(email, onboardingFailedSub, onboadingFailedMsg.replace("ERROR_MSG", " " + e.getMessage()));
            return exceptionHandler(new Response(), e);
        }
    }

    private void onboardParticipant(HttpHeaders header, Map<String, Object> participant, String sponsorCode, String applicantCode) throws Exception {
        Map<String,Object> sponsorDetails = getParticipant(PARTICIPANT_CODE, sponsorCode);
        HttpResponse<String> response = HttpUtils.get(sponsorDetails.get(ENDPOINT_URL) + PARTICIPANT_GET_INFO + applicantCode);
        String email = (String) participant.get(PRIMARY_EMAIL);
        // TODO: remove dummy getinfo
        Map<String, Object> resp = participant;
        if(!StringUtils.equalsIgnoreCase((String) participant.get(PARTICIPANT_NAME), (String) resp.get(PARTICIPANT_NAME)) ||
                !StringUtils.equalsIgnoreCase(email, (String) resp.get(PRIMARY_EMAIL))){
            updateIdentityVerificationStatus(email, sponsorCode, REJECTED);
            throw new ClientException("Identity verification failed, participant name or email is not matched with details in sponsor system");
        } else {
            updateIdentityVerificationStatus((String) participant.get(PRIMARY_EMAIL), sponsorCode, ACCEPTED);
            createParticipantAndSendOTP(header, participant, sponsorCode);
        }
    }

    private void updateIdentityVerificationStatus(String applicantEmail, String sponsorCode, String status) throws Exception {
        String query = String.format("INSERT INTO %s (applicant_email,sponsor_code,status,createdOn,updatedOn) VALUES ('%s','%s','%s',%d,%d)",
                applicantEmail, sponsorCode, status, System.currentTimeMillis(), System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    private void createParticipantAndSendOTP(HttpHeaders header, Map<String, Object> participant, String sponsorCode) throws Exception {
        participant.put(ENDPOINT_URL, "http://testurl/v0.7");
        participant.put(ENCRYPTION_CERT, "https://github.com/Swasth-Digital-Health-Foundation/jwe-helper/blob/main/src/test/resources/x509-self-signed-certificate.pem");
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> createResponse = HttpUtils.post(hcxAPIBasePath + PARTICIPANT_CREATE, JSONUtils.serialize(participant), headersMap);
        if (createResponse.getStatus() != 200) {
            throw new ClientException(getErrorMessage(createResponse));
        }
        String participantCode = (String) JSONUtils.deserialize(createResponse.getBody(), Map.class).get(PARTICIPANT_CODE);
        String query = String.format("INSERT INTO %s (participant_code,sponsor_code,primary_email,primary_mobile,email_otp,phone_otp,createdOn," +
                        "updatedOn,expiry,phone_otp_verified,email_otp_verified,status,attempt_count) VALUES ('%s','%s','%s','%s','%s','%s',%d,%d,%d,%b,%b,%b,'%s',%d)", onboardingOtpTable, participantCode, sponsorCode,
                participant.get(PRIMARY_EMAIL), participant.get(PRIMARY_MOBILE), "", "", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), false, false, PENDING, 0);
        postgreSQLClient.execute(query);
        sendOTP(participant);
    }

    @PostMapping(PARTICIPANT_SEND_OTP)
    public ResponseEntity<Object> sendOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            String phoneOtp = String.valueOf(Math.floor(Math.random() * 900000 + 100000));
            smsService.sendOTP("+91" + requestBody.get(PRIMARY_EMAIL), phoneOtp);
            prefillUrl.replace(PRIMARY_EMAIL, (String) requestBody.get(PRIMARY_EMAIL));
            prefillUrl.replace(PRIMARY_MOBILE, (String) requestBody.get(PRIMARY_MOBILE));
            String emailOtp = String.valueOf(Math.floor(Math.random() * 900000 + 100000));
            otpMsg.replace("RANDOM_CODE", emailOtp);
            otpMsg.replace("USER_LINK", prefillUrl);
            emailService.sendMail((String) requestBody.get(PRIMARY_EMAIL), otpSub, otpMsg);
            String query = String.format("UPDATE %s SET phone_otp='%s',email_otp='%s',updatedOn=%d,expiry=%d WHERE primary_email='%s'",
                    onboardingOtpTable, phoneOtp, emailOtp, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry, requestBody.get(PRIMARY_EMAIL));
            postgreSQLClient.execute(query);
            return getSuccessResponse(new Response());
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    public void verifyOTP(@RequestBody Map<String, Object> requestBody) throws Exception {
        ResultSet resultSet = null;
        boolean emailOtpVerified = false;
        boolean phoneOtpVerified = false;
        int attemptCount = 0;
        String email = (String) requestBody.get(PRIMARY_EMAIL);
        try {
            String selectQuery = String.format("SELECT * FROM %s WHERE primary_email='%s'", onboardingOtpTable, requestBody.get(PRIMARY_EMAIL));
            resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
            if (resultSet.next()) {
                attemptCount = resultSet.getInt(ATTEMPT_COUNT);
                if (resultSet.getLong(EXPIRY) > System.currentTimeMillis()) {
                    if(attemptCount >= otpMaxAttempt) {
                        if (resultSet.getString(EMAIL_OTP).equals(requestBody.get(EMAIL_OTP))) emailOtpVerified = true; else throw new ClientException("Email OTP is invalid, please try again!");
                        if (resultSet.getString(PHONE_OTP).equals(requestBody.get(PHONE_OTP))) phoneOtpVerified = true; else throw new ClientException("Phone OTP is invalid, please try again!");
                    } else {
                        throw new ClientException("OTP retry limit has reached, please re-generate otp and try again!");
                    }
                } else {
                    throw new ClientException("OTP has expired, please re-generate otp and try again!");
                }
            } else {
                throw new ClientException("Participant record does not exist");
            }
            otpVerifyMsg.replace("REGISTRY_CODE", (String) requestBody.get(PARTICIPANT_CODE));
            emailService.sendMail(email, otpVerifySub, otpVerifyMsg);
        } catch (Exception e) {
            String updateQuery = String.format("UPDATE %s SET email_otp_verified=%b,phone_otp_verified=%b,updatedOn=%d,attempt_count=%d WHERE primary_email='%s'",
                    onboardingOtpTable, emailOtpVerified, phoneOtpVerified, System.currentTimeMillis(), attemptCount + 1, email);
            postgreSQLClient.execute(updateQuery);
            emailService.sendMail(email, otpFailedSub, otpFailedMsg.replace("ERROR_MSG", " " + e.getMessage()));
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        ResponseEntity<Object> searchResponse = participantSearch(JSONUtils.deserialize("{ \"filters\": { \""+key+"\": { \"eq\": \" " + value + "\" } } }", Map.class));
        ParticipantResponse participantResponse = (ParticipantResponse) Objects.requireNonNull(searchResponse.getBody());
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
    }
    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) {
        try {
            String url = registryUrl + "/api/v1/Organisation/search";
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

}