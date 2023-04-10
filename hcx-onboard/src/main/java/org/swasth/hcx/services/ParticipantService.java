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
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.OnboardRequest;
import org.swasth.common.dto.OnboardResponse;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.VerificationException;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.postgresql.IDatabaseService;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;
import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Service
public class ParticipantService extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${email.otpSub}")
    private String otpSub;

    @Value("${hcxURL}")
    private String hcxURL;
    @Value("${email.successIdentitySub}")
    private String successIdentitySub;

    @Value("${email.onboardingSuccessSub}")
    private String onboardingSuccessSub;

    @Value("${onboard.verification.email}")
    private Boolean emailEnabled;
    @Value("${onboard.verification.phone}")
    private Boolean phoneEnabled;
    @Value("${onboard.successURL}")
    private String onboardingSuccessURL;

    @Value("${hcx-api.basePath}")
    private String hcxAPIBasePath;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardingOtpTable;

    @Value("${postgres.onboardingTable}")
    private String onboardingTable;

    @Value("${verificationLink.expiry}")
    private int linkExpiry;

    @Value("${verificationLink.maxAttempt}")
    private int linkMaxAttempt;

    @Value("${verificationLink.maxRegenerate}")
    private int linkMaxRegenerate;

    @Value("${env}")
    private String env;
    @Value("${registry.hcxCode}")
    private String hcxCode;
    @Value("${jwt-token.privateKey}")
    private String privatekey;
    @Value("${jwt-token.expiryTime}")
    private Long expiryTime;
    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    protected AuditIndexer auditIndexer;

    @Autowired
    protected EventGenerator eventGenerator;
    @Autowired
    private FreemarkerService freemarkerService;


    public ResponseEntity<Object> verify(HttpHeaders header, ArrayList<Map<String, Object>> body) throws Exception {
        logger.info("Participant verification :: " + body);
        OnboardRequest request = new OnboardRequest(body);
        Map<String, Object> output = new HashMap<>();
        updateIdentityVerificationStatus(request.getPrimaryEmail(), request.getApplicantCode(), request.getVerifierCode(), PENDING);
        createParticipantAndSendOTP(header, request, output);
        return getSuccessResponse(new Response(output));
    }

    private void updateStatus(String email, String status) throws Exception {
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'", onboardingTable, status, System.currentTimeMillis(), email);
        postgreSQLClient.execute(query);
    }

    private void updateIdentityVerificationStatus(String email, String applicantCode, String verifierCode, String status) throws Exception {
        String query = String.format("INSERT INTO %s (applicant_email,applicant_code,verifier_code,status,createdOn,updatedOn) VALUES ('%s','%s','%s','%s',%d,%d) ON CONFLICT (applicant_email) DO NOTHING;",
                onboardingTable, email, applicantCode, verifierCode, status, System.currentTimeMillis(), System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    private void createParticipantAndSendOTP(HttpHeaders headers, OnboardRequest request, Map<String, Object> output) throws Exception {
        Map<String, Object> participant = request.getParticipant();
        participant.put(ENDPOINT_URL, "http://testurl/v0.7");
        participant.put(ENCRYPTION_CERT, "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem");
        participant.put(REGISTRY_STATUS, CREATED);
        if (((ArrayList<String>) participant.get(ROLES)).contains(PAYOR))
            participant.put(SCHEME_CODE, "default");
        String identityVerified = PENDING;
        if (ONBOARD_FOR_PROVIDER.contains(request.getType())) {
            String query = String.format("SELECT * FROM %s WHERE applicant_email ILIKE '%s' AND status IN ('%s', '%s')", onboardingTable, request.getPrimaryEmail(),PENDING,REJECTED);
            ResultSet result = (ResultSet) postgreSQLClient.executeQuery(query);
            if (result.next()) {
                identityVerified = identityVerify(getApplicantBody(request));
                if (StringUtils.equalsIgnoreCase(identityVerified, REJECTED))
                    throw new ClientException("Identity verification is rejected by the payer, Please reach out to them.");
            }
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
        String query = String.format("INSERT INTO %s (participant_code,primary_email,primary_mobile,createdOn," +
                        "updatedOn,expiry,phone_verified,email_verified,status,attempt_count) VALUES ('%s','%s','%s',%d,%d,%d,%b,%b,'%s',%d)", onboardingOtpTable, participantCode,
                participant.get(PRIMARY_EMAIL), participant.get(PRIMARY_MOBILE), System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), false, false, PENDING, 0);
        postgreSQLClient.execute(query);
        sendVerificationLink(participant);
        output.put(PARTICIPANT_CODE, participantCode);
        output.put(IDENTITY_VERIFICATION, identityVerified);
        auditIndexer.createDocument(eventGenerator.getOnboardVerifyEvent(request, participantCode));
        logger.info("Verification link  has been sent successfully :: participant code : " + participantCode + " :: primary email : " + participant.get(PRIMARY_EMAIL));
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

    public ResponseEntity<Object> sendVerificationLink(Map<String, Object> requestBody) throws Exception {
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
        if (regenerateCount >= linkMaxRegenerate) {
            throw new ClientException(ErrorCodes.ERR_MAXIMUM_LINK_REGENERATE, MAXIMUM_LINK_REGENERATE);
        }
        if(phoneEnabled) {
            smsService.sendLink((String) requestBody.get(PRIMARY_MOBILE), generateURL(requestBody,PHONE,(String) requestBody.get(PRIMARY_MOBILE)).toString());
        }
        if(emailEnabled) {
            emailService.sendMail(primaryEmail, otpSub, linkTemplate((String) requestBody.get(PARTICIPANT_NAME), (String) requestBody.get(PARTICIPANT_CODE), generateURL(requestBody,EMAIL,(String) requestBody.get(PRIMARY_EMAIL))));
        }
        regenerateCount++;
        String query1 = String.format("UPDATE %s SET updatedOn=%d,expiry=%d ,regenerate_count=%d, last_regenerate_date='%s' WHERE primary_email='%s'",
                onboardingOtpTable, System.currentTimeMillis(), System.currentTimeMillis() + linkExpiry, regenerateCount, currentDate, requestBody.get(PRIMARY_EMAIL));
        postgreSQLClient.execute(query1);
        auditIndexer.createDocument(eventGenerator.getSendLinkEvent(requestBody, regenerateCount , currentDate));
        return getSuccessResponse(new Response());
    }

    public String communicationVerify(Map<String, Object> requestBody) throws Exception {
        boolean emailVerified = false;
        boolean phoneVerified = false;
        int attemptCount = 0;
        ResultSet resultSet = null;
        String participantCode = null;
        String type;
        String communicationStatus = PENDING;
        Map<String, Object> participantDetails;
        try {
            String jwtToken = (String) requestBody.get(JWT_TOKEN);
            Map<String, Object> jwtPayload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
            participantCode = (String) jwtPayload.get(PARTICIPANT_CODE);
            participantDetails = getParticipant(PARTICIPANT_CODE, hcxCode);
            if (!jwtPayload.isEmpty() && !jwtUtils.isValidSignature(jwtToken, (String) participantDetails.get(ENCRYPTION_CERT))) {
                throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
            }
            String selectQuery = String.format("SELECT * FROM %s WHERE participant_code='%s'", onboardingOtpTable, participantCode);
            resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
            if (resultSet.next()) {
                emailVerified = resultSet.getBoolean(EMAIL_VERIFIED);
                phoneVerified = resultSet.getBoolean(PHONE_VERIFIED);
                attemptCount = resultSet.getInt(ATTEMPT_COUNT);
                if (resultSet.getString("status").equals(SUCCESSFUL)) {
                    throw new ClientException(ErrorCodes.ERR_INVALID_LINK, LINK_VERIFIED);
                }
                if (resultSet.getLong(EXPIRY) > System.currentTimeMillis()) {
                    if (attemptCount < linkMaxAttempt) {
                        type = (String) jwtPayload.get(TYP);
                        if (StringUtils.equals((String)requestBody.get("status"), ACCEPTED)) {
                            if (emailEnabled && phoneEnabled) {
                                if (type.equals(EMAIL)) {
                                    emailVerified = true;
                                } else if (type.equals(PHONE)) {
                                    phoneVerified = true;
                                }
                                if (phoneVerified && emailVerified) {
                                    communicationStatus = SUCCESSFUL;
                                }
                            } else if (emailEnabled) {
                                emailVerified = true;
                                communicationStatus = SUCCESSFUL;
                            } else if (phoneEnabled) {
                                phoneVerified = true;
                                communicationStatus = SUCCESSFUL;
                            }
                        }
                        else if (StringUtils.equals((String)requestBody.get("status"),REJECTED)) {
                            updateOtpStatus(emailVerified, phoneVerified, attemptCount, FAILED, participantCode, (String) requestBody.get(COMMENTS));
                            return REJECTED;
                        }
                    } else {
                        throw new ClientException(ErrorCodes.ERR_INVALID_LINK, LINK_RETRY_LIMIT);
                    }
                } else {
                    throw new ClientException(ErrorCodes.ERR_INVALID_LINK, LINK_EXPIRED);
                }
            } else {
                throw new ClientException(ErrorCodes.ERR_INVALID_LINK, LINK_RECORD_NOT_EXIST);
            }
            updateOtpStatus(emailVerified, phoneVerified, attemptCount, communicationStatus, participantCode, "");
            auditIndexer.createDocument(eventGenerator.getVerifyLinkEvent(requestBody, attemptCount, emailVerified, phoneVerified));
            logger.info("Communication details verification is successful :: participant_code : {} :: type : {}" + participantCode,type);
            return ACCEPTED;
        } catch (Exception e) {
            updateOtpStatus(emailVerified, phoneVerified, attemptCount, FAILED, participantCode, (String) requestBody.get(COMMENTS));
            throw new VerificationException(e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }
    private void updateOtpStatus(boolean emailVerified, boolean phoneVerified, int attemptCount, String status, String code,String comments) throws Exception {
        String updateOtpQuery = String.format("UPDATE %s SET email_verified=%b,phone_verified=%b,status='%s',updatedOn=%d,attempt_count=%d ,comments='%s' WHERE participant_code='%s'",
                onboardingOtpTable, emailVerified, phoneVerified, status, System.currentTimeMillis(), attemptCount + 1,comments,code);
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
        boolean emailVerified = false;
        boolean phoneVerified = false;
        String identityStatus = REJECTED;
        String jwtToken = (String) requestBody.get(JWT_TOKEN);
        Map<String, Object> payload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
        String email = (String) payload.get("email");
        Map<String, Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
        participant.put(REGISTRY_STATUS, ACTIVE);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, "Bearer " + jwtToken);

        String otpQuery = String.format("SELECT * FROM %s WHERE primary_email ILIKE '%s'", onboardingOtpTable, email);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(otpQuery);
        if (resultSet.next()) {
            emailVerified = resultSet.getBoolean(EMAIL_VERIFIED);
            phoneVerified = resultSet.getBoolean(PHONE_VERIFIED);
        }

        String onboardingQuery = String.format("SELECT * FROM %s WHERE applicant_email ILIKE '%s'", onboardingTable, email);
        ResultSet resultSet1 = (ResultSet) postgreSQLClient.executeQuery(onboardingQuery);
        if (resultSet1.next()) {
            identityStatus = resultSet1.getString("status");
        }
        auditIndexer.createDocument(eventGenerator.getOnboardUpdateEvent(email, emailVerified, phoneVerified, identityStatus));
        logger.info("Email verification: {} :: Phone verification: {} :: Identity verification: {}", emailVerified, emailVerified, identityStatus);
        if (emailVerified && phoneVerified && StringUtils.equalsIgnoreCase(identityStatus, ACCEPTED)) {
            HttpResponse<String> response = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_UPDATE, JSONUtils.serialize(participant), headersMap);
            if (response.getStatus() == 200) {
                logger.info("Participant details are updated successfully :: participant code : " + participant.get(PARTICIPANT_CODE));
                emailService.sendMail(email,onboardingSuccessSub,successTemplate((String) requestBody.get(PARTICIPANT_NAME)));
                return getSuccessResponse(new Response(PARTICIPANT_CODE, participant.get(PARTICIPANT_CODE)));
            } else return new ResponseEntity<>(response.getBody(), HttpStatus.valueOf(response.getStatus()));
        } else {
            logger.info("Participant details are not updated, due to failed identity verification :: participant code : " + participant.get(PARTICIPANT_CODE));
            throw new ClientException(ErrorCodes.ERR_UPDATE_PARTICIPANT_DETAILS, "Identity verification failed");
        }
    }

    public ResponseEntity<Object> manualIdentityVerify(Map<String, Object> requestBody) throws Exception {
        String applicantEmail = (String) requestBody.get(PRIMARY_EMAIL);
        String status = (String) requestBody.get(REGISTRY_STATUS);
        if (!ALLOWED_ONBOARD_STATUS.contains(status))
            throw new ClientException(ErrorCodes.ERR_INVALID_ONBOARD_STATUS, "Invalid onboard status, allowed values are: " + ALLOWED_ONBOARD_STATUS);
        //Update status for the user
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'",
                onboardingTable, status, System.currentTimeMillis(), applicantEmail);
        postgreSQLClient.execute(query);
        auditIndexer.createDocument(eventGenerator.getManualIdentityVerifyEvent(applicantEmail, status));
        if (status.equals(ACCEPTED)) {
            emailService.sendMail(applicantEmail,successIdentitySub,commonTemplate("identity-success.ftl"));
            return getSuccessResponse(new Response());
        } else {
            throw new ClientException(ErrorCodes.ERR_INVALID_IDENTITY, "Identity verification has failed");
        }
    }

    public ResponseEntity<Object> getInfo(Map<String, Object> requestBody) throws Exception {
        String verifierCode;
        String applicantCode;
        Map<String, Object> verifierDetails;
        if (requestBody.containsKey(VERIFICATION_TOKEN)) {
            String token = (String) requestBody.get(VERIFICATION_TOKEN);
            Map<String, Object> jwtPayload = JSONUtils.decodeBase64String(token.split("\\.")[1], Map.class);
            verifierCode = (String) jwtPayload.get(ISS);
            applicantCode = (String) jwtPayload.get(SUB);
            verifierDetails = getParticipant(PARTICIPANT_CODE, verifierCode);
            if (!token.isEmpty() && !jwtUtils.isValidSignature(token, (String) verifierDetails.get(SIGNING_CERT_PATH)))
                throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
        } else {
            verifierCode = (String) requestBody.getOrDefault(VERIFIER_CODE, "");
            applicantCode = (String) requestBody.getOrDefault(APPLICANT_CODE, "");
            verifierDetails = getParticipant(PARTICIPANT_CODE, verifierCode);
        }
        HttpResponse<String> response = HttpUtils.post(verifierDetails.get(ENDPOINT_URL) + APPLICANT_GET_INFO, JSONUtils.serialize(requestBody), headers(verifierCode));
        auditIndexer.createDocument(eventGenerator.getApplicantGetInfoEvent(requestBody, applicantCode, verifierCode, response));
        return new ResponseEntity<>(response.getBody(), HttpStatus.valueOf(response.getStatus()));
    }

    public ResponseEntity<Object> applicantVerify(Map<String, Object> requestBody) throws Exception {
        OnboardResponse response = new OnboardResponse((String) requestBody.get(PARTICIPANT_CODE), (String) requestBody.get(VERIFIER_CODE));
        String result;
        if (requestBody.containsKey(JWT_TOKEN)) {
            result = communicationVerify(requestBody);
        } else {
            result = identityVerify(requestBody);
        }
        response.setResult(result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String identityVerify(Map<String, Object> requestBody) throws Exception {
        logger.info("Identity verification :: request: {}", requestBody);
        String verifierCode = (String) requestBody.get(VERIFIER_CODE);
        Map<String, Object> verifierDetails = getParticipant(PARTICIPANT_CODE, verifierCode);
        String result = REJECTED;
        Response response = new Response();
        HttpResponse<String> httpResp = HttpUtils.post(verifierDetails.get(ENDPOINT_URL) + APPLICANT_VERIFY, JSONUtils.serialize(requestBody),headers(verifierCode));
        if (httpResp.getStatus() == 200) {
            Map<String,Object> payorResp = JSONUtils.deserialize(httpResp.getBody(), Map.class);
            result = (String) payorResp.get(RESULT);
            updateStatus((String) requestBody.get(EMAIL), result);
        } else {
            response = JSONUtils.deserialize(httpResp.getBody(), Response.class);
            throw new ClientException(response.getError().getCode(), response.getError().getMessage());
        }
        auditIndexer.createDocument(eventGenerator.getIdentityVerifyEvent(requestBody, result, response.getError()));
        logger.info("Identity verification response from payer system :: status: {} :: response: {}", httpResp.getStatus(), httpResp.getBody());
        return result;
    }

    private Map<String,String> headers(String verifierCode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String,String> headers = new HashMap<>();
        headers.put(AUTHORIZATION,"Bearer "+ jwtUtils.generateAuthToken(privatekey,verifierCode,hcxCode,expiryTime));
        return headers;
    }


    public String getEmail(String jwtToken) {
        try {
            Map<String, Object> payload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
            return (String) payload.getOrDefault("email", "");
        } catch (Exception e) {
            logger.error("Error while parsing JWT token");
            return "";
        }
    }

    public URL generateURL(Map<String,Object> participant,String type,String sub) throws Exception{
        String token = generateToken(sub,type,(String) participant.get(PARTICIPANT_NAME),(String) participant.get(PARTICIPANT_CODE));
        String url = String.format("%s/onboarding/verify?%s=%s&jwt_token=%s",hcxURL,type,sub,token) ;
        return new URL(url);
    }

    public String generateToken(String sub,String typ,String name,String code) throws NoSuchAlgorithmException, InvalidKeySpecException {
        long date = new Date().getTime();
        Map<String, Object> headers = new HashMap<>();
        headers.put(ALG,RS256);
        headers.put(TYPE, JWT);
        Map<String, Object> payload = new HashMap<>();
        payload.put(JTI, UUID.randomUUID());
        payload.put(ISS, hcxCode);
        payload.put(TYP, typ);
        payload.put(PARTICIPANT_NAME,name);
        payload.put(PARTICIPANT_CODE,code);
        payload.put(SUB, sub);
        payload.put(IAT, date);
        payload.put(EXP, new Date(date + expiryTime).getTime());
        return jwtUtils.generateJWS(headers,payload,privatekey);
    }
    public String linkTemplate(String name ,String code,URL signedURL) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", name);
        model.put("PARTICIPANT_CODE", code);
        model.put("URL",signedURL);
        return freemarkerService.renderTemplate("send-link.ftl",model);
    }

    public String successTemplate(String name) throws Exception {
        Map<String,Object> model = new HashMap<>();
        model.put("USER_NAME",name);
        model.put("ONBOARDING_SUCCESS_URL",onboardingSuccessURL);
        return freemarkerService.renderTemplate("onboard-success.ftl",model);

    }

    public String commonTemplate(String templateName) throws Exception {
        return freemarkerService.renderTemplate(templateName,new HashMap<>());
    }
}
