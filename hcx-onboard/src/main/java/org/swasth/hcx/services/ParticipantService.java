package org.swasth.hcx.services;

import freemarker.template.TemplateException;
import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.*;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.VerificationException;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.utils.CertificateUtil;
import org.swasth.hcx.utils.SlugUtils;
import org.swasth.postgresql.IDatabaseService;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Service
public class ParticipantService extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${email.linkSub}")
    private String linkSub;

    @Value("${email.verificationSub}")
    private String verificationSub;

    @Value("${phone.sendLinkMsg}")
    private String phoneSub;

    @Value("${phone.verificationMsg}")
    private  String phoneStatus;
    @Value("${hcxURL}")
    private String hcxURL;

    @Value("${apiVersion}")
    private String apiVersion;
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

    @Value("${postgres.table.onboard-verification}")
    private String onboardVerificationTable;

    @Value("${postgres.table.onboard-verifier}")
    private String onboardingVerifierTable;

    @Value("${postgres.mock-service.table.mock-participant}")
    private String mockParticipantsTable;
    @Value("${verificationLink.expiry}")
    private int linkExpiry;

    @Value("${verificationLink.maxAttempt}")
    private int linkMaxAttempt;

    @Value("${verificationLink.maxRegenerate}")
    private int linkMaxRegenerate;

    @Value("${env}")
    private String env;
    @Value("${mock-service.allowedEnv}")
    private ArrayList<String> mockParticipantAllowedEnv;
    @Value("${registry.hcxCode}")
    private String hcxCode;
    @Value("${jwt-token.privateKey}")
    private String privatekey;
    @Value("${jwt-token.expiryTime}")
    private Long expiryTime;

    @Value("${mock-service.provider.endpointURL}")
    private String mockProviderEndpointURL;

    @Value("${mock-service.payor.endpointURL}")
    private String mockPayorEndpointURL;

    @Value("${keycloak.base-url}")
    private String keycloakURL;
    @Value("${keycloak.admin-password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.admin-user}")
    private String keycloakAdminUserName;
    @Value("${keycloak.master-realm}")
    private String keycloakMasterRealm;
    @Value("${keycloak.users-realm}")
    private String keycloackUserRealm;
    @Value("${keycloak.client-id}")
    private String keycloackClientId;
    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Resource(name="postgresClientMockService")
    @Autowired
    private IDatabaseService postgresClientMockService;

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
        updateIdentityStatus(request.getPrimaryEmail(), request.getApplicantCode(), request.getVerifierCode(), PENDING);
        processOnboard(header, request, output);
        return getSuccessResponse(new Response(output));
    }

    private void updateStatus(String email, String status) throws Exception {
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'", onboardingVerifierTable, status, System.currentTimeMillis(), email);
        postgreSQLClient.execute(query);
    }

    private void updateIdentityStatus(String email, String applicantCode, String verifierCode, String status) throws Exception {
        String query = String.format("INSERT INTO %s (applicant_email,applicant_code,verifier_code,status,createdOn,updatedOn) VALUES ('%s','%s','%s','%s',%d,%d) ON CONFLICT (applicant_email) DO NOTHING;",
                onboardingVerifierTable, email, applicantCode, verifierCode, status, System.currentTimeMillis(), System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

    private void processOnboard(HttpHeaders headers, OnboardRequest request, Map<String, Object> output) throws Exception {
        Map<String, Object> participant = request.getParticipant();
        participant.put(ENDPOINT_URL, "http://testurl/v0.7");
        participant.put(ENCRYPTION_CERT, "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-35/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem");
        participant.put(REGISTRY_STATUS, CREATED);
        if (((ArrayList<String>) participant.get(ROLES)).contains(PAYOR))
            participant.put(SCHEME_CODE, "default");
        String identityVerified = PENDING;
        if (ONBOARD_FOR_PROVIDER.contains(request.getType())) {
            String query = String.format("SELECT * FROM %s WHERE applicant_email ILIKE '%s' AND status IN ('%s', '%s')", onboardingVerifierTable, request.getPrimaryEmail(),PENDING,REJECTED);
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
                        "updatedOn,expiry,phone_verified,email_verified,status,attempt_count) VALUES ('%s','%s','%s',%d,%d,%d,%b,%b,'%s',%d)", onboardVerificationTable, participantCode,
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
        if(!requestBody.containsKey(ROLES)){
            requestBody = getParticipant(PARTICIPANT_CODE,(String) requestBody.get(PARTICIPANT_CODE));
        }
        String primaryEmail = (String) requestBody.get(PRIMARY_EMAIL);
        String query = String.format("SELECT regenerate_count, last_regenerate_date, email_verified, phone_verified FROM %s WHERE primary_email='%s'", onboardVerificationTable, primaryEmail);
        ResultSet result = (ResultSet) postgreSQLClient.executeQuery(query);
        if (!result.next()) {
            throw new ClientException(ErrorCodes.ERR_INVALID_REQUEST, INVALID_EMAIL);
        }
        int regenerateCount = result.getInt("regenerate_count");
        LocalDate lastRegenerateDate = result.getObject("last_regenerate_date", LocalDate.class);
        boolean emailVerified = result.getBoolean(EMAIL_VERIFIED);
        boolean phoneVerified = result.getBoolean(PHONE_VERIFIED);
        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(lastRegenerateDate)) {
            regenerateCount = 0;
        }
        if (regenerateCount >= linkMaxRegenerate) {
            throw new ClientException(ErrorCodes.ERR_MAXIMUM_LINK_REGENERATE, MAXIMUM_LINK_REGENERATE);
        }
        String shortUrl = null;
        String longUrl = null;
        if (phoneEnabled && !phoneVerified) {
            RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange('0', 'z').filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build();
            shortUrl = hcxURL+"/api/url/" + randomStringGenerator.generate(10);
            longUrl = generateURL(requestBody,PHONE,(String) requestBody.get(PRIMARY_MOBILE)).toString();
            smsService.sendLink((String) requestBody.get(PRIMARY_MOBILE),phoneSub +"\r\n"+ shortUrl);

        }
        if (emailEnabled && !emailVerified) {
            emailService.sendMail(primaryEmail, linkSub, linkTemplate((String) requestBody.get(PARTICIPANT_NAME), (String) requestBody.get(PARTICIPANT_CODE), generateURL(requestBody, EMAIL, primaryEmail),linkExpiry / 86400000, (ArrayList<String>) requestBody.get(ROLES)));
        }
        regenerateCount++;
        String updateQuery = String.format("UPDATE %s SET updatedOn=%d, expiry=%d, regenerate_count=%d, last_regenerate_date='%s', phone_short_url='%s', phone_long_url='%s' WHERE primary_email='%s'",
                onboardVerificationTable, System.currentTimeMillis(), System.currentTimeMillis() + linkExpiry, regenerateCount, currentDate, shortUrl, longUrl, primaryEmail);
        postgreSQLClient.execute(updateQuery);
        auditIndexer.createDocument(eventGenerator.getSendLinkEvent(requestBody, regenerateCount, currentDate));
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
        String name;
        Map<String, Object> participantDetails;
        try {
            String jwtToken = (String) requestBody.get(JWT_TOKEN);
            Map<String, Object> jwtPayload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
            participantCode = (String) jwtPayload.get(PARTICIPANT_CODE);
            name =  (String) jwtPayload.get(PARTICIPANT_NAME);
            participantDetails = getParticipant(PARTICIPANT_CODE, hcxCode);
            if (!jwtPayload.isEmpty() && !jwtUtils.isValidSignature(jwtToken, (String) participantDetails.get(ENCRYPTION_CERT))) {
                throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
            }
            String selectQuery = String.format("SELECT * FROM %s WHERE participant_code='%s'", onboardVerificationTable, participantCode);
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
                        if (StringUtils.equals((String)requestBody.get("status"), SUCCESSFUL)) {
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
                        } else if (StringUtils.equals((String)requestBody.get("status"),FAILED)) {
                            communicationStatus = FAILED;
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
            updateOtpStatus(emailVerified, phoneVerified, attemptCount, communicationStatus, participantCode, (String) requestBody.getOrDefault(COMMENTS,""));
            auditIndexer.createDocument(eventGenerator.getVerifyLinkEvent(requestBody, attemptCount, emailVerified, phoneVerified));
            logger.info("Communication details verification :: participant_code : {} :: type : {} :: status : {}",participantCode,type,communicationStatus);
            if(StringUtils.equals(type,EMAIL)){
                communicationStatus =  emailVerified ? SUCCESSFUL : FAILED;
                emailService.sendMail((String) jwtPayload.get(SUB) ,verificationSub,verificationStatus(name,communicationStatus));
            }
            if(StringUtils.equals(type,PHONE)) {
                communicationStatus =  phoneVerified ? SUCCESSFUL : FAILED;
                String phoneverification = phoneStatus;
                phoneverification = phoneverification.replace("STATUS",communicationStatus);
                smsService.sendLink((String) jwtPayload.get(SUB),phoneverification + "\r\n" + "Thanks, HCX Team.");
            }
            return communicationStatus;
        } catch (Exception e) {
            updateOtpStatus(emailVerified, phoneVerified, attemptCount, FAILED, participantCode, (String) requestBody.getOrDefault(COMMENTS,""));
            throw new VerificationException(e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }
    private void updateOtpStatus(boolean emailVerified, boolean phoneVerified, int attemptCount, String status, String code,String comments) throws Exception {
        String updateOtpQuery = String.format("UPDATE %s SET email_verified=%b,phone_verified=%b,status='%s',updatedOn=%d,attempt_count=%d ,comments='%s' WHERE participant_code='%s'",
                onboardVerificationTable, emailVerified, phoneVerified, status, System.currentTimeMillis(), attemptCount + 1,comments,code);
        postgreSQLClient.execute(updateOtpQuery);
    }

    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        HttpResponse<String> searchResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_SEARCH, "{ \"filters\": { \"" + key + "\": { \"eq\": \" " + value + "\" } } }", new HashMap<>());
        ParticipantResponse participantResponse = JSONUtils.deserialize(searchResponse.getBody(), ParticipantResponse.class);
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
    }

    public ResponseEntity<Object> onboardUpdate(HttpHeaders headers,Map<String, Object> requestBody) throws Exception {
        logger.info("Onboard update: " + requestBody);
        boolean emailVerified = false;
        boolean phoneVerified = false;
        String commStatus = PENDING;
        String identityStatus = REJECTED;
        Map<String,Object> mockProviderDetails = new HashMap<>();
        Map<String,Object> mockPayorDetails = new HashMap<>();
        String jwtToken = (String) requestBody.get(JWT_TOKEN);
        Map<String, Object> payload = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
        String email = (String) payload.get("email");
        Map<String, Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, "Bearer " + jwtToken);

        String otpQuery = String.format("SELECT * FROM %s WHERE primary_email ILIKE '%s'", onboardVerificationTable, email);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(otpQuery);
        if (resultSet.next()) {
            emailVerified = resultSet.getBoolean(EMAIL_VERIFIED);
            phoneVerified = resultSet.getBoolean(PHONE_VERIFIED);
            commStatus = resultSet.getString("status");
        }

        String onboardingQuery = String.format("SELECT * FROM %s WHERE applicant_email ILIKE '%s'", onboardingVerifierTable, email);
        ResultSet resultSet1 = (ResultSet) postgreSQLClient.executeQuery(onboardingQuery);
        if (resultSet1.next()) {
            identityStatus = resultSet1.getString("status");
        }

        auditIndexer.createDocument(eventGenerator.getOnboardUpdateEvent(email, emailVerified, phoneVerified, identityStatus));
        logger.info("Email verification: {} :: Phone verification: {} :: Identity verification: {}", emailVerified, phoneVerified, identityStatus);

        if (commStatus.equals(SUCCESSFUL) && identityStatus.equals(ACCEPTED)) {
            participant.put(REGISTRY_STATUS, ACTIVE);
        }
        Map<String, Object> participantDetails = getParticipant(PARTICIPANT_CODE, (String) participant.get(PARTICIPANT_CODE));
        HttpResponse<String> httpResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_UPDATE, JSONUtils.serialize(participant), headersMap);

        if (httpResponse.getStatus() == 200) {
            logger.info("Participant details are updated successfully :: participant code : " + participant.get(PARTICIPANT_CODE));
            if (commStatus.equals(SUCCESSFUL) && identityStatus.equals(ACCEPTED)) {
                if (mockParticipantAllowedEnv.contains(env)) {
                    String searchQuery = String.format("SELECT * FROM %s WHERE parent_participant_code = '%s'", mockParticipantsTable, participant.get(PARTICIPANT_CODE));
                    ResultSet result = (ResultSet) postgresClientMockService.executeQuery(searchQuery);
                    if (!result.next()) {
                       mockProviderDetails = createMockParticipant(headers, PROVIDER, participantDetails);
                       mockPayorDetails = createMockParticipant(headers, PAYOR, participantDetails);
                    }
                    if(participantDetails.getOrDefault("status","").equals(CREATED)) {
                        emailService.sendMail(email, onboardingSuccessSub, successTemplate((String) participant.get(PARTICIPANT_NAME),mockProviderDetails,mockPayorDetails));
                    }
                }else if(participantDetails.getOrDefault("status","").equals(CREATED)) {
                    emailService.sendMail(email, onboardingSuccessSub, pocSuccessTemplate((String) participant.get(PARTICIPANT_NAME)));
                }
            }
            Response response = new Response(PARTICIPANT_CODE, participant.get(PARTICIPANT_CODE));
            response.put(IDENTITY_VERIFICATION, identityStatus);
            response.put(COMMUNICATION_VERIFICATION, commStatus);
            if (emailEnabled) response.put(EMAIL_VERIFIED, emailVerified);
            if (phoneEnabled) response.put(PHONE_VERIFIED, phoneVerified);
            return getSuccessResponse(response);
        } else {
            return new ResponseEntity<>(httpResponse.getBody(), HttpStatus.valueOf(httpResponse.getStatus()));
        }
    }


    public ResponseEntity<Object> manualIdentityVerify(Map<String, Object> requestBody) throws Exception {
        String applicantEmail = (String) requestBody.get(PRIMARY_EMAIL);
        String status = (String) requestBody.get(REGISTRY_STATUS);
        if (!ALLOWED_ONBOARD_STATUS.contains(status))
            throw new ClientException(ErrorCodes.ERR_INVALID_ONBOARD_STATUS, "Invalid onboard status, allowed values are: " + ALLOWED_ONBOARD_STATUS);
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'",
                onboardingVerifierTable, status, System.currentTimeMillis(), applicantEmail);
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
        auditIndexer.createDocument(eventGenerator.getApplicantGetInfoEvent(requestBody, applicantCode, verifierCode,JSONUtils.deserialize(response.getBody(),Map.class), response.getStatus()));
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

    public ResponseEntity<Object> applicantSearch(Map<String,Object> requestBody,String fields,HttpHeaders headers) throws Exception {
        HttpResponse<String> response = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_SEARCH, JSONUtils.serialize(requestBody), new HashMap<>());
        Map<String,Object> responseMap = JSONUtils.deserialize(response.getBody(),Map.class);
        ArrayList<Map<String,Object>> participantList = JSONUtils.convert(responseMap.get(PARTICIPANTS),ArrayList.class);
        if (fields != null && fields.toLowerCase().contains(SPONSORS))
            addSponsors(participantList);
        if(fields != null && fields.toLowerCase().contains(COMMUNICATION))
            addCommunicationStatus(participantList);
        if(fields != null && fields.toLowerCase().contains(MOCK_PARTICIPANT))
            getMockParticipant(participantList,headers);
        return new ResponseEntity<>(new ParticipantResponse(participantList), HttpStatus.OK);
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

    private URL generateURL(Map<String,Object> participant,String type,String sub) throws Exception{
        String token = generateToken(sub,type,(String) participant.get(PARTICIPANT_NAME),(String) participant.get(PARTICIPANT_CODE));
        String url = String.format("%s/onboarding/verify?%s=%s&jwt_token=%s",hcxURL,type,sub,token) ;
        return new URL(url);
    }

    private String generateToken(String sub,String typ,String name,String code) throws NoSuchAlgorithmException, InvalidKeySpecException {
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

    private String linkTemplate(String name ,String code,URL signedURL,int day,ArrayList<String> role) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", name);
        model.put("PARTICIPANT_CODE", code);
        model.put("URL",signedURL);
        model.put("role",role.get(0));
        model.put("DAY",day);
        return freemarkerService.renderTemplate("send-link.ftl",model);
    }

    private String successTemplate(String participantName,Map<String,Object> mockProviderDetails,Map<String,Object> mockPayorDetails) throws Exception {
        Map<String,Object> model = new HashMap<>();
        model.put("USER_NAME",participantName);
        model.put("MOCK_PROVIDER_CODE", mockProviderDetails.getOrDefault(PARTICIPANT_CODE,""));
        model.put("MOCK_PROVIDER_USER_NAME",mockProviderDetails.getOrDefault(PRIMARY_EMAIL,""));
        model.put("MOCK_PROVIDER_PASSWORD",mockProviderDetails.getOrDefault(PASSWORD,""));
        model.put("MOCK_PAYOR_CODE",mockPayorDetails.getOrDefault(PARTICIPANT_CODE,""));
        model.put("MOCK_PAYOR_USER_NAME",mockPayorDetails.getOrDefault(PRIMARY_EMAIL,""));
        model.put("MOCK_PAYOR_PASSWORD",mockPayorDetails.getOrDefault(PASSWORD,""));
        return freemarkerService.renderTemplate("onboard-success.ftl",model);
    }

    private String pocSuccessTemplate(String name) throws TemplateException, IOException {
        Map<String,Object> model = new HashMap<>();
        model.put("USER_NAME",name);
        model.put("ONBOARDING_SUCCESS_URL",onboardingSuccessURL);
        return freemarkerService.renderTemplate("onboard-poc-success.ftl",model);
    }
    public String commonTemplate(String templateName) throws Exception {
        return freemarkerService.renderTemplate(templateName,new HashMap<>());
    }

    private String verificationStatus(String name , String status) throws  Exception{
        Map<String,Object>  model = new HashMap<>();
        model.put("USER_NAME",name);
        model.put("STATUS",status);
        return freemarkerService.renderTemplate("verification-status.ftl",model);
    }
    private void addSponsors(List<Map<String, Object>> participantsList) throws Exception {
        String selectQuery = String.format("SELECT * FROM %S WHERE applicant_email IN (%s)", onboardingVerifierTable, getParticipantCodeList(participantsList));
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String, Object> sponsorMap = new HashMap<>();
        while (resultSet.next()) {
            Sponsor sponsorResponse = new Sponsor(resultSet.getString(APPLICANT_EMAIL), resultSet.getString(APPLICANT_CODE), resultSet.getString(VERIFIER_CODE), resultSet.getString(FORMSTATUS), resultSet.getLong("createdon"), resultSet.getLong("updatedon"));
            sponsorMap.put(resultSet.getString(APPLICANT_EMAIL), sponsorResponse);
        }
        filterSponsors(sponsorMap, participantsList);
    }

    private void addCommunicationStatus(List<Map<String, Object>> participantsList) throws Exception {
        String selectQuery = String.format("SELECT * FROM %s WHERE participant_code IN (%s)", onboardVerificationTable, getParticipantCodeList(participantsList));
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String,Object> verificationMap = new HashMap<>();
        while (resultSet.next()) {
            Map<String,Object>  verification = new HashMap<>();
            verification.put("status",resultSet.getString("status"));
            if(emailEnabled) {
                verification.put("emailVerified", resultSet.getBoolean("email_verified"));
            }
            if(phoneEnabled) {
                verification.put("phoneVerified", resultSet.getBoolean("phone_verified"));
            }
            verificationMap.put(resultSet.getString(PARTICIPANT_CODE),verification);
        }
        filterVerification(verificationMap,participantsList);
    }
    
    private String getParticipantWithQuote(String participantList) {
        return "'" + participantList.replace("[", "").replace("]", "").replace(" ", "").replace(",", "','") + "'";
    }
  
  private void filterSponsors(Map<String, Object> sponsorMap, List<Map<String, Object>> participantsList) {
        for (Map<String, Object> responseList : participantsList) {
            String email = (String) responseList.get(PRIMARY_EMAIL);
            if (sponsorMap.containsKey(email)) {
                responseList.put(SPONSORS, Collections.singletonList(sponsorMap.get(email)));
            }
        }
    }
    private void filterVerification(Map<String, Object> verificationMap, List<Map<String, Object>> participantsList) {
        for (Map<String, Object> responseList : participantsList) {
            String code = (String) responseList.get(PARTICIPANT_CODE);
            if (verificationMap.containsKey(code))
                responseList.put(COMMUNICATION, verificationMap.get(code));
        }
    }

    @Async
    private Map<String,Object> createMockParticipant(HttpHeaders headers, String role,Map<String,Object> participantDetails) throws Exception {
        String parentParticipantCode = (String) participantDetails.getOrDefault(PARTICIPANT_CODE,"");
        logger.info("creating Mock participant for :: parent participant code : " + parentParticipantCode + " :: Role: " + role);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        Map<String,Object> mockParticipant = getMockParticipantBody(participantDetails,role,parentParticipantCode);
        String privateKey = (String) mockParticipant.getOrDefault(PRIVATE_KEY,"");
        mockParticipant.remove(PRIVATE_KEY);
        HttpResponse<String> createResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_CREATE, JSONUtils.serialize(mockParticipant), headersMap);
        ParticipantResponse pcptResponse = JSONUtils.deserialize(createResponse.getBody(), ParticipantResponse.class);
        if (createResponse.getStatus() != 200) {
            throw new ClientException(pcptResponse.getError().getCode() == null ? ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS : pcptResponse.getError().getCode(), pcptResponse.getError().getMessage());
        }
        String childParticipantCode = (String) JSONUtils.deserialize(createResponse.getBody(), Map.class).get(PARTICIPANT_CODE);
        return updateMockDetails(mockParticipant,parentParticipantCode,childParticipantCode,privateKey);
    }

    private void getEmailAndName(String role, Map<String, Object> mockParticipant, Map<String, Object> participantDetails, String name) {
        mockParticipant.put(PRIMARY_EMAIL, SlugUtils.makeEmailSlug((String) participantDetails.getOrDefault(PRIMARY_EMAIL, ""), role));
        mockParticipant.put(PARTICIPANT_NAME, participantDetails.getOrDefault(PARTICIPANT_NAME, "") + " " + name);
    }

    private Map<String,Object> getMockParticipantBody(Map<String,Object> participantDetails,String role,String parentParticipantCode) throws Exception {
        Map<String, Object> mockParticipant = new HashMap<>();
        if (role.equalsIgnoreCase(PAYOR)) {
            mockParticipant.put(ROLES, new ArrayList<>(List.of(PAYOR)));
            mockParticipant.put(SCHEME_CODE, "default");
            mockParticipant.put(ENDPOINT_URL,mockPayorEndpointURL);
            getEmailAndName("mock_payor", mockParticipant, participantDetails, "Mock Payor");
        }
        if (role.equalsIgnoreCase(PROVIDER)) {
            mockParticipant.put(ROLES, new ArrayList<>(List.of(PROVIDER)));
            mockParticipant.put(ENDPOINT_URL,mockProviderEndpointURL);
            getEmailAndName("mock_provider", mockParticipant, participantDetails, "Mock Provider");
        }
        Map<String,Object> certificate = CertificateUtil.generateCertificates(parentParticipantCode,hcxURL);
        mockParticipant.put(SIGNING_CERT_PATH, certificate.getOrDefault(PUBLIC_KEY, ""));
        mockParticipant.put(ENCRYPTION_CERT, certificate.getOrDefault(PUBLIC_KEY, ""));
        mockParticipant.put(PRIVATE_KEY,certificate.getOrDefault(PRIVATE_KEY,""));
        mockParticipant.put(REGISTRY_STATUS, ACTIVE);
        return mockParticipant;
    }

    private Map<String,Object> updateMockDetails(Map<String,Object> mockParticipant,String parentParticipantCode,String childParticipantCode,String privateKey) throws Exception {
        String childPrimaryEmail = (String) mockParticipant.get(PRIMARY_EMAIL);
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange('0', 'z').filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build();
        String password = randomStringGenerator.generate(12) + "@";
        String query = String.format("INSERT INTO %s (parent_participant_code,child_participant_code,primary_email,password,private_key) VALUES ('%s','%s','%s','%s','%s');",
                mockParticipantsTable, parentParticipantCode, childParticipantCode, childPrimaryEmail,password, privateKey);
        postgresClientMockService.execute(query);
        Map<String,Object> mockParticipantDetails = new HashMap<>();
        mockParticipantDetails.put(PARTICIPANT_CODE,childParticipantCode);
        mockParticipantDetails.put(PRIMARY_EMAIL,childPrimaryEmail);
        mockParticipantDetails.put(PASSWORD,password);
        setKeycloakPassword(childParticipantCode,password);
        logger.info("created Mock participant for :: parent participant code  : " + parentParticipantCode + " :: child participant code  : " + childParticipantCode);
        return mockParticipantDetails;
    }

    private void setKeycloakPassword(String childParticipantCode, String password) throws ClientException {
        try {
            TimeUnit.SECONDS.sleep(2); // After creating participant, elasticsearch will retrieve data after one second hence added two seconds delay for search API.
            Map<String,Object> participantDetails = getParticipant(PARTICIPANT_CODE,childParticipantCode);
            ArrayList<String> osOwner = (ArrayList<String>) participantDetails.get(OS_OWNER);
            Keycloak keycloak = Keycloak.getInstance(keycloakURL, keycloakMasterRealm,keycloakAdminUserName, keycloakAdminPassword, keycloackClientId);
            RealmResource realmResource = keycloak.realm(keycloackUserRealm);
            UserResource userResource = realmResource.users().get(osOwner.get(0));
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);
            userResource.resetPassword(passwordCred);
            logger.info("The Keycloak password for the userID :" + osOwner.get(0) + " has been successfully updated");
         } catch (Exception e){
           throw new ClientException("Unable to set keycloack password : " + e.getMessage());
        }
    }

    private void getMockParticipant(List<Map<String, Object>> participantsList, HttpHeaders headers) throws Exception {
        validateRole(headers);
        String selectQuery = String.format("SELECT * FROM %s WHERE parent_participant_code IN (%s)", mockParticipantsTable, getParticipantCodeList(participantsList));
        ResultSet resultSet = (ResultSet) postgresClientMockService.executeQuery(selectQuery);
        Map<String, Object> mockDetails = new HashMap<>();
        while (resultSet.next()) {
            String childParticipantCode = resultSet.getString(CHILD_PARTICIPANT_CODE);
            String parentParticipantCode = resultSet.getString(PARENT_PARTICIPANT_CODE);
            Map<String, Object> mockUsers = (Map<String, Object>) mockDetails.getOrDefault(parentParticipantCode, new HashMap<>());
            if (childParticipantCode.contains(MOCK_PROVIDER)) {
                addDetails(resultSet, mockUsers, childParticipantCode, MOCK_PROVIDER);
            } else if (childParticipantCode.contains(MOCK_PAYOR)) {
                addDetails(resultSet, mockUsers, childParticipantCode, MOCK_PAYOR);
            }
            mockDetails.put(parentParticipantCode, mockUsers);
        }
        filterMockParticipants(mockDetails, participantsList);
    }

    private void validateRole(HttpHeaders headers) throws Exception {
        String jwtToken = Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0);
        Map<String, Object> token = JSONUtils.decodeBase64String(jwtToken.split("\\.")[1], Map.class);
        Map<String, Object> realmAccess = (Map<String, Object>) token.get("realm_access");
        ArrayList<String> role = (ArrayList<String>) realmAccess.get(ROLES);
        if (!role.get(0).equalsIgnoreCase(ADMIN_ROLE)) {
            throw new ClientException("Invalid Token,Provide a valid admin token ");
        }
    }

    private void addDetails(ResultSet resultSet, Map<String, Object> mockUsers, String childParticipantCode, String type) throws SQLException {
        Map<String, Object> mockParticipant = new HashMap<>();
        mockParticipant.put(PARTICIPANT_CODE, childParticipantCode);
        mockParticipant.put(PRIMARY_EMAIL, resultSet.getString(PRIMARY_EMAIL));
        mockParticipant.put(PASSWORD, resultSet.getString(PASSWORD));
        mockUsers.put(type, mockParticipant);
    }

    private void filterMockParticipants(Map<String, Object> mockDetails, List<Map<String, Object>> participantsList) {
        for (Map<String, Object> responseList : participantsList) {
            String code = (String) responseList.get(PARTICIPANT_CODE);
            Map<String, Object> mockUsers = (Map<String, Object>) mockDetails.get(code);
            if (mockUsers != null) {
                Map<String, Object> mockProvider = (Map<String, Object>) mockUsers.get(MOCK_PROVIDER);
                Map<String, Object> mockPayor = (Map<String, Object>) mockUsers.get(MOCK_PAYOR);
                responseList.put(MOCK_PROVIDER, mockProvider);
                responseList.put(MOCK_PAYOR, mockPayor);
            }
        }
    }
    private String getParticipantCodeList(List<Map<String, Object>> participantsList){
        String participantCodeList = participantsList.stream().map(participant -> participant.get(PARTICIPANT_CODE)).collect(Collectors.toList()).toString();
        return getParticipantWithQuote(participantCodeList);
    }
}
