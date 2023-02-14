package org.swasth.hcx.services;

import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.OnboardRequest;
import org.swasth.common.dto.OnboardResponse;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.OTPVerificationException;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.postgresql.IDatabaseService;

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Service
public class ParticipantService extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${email.otpSub}")
    private String otpSub;

    @Value("${email.otpMsg}")
    private String otpMsg;

    @Value("${email.successIdentitySub}")
    private String successIdentitySub;

    @Value("${email.successIdentityMsg}")
    private String successIdentityMsg;

    @Value("${email.onboardingSuccessSub}")
    private String onboardingSuccessSub;

    @Value("${email.onboardingSuccessMsg}")
    private String onboardingSuccessMsg;

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

    @Value("${otp.maxRegenerate}")
    private int maxRegenerate;

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

    public ResponseEntity<Object> verify(HttpHeaders header, ArrayList<Map<String, Object>> body) throws Exception {
        OnboardRequest request = new OnboardRequest(body);
        logger.info("Participant verification :: " + body);
        Map<String, Object> output = new HashMap<>();
        if (request.getType().equals(ONBOARD_THROUGH_JWT)) {
            updateEmail(request.getPrimaryEmail(), request.getApplicantCode());
        } else if (request.getType().equals(ONBOARD_THROUGH_VERIFIER)) {
            updateEmail(request.getPrimaryEmail(), request.getApplicantCode());
        } else {
            updateIdentityVerificationStatus(request.getPrimaryEmail(), "", "", PENDING);
        }
        createParticipantAndSendOTP(header, request, output);
        return getSuccessResponse(new Response(output));
    }

    private void updateEmail(String email, String applicantCode) throws Exception {
        String query = String.format("UPDATE %s SET applicant_email='%s',updatedOn=%d WHERE applicant_code='%s'", onboardingTable, email, System.currentTimeMillis(), applicantCode);
        postgreSQLClient.execute(query);
    }

    private void updateIdentityVerificationStatus(String email, String applicantCode, String verifierCode, String status) throws Exception {
        String query = String.format("INSERT INTO %s (applicant_email,applicant_code,verifier_code,status,createdOn,updatedOn) VALUES ('%s','%s','%s','%s',%d,%d)",
                onboardingTable, email, applicantCode, verifierCode, status, System.currentTimeMillis(), System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    private void createParticipantAndSendOTP(HttpHeaders headers, OnboardRequest request, Map<String, Object> output) throws Exception {
        Map<String, Object> participant = request.getParticipant();
        participant.put(ENDPOINT_URL, "http://testurl/v0.7");
        participant.put(ENCRYPTION_CERT, "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem");
        participant.put(SIGNING_CERT_PATH, "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem");
        participant.put(REGISTRY_STATUS, CREATED);
        if (((ArrayList<String>) participant.get(ROLES)).contains(PAYOR))
            participant.put(SCHEME_CODE, "default");
        String identityVerified = PENDING;
        if (ONBOARD_FOR_PROVIDER.contains(request.getType())) {
            identityVerified = identityVerify(headers, getApplicantBody(request));
        }
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        HttpResponse<String> createResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_CREATE, JSONUtils.serialize(participant), headersMap);
        ParticipantResponse pcptResponse = JSONUtils.deserialize(createResponse.getBody(), ParticipantResponse.class);
        if (createResponse.getStatus() != 200) {
            throw new ClientException(pcptResponse.getError().getCode() == null ? ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS : pcptResponse.getError().getCode(), pcptResponse.getError().getMessage());
        }
        String participantCode = (String) JSONUtils.deserialize(createResponse.getBody(), Map.class).get(PARTICIPANT_CODE);
        participant.put(PARTICIPANT_CODE, participantCode);
        String query = String.format("INSERT INTO %s (participant_code,primary_email,primary_mobile,email_otp,phone_otp,createdOn," +
                        "updatedOn,expiry,phone_otp_verified,email_otp_verified,status,attempt_count) VALUES ('%s','%s','%s','%s','%s',%d,%d,%d,%b,%b,'%s',%d)", onboardingOtpTable, participantCode,
                participant.get(PRIMARY_EMAIL), participant.get(PRIMARY_MOBILE), "", "", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), false, false, PENDING, 0);
        postgreSQLClient.execute(query);
        sendOTP(participant);
        output.put(PARTICIPANT_CODE, participantCode);
        output.put(IDENTITY_VERIFICATION, identityVerified);
        logger.info("OTP has been sent successfully :: participant code : " + participantCode + " :: primary email : " + participant.get(PRIMARY_EMAIL));
    }

    // TODO: change request body to pojo
    private Map<String,Object> getApplicantBody(OnboardRequest request){
        Map<String,Object> body = new HashMap<>();
        body.put(APPLICANT_CODE, request.getApplicantCode());
        body.put(VERIFIER_CODE, request.getVerifierCode());
        body.put(EMAIL, request.getPrimaryEmail());
        body.put(MOBILE, request.getPrimaryMobile());
        body.put(APPLICANT_NAME, request.getParticipantName());
        body.put(ADDITIONALVERIFICATION, request.getAdditionalVerification());
        body.put(ROLE, PROVIDER);
        return body;
    }

    public ResponseEntity<Object> sendOTP(Map<String, Object> requestBody) throws Exception {
        String primaryEmail = (String) requestBody.get(PRIMARY_EMAIL);
        String query = String.format("SELECT regenerate_count, last_regenerate_date FROM %s WHERE primary_email='%s'", onboardingOtpTable, primaryEmail);
        ResultSet result = (ResultSet) postgreSQLClient.executeQuery(query);
        LocalDate lastRegenerateDate = null;
        int regenerateCount = 0;
        LocalDate currentDate = LocalDate.now();
        if (result.next()) {
            regenerateCount = result.getInt("regenerate_count");
            lastRegenerateDate = result.getObject("last_regenerate_date", LocalDate.class);
        }
        if (!currentDate.equals(lastRegenerateDate)) {
            regenerateCount = 0;
        }
        if (regenerateCount >= maxRegenerate) {
            throw new ClientException(ErrorCodes.ERR_MAXIMUM_OTP_REGENERATE, MAXIMUM_OTP_REGENERATE);
        }
        String phoneOtp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        smsService.sendOTP((String) requestBody.get(PRIMARY_MOBILE), phoneOtp);
        String emailOtp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        sendEmailOTP(primaryEmail, (String) requestBody.get(PARTICIPANT_NAME), (String) requestBody.get(PARTICIPANT_CODE), emailOtp);
        String query1 = String.format("UPDATE %s SET phone_otp='%s',email_otp='%s',updatedOn=%d,expiry=%d ,regenerate_count=%d, last_regenerate_date='%s' WHERE primary_email='%s'",
                onboardingOtpTable, phoneOtp, emailOtp, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry, regenerateCount + 1, currentDate, requestBody.get(PRIMARY_EMAIL));
        postgreSQLClient.execute(query1);
        return getSuccessResponse(new Response());
    }

    private void sendEmailOTP(String email, String participantName, String participantCode, String emailOtp) {
        String emailMsg = otpMsg;
        emailMsg = emailMsg.replace("USER_NAME", StringUtils.capitalize(participantName))
                .replace("PARTICIPANT_CODE", participantCode)
                .replace("RANDOM_CODE", " " + emailOtp);
        emailService.sendMail(email, otpSub, emailMsg);
    }

    public String verifyOTP(Map<String, Object> requestBody) throws Exception {
        String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
        ResultSet resultSet = null;
        boolean emailOtpVerified = false;
        boolean phoneOtpVerified = false;
        int attemptCount = 0;
        String status = FAILED;
        List<Map<String, Object>> otpVerificationList = (List<Map<String, Object>>) requestBody.get(OTPVERIFICATION);
        try {
            String selectQuery = String.format("SELECT * FROM %s WHERE participant_code='%s'", onboardingOtpTable, participantCode);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
            if (resultSet.next()) {
                attemptCount = resultSet.getInt(ATTEMPT_COUNT);
                if (resultSet.getString("status").equals(SUCCESSFUL)) {
                    status = SUCCESSFUL;
                    throw new ClientException(ErrorCodes.ERR_INVALID_OTP, OTP_ALREADY_VERIFIED);
                }
                if (resultSet.getLong(EXPIRY) > System.currentTimeMillis()) {
                    if (attemptCount < otpMaxAttempt) {
                        for (Map<String, Object> otpVerification : otpVerificationList) {
                            if (otpVerification.get(CHANNEL).equals(EMAIL)) {
                                emailOtpVerified = verifyOTP(resultSet, otpVerification, EMAIL_OTP);
                            }
                            if (otpVerification.get(CHANNEL).equals(PHONE)) {
                                phoneOtpVerified = verifyOTP(resultSet, otpVerification, PHONE_OTP);
                            }
                        }
                    } else {
                        throw new ClientException(ErrorCodes.ERR_INVALID_OTP, OTP_RETRY_LIMIT);
                    }
                } else {
                    throw new ClientException(ErrorCodes.ERR_INVALID_OTP, OTP_EXPIRED);
                }
            } else {
                throw new ClientException(ErrorCodes.ERR_INVALID_OTP, OTP_RECORD_NOT_EXIST);
            }
            updateOtpStatus(true, true, attemptCount, SUCCESSFUL, participantCode);
            logger.info("Communication details verification is successful :: participant_code  : " + participantCode);
            return ACCEPTED;
        } catch (Exception e) {
            updateOtpStatus(emailOtpVerified, phoneOtpVerified, attemptCount, status, participantCode);
            throw new OTPVerificationException(e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private void updateOtpStatus(boolean emailOtpVerified, boolean phoneOtpVerified, int attemptCount, String status, String email) throws Exception {
        String updateOtpQuery = String.format("UPDATE %s SET email_otp_verified=%b,phone_otp_verified=%b,status='%s',updatedOn=%d,attempt_count=%d WHERE participant_code='%s'",
                onboardingOtpTable, emailOtpVerified, phoneOtpVerified, status, System.currentTimeMillis(), attemptCount + 1, email);
        postgreSQLClient.execute(updateOtpQuery);
    }

    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        HttpResponse<String> searchResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_SEARCH, "{ \"filters\": { \"" + key + "\": { \"eq\": \" " + value + "\" } } }", new HashMap<>());
        ParticipantResponse participantResponse = JSONUtils.deserialize(searchResponse.getBody(), ParticipantResponse.class);
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
    }

    public ResponseEntity<Object> onboardUpdate(Map<String, Object> requestBody) throws Exception {
        logger.info("Onboard update: " + requestBody);
        boolean emailOtpVerified = false;
        boolean phoneOtpVerified = false;
        String identityStatus = REJECTED;
        String jwtToken = (String) requestBody.get(JWT_TOKEN);
        Map<String, Object> payload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
        Map<String, Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
        String email = (String) payload.get("email");
        participant.put(REGISTRY_STATUS, ACTIVE);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, "Bearer " + jwtToken);

        String otpQuery = String.format("SELECT * FROM %s WHERE primary_email='%s'", onboardingOtpTable, email);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(otpQuery);
        if (resultSet.next()) {
            emailOtpVerified = resultSet.getBoolean(EMAIL_OTP_VERIFIED);
            phoneOtpVerified = resultSet.getBoolean(PHONE_OTP_VERIFIED);
        }

        String onboardingQuery = String.format("SELECT * FROM %s WHERE applicant_email='%s'", onboardingTable, email);
        ResultSet resultSet1 = (ResultSet) postgreSQLClient.executeQuery(onboardingQuery);
        if (resultSet1.next()) {
            identityStatus = resultSet1.getString("status");
        }

        if (emailOtpVerified && phoneOtpVerified && StringUtils.equalsIgnoreCase(identityStatus, ACCEPTED)) {
            HttpResponse<String> response = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_UPDATE, JSONUtils.serialize(participant), headersMap);
            if (response.getStatus() == 200) {
                logger.info("Participant details are updated successfully :: participant code : " + participant.get(PARTICIPANT_CODE));
                emailService.sendMail(email, onboardingSuccessSub, onboardingSuccessMsg.replace("USER_NAME", StringUtils.capitalize((String) participant.get(PARTICIPANT_NAME))));
                return getSuccessResponse(new Response(PARTICIPANT_CODE, participant.get(PARTICIPANT_CODE)));
            } else return responseHandler(response, (String) participant.get(PARTICIPANT_CODE));
        } else {
            logger.info("Participant details are not updated, due to failed identity verification :: participant code : " + participant.get(PARTICIPANT_CODE));
            throw new ClientException(ErrorCodes.ERR_UPDATE_PARTICIPANT_DETAILS, "Identity verification failed");
        }
    }

    public ResponseEntity<Object> identityVerify(Map<String, Object> requestBody) throws Exception {
        String applicantEmail = (String) requestBody.get(PRIMARY_EMAIL);
        String status = (String) requestBody.get(REGISTRY_STATUS);
        if (!ALLOWED_ONBOARD_STATUS.contains(status))
            throw new ClientException(ErrorCodes.ERR_INVALID_ONBOARD_STATUS, "Invalid onboard status, allowed values are: " + ALLOWED_ONBOARD_STATUS);
        //Update status for the user
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'",
                onboardingTable, status, System.currentTimeMillis(), applicantEmail);
        postgreSQLClient.execute(query);
        if (status.equals(ACCEPTED)) {
            emailService.sendMail(applicantEmail, successIdentitySub, successIdentityMsg);
            return getSuccessResponse(new Response());
        } else {
            throw new ClientException(ErrorCodes.ERR_INVALID_IDENTITY, "Identity verification has failed");
        }
    }

    public ResponseEntity<Object> getInfo(HttpHeaders header, Map<String, Object> requestBody) throws Exception {
        try {
            String applicantCode;
            String verifierCode;
            String verificationToken = "";
            Map<String, Object> verifierDetails;
            if (requestBody.containsKey(VERIFICATION_TOKEN)) {
                verificationToken = (String) requestBody.get(VERIFICATION_TOKEN);
                Map<String, Object> jwtPayload = JSONUtils.decodeBase64String(verificationToken.split("\\.")[1], Map.class);
                verifierCode = (String) jwtPayload.get(ISS);
                applicantCode = (String) jwtPayload.get(SUB);
                verifierDetails = getParticipant(PARTICIPANT_CODE, verifierCode);
                if (!verificationToken.isEmpty() && !jwtUtils.isValidSignature(verificationToken, (String) verifierDetails.get(SIGNING_CERT_PATH)))
                    throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
            } else if (requestBody.containsKey(MOBILE)) {
                verifierCode = (String) requestBody.get(VERIFIER_CODE);
                verifierDetails = getParticipant(PARTICIPANT_CODE, verifierCode);
            } else {
                applicantCode = (String) requestBody.get(APPLICANT_CODE);
                verifierCode = (String) requestBody.get(VERIFIER_CODE);
                verifierDetails = getParticipant(PARTICIPANT_CODE, verifierCode);
            }

            HttpResponse<String> response = HttpUtils.post(verifierDetails.get(ENDPOINT_URL) + APPLICANT_GET_INFO, JSONUtils.serialize(requestBody));
            return new ResponseEntity<>(response.getBody(), HttpStatus.valueOf(response.getStatus()));
        } catch (Exception e){
            return exceptionHandler(new Response(), e);
        }
    }

    public ResponseEntity<Object> applicantVerify(HttpHeaders header, Map<String, Object> requestBody) throws Exception {
        try {
            OnboardResponse response = new OnboardResponse((String) requestBody.get(PARTICIPANT_CODE), (String) requestBody.get(VERIFIER_CODE));
            String result;
            if (requestBody.containsKey(OTPVERIFICATION)) {
                result = verifyOTP(requestBody);
            } else {
                result = identityVerify(header, requestBody);
            }
            response.setResult(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private String identityVerify(HttpHeaders header, Map<String, Object> requestBody) throws Exception {
        Map<String, Object> verifierDetails = getParticipant(PARTICIPANT_CODE, (String) requestBody.get(VERIFIER_CODE));
        String result;
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put(APPLICANT_CODE, reqBody.get(APPLICANT_CODE));
        HttpResponse<String> httpResp = HttpUtils.post(verifierDetails.get(ENDPOINT_URL) + APPLICANT_VERIFY, JSONUtils.serialize(reqBody));
        if (httpResp.getStatus() == 200) {
            Map<String,Object> payorResp = JSONUtils.deserialize(httpResp.getBody(), Map.class);
            result = (String) payorResp.get(RESULT);
            updateIdentityVerificationStatus((String) requestBody.get(EMAIL), (String) requestBody.get(APPLICANT_CODE), (String) requestBody.get(VERIFIER_CODE), result);
        } else {
            Response errResp = JSONUtils.deserialize(httpResp.getBody(), Response.class);
            throw new ClientException(errResp.getError().getCode(), errResp.getError().getMessage());
        }

        return result;
    }

    private boolean verifyOTP(ResultSet resultSet, Map<String, Object> otpVerification, String key) throws Exception {
        if (resultSet.getString(key).equals(otpVerification.get(OTP))) {
            return true;
        } else {
            throw new ClientException(StringUtils.capitalize(key.replace("_", " "))  +  " is invalid, please try again!");
        }
    }
}