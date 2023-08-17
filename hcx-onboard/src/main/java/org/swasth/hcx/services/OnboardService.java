package org.swasth.hcx.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import freemarker.template.TemplateException;
import kong.unirest.HttpResponse;
import org.apache.commons.lang3.RandomStringUtils;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
public class OnboardService extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Value("${email.send-link-sub}")
    private String linkSub;
    @Value("${email.regenerate-link-sub}")
    private String regenerateLinkSub;
    @Value("${email.verification-sub}")
    private String verificationSub;
    @Value("${phone.verification-msg}")
    private String phoneStatus;
    @Value("${email.password-generate-sub}")
    private String passwordGenerateSub;
    @Value("${hcx-url}")
    private String hcxURL;

    @Value("${api-version}")
    private String apiVersion;
    @Value("${email.success-identity-sub}")
    private String successIdentitySub;

    @Value("${email.onboarding-success-sub}")
    private String onboardingSuccessSub;

    @Value("${onboard.success-url}")
    private String onboardingSuccessURL;

    @Value("${hcx-api.base-path}")
    private String hcxAPIBasePath;

    @Value("${postgres.table.onboard-verification}")
    private String onboardVerificationTable;

    @Value("${postgres.table.onboard-verifier}")
    private String onboardingVerifierTable;

    @Value("${postgres.table.onboard-user-invite}")
    private String onboardUserInviteTable;

    @Value("${postgres.mock-service.table.mock-participant}")
    private String mockParticipantsTable;
    @Value("${verification-link.expiry}")
    private int linkExpiry;

    @Value("${verification-link.max-attempt}")
    private int linkMaxAttempt;

    @Value("${verification-link.max-regenerate}")
    private int linkMaxRegenerate;

    @Value("${env}")
    private String env;
    @Value("${mock-service.allowed-env}")
    private ArrayList<String> mockParticipantAllowedEnv;
    @Value("${registry.hcx-code}")
    private String hcxCode;
    @Value("${jwt-token.private-key}")
    private String privatekey;
    @Value("${jwt-token.expiry-time}")
    private Long expiryTime;

    @Value("${mock-service.provider.endpoint-url}")
    private String mockProviderEndpointURL;

    @Value("${mock-service.payor.endpoint-url}")
    private String mockPayorEndpointURL;

    @Value("${keycloak.base-url}")
    private String keycloakURL;
    @Value("${keycloak.admin-password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.admin-user}")
    private String keycloakAdminUserName;
    @Value("${keycloak.master-realm}")
    private String keycloakMasterRealm;
    @Value("${keycloak.participant-realm}")
    private String keycloackParticipantRealm;
    @Value("${keycloak.client-id}")
    private String keycloackClientId;

    @Value("${endpoint.user-invite}")
    private String userInviteEndpoint;
    @Value("${email.user-invite-sub}")
    private String userInviteSub;
    @Value("${email.user-invite-accept-sub}")
    private String userInviteAcceptSub;
    @Value("${email.user-invite-reject-sub}")
    private String userInviteRejectSub;
    @Value("${onboard.email}")
    private String emailConfig;
    @Value("${onboard.phone}")
    private String phoneConfig;

    @Autowired
    private SMSService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IDatabaseService postgreSQLClient;

    @Resource(name = "postgresClientMockService")
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

    private Keycloak keycloak;

    @PostConstruct()
    public void init(){
        keycloak = Keycloak.getInstance(keycloakURL, keycloakMasterRealm, keycloakAdminUserName, keycloakAdminPassword, keycloackClientId);
    }
    public ResponseEntity<Object> verify(HttpHeaders header, ArrayList<Map<String, Object>> body) throws Exception {
        logger.info("Participant verification :: " + body);
        OnboardRequest request = new OnboardRequest(body);
        Map<String, Object> output = new HashMap<>();
        updateIdentityStatus(request);
        onboardProcess(header, request, output);
        return getSuccessResponse(new Response(output));
    }

    private void onboardProcess(HttpHeaders headers, OnboardRequest request, Map<String, Object> output) throws Exception {
        Map<String, Object> participant = request.getParticipant();
        String identityVerified = verifyParticipantIdentity(request);
        addParticipantDetails(participant, request);
        String participantCode = createEntity(PARTICIPANT_CREATE, JSONUtils.serialize(participant), getHeadersMap(headers), ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, PARTICIPANT_CODE);
        participant.put(PARTICIPANT_CODE, participantCode);
        User user = new User(participant.get(PARTICIPANT_NAME) + " Admin", (String) participant.get(PRIMARY_EMAIL), (String) participant.get(PRIMARY_MOBILE), participantCode);
        user.addTenantRole(participantCode, ADMIN);
        user.addTenantRole(participantCode, CONFIG_MANAGER);
        String userId = createUser(headers, user);
        String query = String.format("INSERT INTO %s (participant_code,primary_email,primary_mobile,createdOn," +
                        "updatedOn,expiry,phone_verified,email_verified,status,attempt_count) VALUES ('%s','%s','%s',%d,%d,%d,%b,%b,'%s',%d)", onboardVerificationTable, participantCode,
                participant.get(PRIMARY_EMAIL), participant.get(PRIMARY_MOBILE), System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), false, false, PENDING, 0);
        postgreSQLClient.execute(query);
        if (ONBOARD_FOR_PROVIDER.contains(request.getType())) {
            Map<String, Object> onboardValidations = getConfig(request.getVerifierCode(), ONBOARD_VALIDATION_PROPERTIES);
            updateConfig(participantCode, onboardValidations);
        }
        participant.put(USER_ID, userId);
        sendVerificationLink(participant);
        updateResponse(output, identityVerified, participantCode, userId);
        auditIndexer.createDocument(eventGenerator.getOnboardVerifyEvent(request, participantCode));
        logger.info("Verification link  has been sent successfully :: participant code : " + participantCode + " :: primary email : " + participant.get(PRIMARY_EMAIL));
    }

    private String verifyParticipantIdentity(OnboardRequest request) throws Exception {
        String identityVerified = PENDING;
        if (ONBOARD_FOR_PROVIDER.contains(request.getType())) {
            String query = String.format("SELECT * FROM %s WHERE applicant_email ILIKE '%s' AND status IN ('%s', '%s')", onboardingVerifierTable, request.getPrimaryEmail(), PENDING, REJECTED);
            ResultSet result = (ResultSet) postgreSQLClient.executeQuery(query);
            if (result.next()) {
                identityVerified = identityVerify(getApplicantBody(request));
                if (StringUtils.equalsIgnoreCase(identityVerified, REJECTED))
                    throw new ClientException("Identity verification is rejected by the payer, Please reach out to them.");
            }
        }
        return identityVerified;
    }

    private String createEntity(String api, String participant, Map<String, String> headers, ErrorCodes errCode, String id) throws Exception {
        HttpResponse<String> createResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + api, participant, headers);
        RegistryResponse response = JSONUtils.deserialize(createResponse.getBody(), RegistryResponse.class);
        logger.debug("Registry response :: status: " + createResponse.getStatus() + " :: body: " + createResponse.getBody());
        if (createResponse.getStatus() != 200) {
            throw new ClientException(response.getError().getCode() == null ? errCode : response.getError().getCode(), response.getError().getMessage());
        }
        return (String) JSONUtils.deserialize(createResponse.getBody(), Map.class).get(id);
    }

    private void addParticipantDetails(Map<String,Object> participant, OnboardRequest request) {
        participant.put(ENDPOINT_URL, "http://testurl/v0.7");
        participant.put(ENCRYPTION_CERT, "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem");
        participant.put(REGISTRY_STATUS, CREATED);
        if (((ArrayList<String>) participant.get(ROLES)).contains(PAYOR))
            participant.put(SCHEME_CODE, "default");
        if (((ArrayList<String>) participant.get(ROLES)).contains(PROVIDER))
            participant.put(APPLICANT_CODE, request.getApplicantCode());
    }

    private String createUser(HttpHeaders headers, User user) throws Exception {
        logger.info("User Request Body: " + JSONUtils.serialize(user));
        String userId = createEntity(USER_CREATE, JSONUtils.serialize(user), getHeadersMap(headers), ErrorCodes.ERR_INVALID_USER_DETAILS, USER_ID);
        logger.info("Create user: " + userId);
        return userId;
    }

    private static void updateResponse(Map<String, Object> output, String identityVerified, String participantCode, String userId) {
        output.put(PARTICIPANT_CODE, participantCode);
        output.put(IDENTITY_VERIFICATION, identityVerified);
        output.put(USER_ID, userId);
    }

    // TODO: change request body to pojo
    private Map<String, Object> getApplicantBody(OnboardRequest request) {
        Map<String, Object> body = new HashMap<>();
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
        if (!requestBody.containsKey(ROLES)) {
            requestBody = getParticipant(PARTICIPANT_CODE, (String) requestBody.get(PARTICIPANT_CODE));
        }
        String query = String.format("SELECT regenerate_count, last_regenerate_date, email_verified, phone_verified FROM %s WHERE primary_email='%s'", onboardVerificationTable, (String) requestBody.get(PRIMARY_EMAIL));
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
        if (!phoneVerified && requestBody.containsKey(PRIMARY_MOBILE)) {
            shortUrl = hcxURL + "/api/url/" + generateRandomPassword(10);
            longUrl = generateURL(requestBody, PHONE, (String) requestBody.get(PRIMARY_MOBILE)).toString();
            String phoneMessage = String.format("Dear %s,\n\nTo verify your mobile number as part of the HCX onboarding process, " + "click on %s and proceed as directed.\n\nLink validity: 7 days.", requestBody.getOrDefault(PARTICIPANT_NAME,"user"), shortUrl);
            smsService.sendLink((String) requestBody.get(PRIMARY_MOBILE), phoneMessage);
        }
        if (!emailVerified && requestBody.containsKey(PRIMARY_EMAIL)) {
            if (regenerateCount > 0) {
                emailService.sendMail((String) requestBody.get(PRIMARY_EMAIL), regenerateLinkSub, regenerateLinkTemplate((String) requestBody.get(PARTICIPANT_NAME), generateURL(requestBody, EMAIL, (String) requestBody.get(PRIMARY_EMAIL)), linkExpiry / 86400000));
            } else {
                emailService.sendMail((String) requestBody.get(PRIMARY_EMAIL), linkSub, linkTemplate((String) requestBody.get(PARTICIPANT_NAME), (String) requestBody.get(PARTICIPANT_CODE), generateURL(requestBody, EMAIL, (String) requestBody.get(PRIMARY_EMAIL)), linkExpiry / 86400000, (ArrayList<String>) requestBody.get(ROLES), (String) requestBody.getOrDefault("user_id", "")));
            }
        }
        regenerateCount++;
        String updateQuery = String.format("UPDATE %s SET updatedOn=%d, expiry=%d, regenerate_count=%d, last_regenerate_date='%s', phone_short_url='%s', phone_long_url='%s' WHERE primary_email='%s'",
                onboardVerificationTable, System.currentTimeMillis(), System.currentTimeMillis() + linkExpiry, regenerateCount, currentDate, shortUrl, longUrl, (String) requestBody.get(PRIMARY_EMAIL));
        postgreSQLClient.execute(updateQuery);
        auditIndexer.createDocument(eventGenerator.getSendLinkEvent(requestBody, regenerateCount, currentDate));
        return getSuccessResponse(new Response());
    }

    public String communicationVerify(HttpHeaders headers, Map<String, Object> requestBody) throws Exception {
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
            OnboardValidations validations = new OnboardValidations(getConfig(participantCode, PARTICIPANT_VALIDATION_PROPERTIES));
            boolean emailEnabled = validations.isEmailEnabled();
            boolean phoneEnabled = validations.isPhoneEnabled();
            name = (String) jwtPayload.get(PARTICIPANT_NAME);
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
                        if (StringUtils.equals((String) requestBody.get("status"), SUCCESSFUL)) {
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
                                if (type.equals(EMAIL)) {
                                    emailVerified = true;
                                    communicationStatus = SUCCESSFUL;
                                } else {
                                    phoneVerified = true;
                                }
                            } else if (phoneEnabled) {
                                if (type.equals(PHONE)) {
                                    phoneVerified = true;
                                    communicationStatus = SUCCESSFUL;
                                } else {
                                    emailVerified = true;
                                }
                            }
                        updateParticipant(participantCode, headers, communicationStatus);
                        } else if (StringUtils.equals((String) requestBody.get("status"), FAILED)) {
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
            updateOtpStatus(emailVerified, phoneVerified, attemptCount, communicationStatus, participantCode, (String) requestBody.getOrDefault(COMMENTS, ""));
            auditIndexer.createDocument(eventGenerator.getVerifyLinkEvent(requestBody, attemptCount, emailVerified, phoneVerified));
            logger.info("Communication details verification :: participant_code : {} :: type : {} :: status : {}", participantCode, type, communicationStatus);
            if (StringUtils.equals(type, EMAIL) && emailEnabled) {
                communicationStatus = emailVerified ? SUCCESSFUL : FAILED;
                emailService.sendMail((String) jwtPayload.get(SUB), verificationSub, verificationStatus(name, communicationStatus));
            }
            if (StringUtils.equals(type, PHONE) && phoneEnabled) {
                communicationStatus = phoneVerified ? SUCCESSFUL : FAILED;
                String phoneverification = phoneStatus;
                phoneverification = phoneverification.replace("STATUS", communicationStatus);
                smsService.sendLink((String) jwtPayload.get(SUB), phoneverification + "\r\n" + "Thanks, HCX Team.");
            }
            return communicationStatus;
        } catch (Exception e) {
            e.printStackTrace();
            updateOtpStatus(emailVerified, phoneVerified, attemptCount, FAILED, participantCode, (String) requestBody.getOrDefault(COMMENTS, ""));
            throw new VerificationException(e.getMessage());
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    private void updateParticipant(String participantCode, HttpHeaders headers, String communicationStatus) throws Exception{
        Map<String,Object> participantDetails = getParticipant(PARTICIPANT_CODE, participantCode);
        String identityStatus = "";
        String onboardingQuery = String.format("SELECT * FROM %s WHERE applicant_email ILIKE '%s'", onboardingVerifierTable, participantDetails.get(PRIMARY_EMAIL));
        ResultSet resultSet1 = (ResultSet) postgreSQLClient.executeQuery(onboardingQuery);
        if (resultSet1.next()) {
            identityStatus = resultSet1.getString("status");
        }
        if (communicationStatus.equals(SUCCESSFUL) && identityStatus.equals(ACCEPTED)) {
            Map<String,Object> requestBody = new HashMap();
            requestBody.put(REGISTRY_STATUS, ACTIVE);
            requestBody.put(PARTICIPANT_CODE, participantDetails.get(PARTICIPANT_CODE));
            HttpResponse<String> httpResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_UPDATE, JSONUtils.serialize(requestBody), getHeadersMap(headers));
            if (httpResponse.getStatus() != 200) {
                throw new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR, "Error while updating participant details: " + httpResponse.getBody());
            }
            if (!StringUtils.equals((String) participantDetails.getOrDefault(REGISTRY_STATUS, ""), ACTIVE) && StringUtils.equals((String) requestBody.getOrDefault(REGISTRY_STATUS, ""), ACTIVE)) {
                generateAndSetPassword((String) participantDetails.get(PARTICIPANT_CODE));
            }
        }
    }

    private void updateOtpStatus(boolean emailVerified, boolean phoneVerified, int attemptCount, String status, String code, String comments) throws Exception {
        String updateOtpQuery = String.format("UPDATE %s SET email_verified=%b,phone_verified=%b,status='%s',updatedOn=%d,attempt_count=%d ,comments='%s' WHERE participant_code='%s'",
                onboardVerificationTable, emailVerified, phoneVerified, status, System.currentTimeMillis(), attemptCount + 1, comments, code);
        postgreSQLClient.execute(updateOtpQuery);
    }

    private Map<String, Object> getParticipant(String key, String value) throws Exception {
        HttpResponse<String> searchResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_SEARCH, "{ \"filters\": { \"" + key + "\": { \"eq\": \" " + value + "\" } } }", new HashMap<>());
        RegistryResponse registryResponse = JSONUtils.deserialize(searchResponse.getBody(), RegistryResponse.class);
        if (registryResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) registryResponse.getParticipants().get(0);
    }

    private Map<String, Object> userSearch(String requestBody, HttpHeaders headers) throws Exception {
        Map<String,String> headersMap = new HashMap<>();
        Token token = new Token(Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0));
        headersMap.put(AUTHORIZATION,"Bearer " + token.getToken());
        HttpResponse<String> searchResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + USER_SEARCH, requestBody, headersMap);
        RegistryResponse registryResponse = JSONUtils.deserialize(searchResponse.getBody(), RegistryResponse.class);
        if (registryResponse.getUsers().isEmpty())
            return new HashMap<>();
        else
            return (Map<String, Object>) registryResponse.getUsers().get(0);
    }

    public ResponseEntity<Object> onboardUpdate(HttpHeaders headers, Map<String, Object> requestBody) throws Exception {
        logger.info("Onboard update: " + requestBody);
        boolean emailVerified = false;
        boolean phoneVerified = false;
        String commStatus = PENDING;
        String identityStatus = REJECTED;
        Map<String, Object> mockProviderDetails = new HashMap<>();
        Map<String, Object> mockPayorDetails = new HashMap<>();
        Map<String, Object> participant = (Map<String, Object>) requestBody.get(PARTICIPANT);
        Map<String,Object> participantDetails = getParticipant(PARTICIPANT_CODE, (String) participant.get(PARTICIPANT_CODE));
        String email = (String) participantDetails.get(PRIMARY_EMAIL);
        OnboardValidations validations = new OnboardValidations(getConfig((String) participant.get(PARTICIPANT_CODE), PARTICIPANT_VALIDATION_PROPERTIES));
        boolean emailEnabled = validations.isEmailEnabled();
        boolean phoneEnabled = validations.isPhoneEnabled();
        List<String> roles = (List<String>) participantDetails.get(ROLES);
        setOnboardValidations(participant, roles);
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
        if (participant.containsKey(ONBOARD_VALIDATION_PROPERTIES)) {
            participant.remove(ONBOARD_VALIDATION_PROPERTIES);
        }
        HttpResponse<String> httpResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_UPDATE, JSONUtils.serialize(participant), getHeadersMap(headers));

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
                    if (participantDetails.getOrDefault("status", "").equals(CREATED)) {
                        emailService.sendMail(email, onboardingSuccessSub, successTemplate((String) participant.get(PARTICIPANT_NAME), mockProviderDetails, mockPayorDetails));
                    }
                } else if (participantDetails.getOrDefault("status", "").equals(CREATED)) {
                    emailService.sendMail(email, onboardingSuccessSub, pocSuccessTemplate((String) participant.get(PARTICIPANT_NAME)));
                }
            }
            if (!StringUtils.equals((String) participantDetails.get(REGISTRY_STATUS), ACTIVE) && StringUtils.equals((String) participant.getOrDefault(REGISTRY_STATUS, ""), ACTIVE)){
                generateAndSetPassword((String) participant.get(PARTICIPANT_CODE));
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

    private void setOnboardValidations(Map<String, Object> participant, List<String> roles) throws Exception {
        if (roles.contains(PAYOR)) {
            if (participant.containsKey(ONBOARD_VALIDATION_PROPERTIES)) {
                Gson gson = new Gson();
                String onboardValidationsJson = gson.toJson(participant.getOrDefault(ONBOARD_VALIDATION_PROPERTIES,new HashMap<>()));
                String updateQuery = String.format("UPDATE %s SET onboard_validation_properties = '%s' WHERE participant_code = '%s'",
                        onboardVerificationTable, onboardValidationsJson, participant.get(PARTICIPANT_CODE));
                postgreSQLClient.execute(updateQuery);
            }
        }
    }

    private Map<String, Object> getConfig(String code, String type) throws Exception {
        Map<String, Object> onboardValidations;
        String selectQuery = String.format("SELECT %s from %s where participant_code = '%s'", type, onboardVerificationTable, code);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        if (resultSet.next()) {
            String jsonData = resultSet.getString(1);
            if (jsonData != null) {
                onboardValidations = JSONUtils.deserialize(jsonData, Map.class);
            } else {
                onboardValidations = getSystemConfig();
            }
        } else {
            onboardValidations = getSystemConfig();
        }
        return onboardValidations;
    }

    private void updateConfig(String participantCode, Map<String, Object> onboardValidations) throws Exception {
        String updateQuery = String.format("UPDATE %s SET participant_validation_properties = '%s' WHERE participant_code='%s'", onboardVerificationTable, convertMapJson(onboardValidations), participantCode);
        postgreSQLClient.execute(updateQuery);
    }

    public ResponseEntity<Object> manualIdentityVerify(HttpHeaders headers, Map<String, Object> requestBody) throws Exception {
        String applicantEmail = (String) requestBody.get(PRIMARY_EMAIL);
        String status = (String) requestBody.get(REGISTRY_STATUS);
        if (!ALLOWED_ONBOARD_STATUS.contains(status))
            throw new ClientException(ErrorCodes.ERR_INVALID_ONBOARD_STATUS, "Invalid onboard status, allowed values are: " + ALLOWED_ONBOARD_STATUS);
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'",
                onboardingVerifierTable, status, System.currentTimeMillis(), applicantEmail);
        postgreSQLClient.execute(query);
        auditIndexer.createDocument(eventGenerator.getManualIdentityVerifyEvent(applicantEmail, status));
        if (status.equals(ACCEPTED)) {
            updateParticipant(headers, applicantEmail, status);
            emailService.sendMail(applicantEmail, successIdentitySub, commonTemplate("identity-success.ftl"));
            return getSuccessResponse(new Response());
        } else {
            throw new ClientException(ErrorCodes.ERR_INVALID_IDENTITY, "Identity verification has failed");
        }
    }

    private void updateParticipant(HttpHeaders headers, String email, String identityStatus) throws Exception{
        Map<String,Object> participantDetails = getParticipant(PRIMARY_EMAIL, email);
        String onboardingQuery = String.format("SELECT * FROM %s WHERE participant_code='%s'", onboardVerificationTable, participantDetails.get(PARTICIPANT_CODE));
        ResultSet resultSet1 = (ResultSet) postgreSQLClient.executeQuery(onboardingQuery);
        String communicationStatus = "";
        if (resultSet1.next()) {
            communicationStatus = resultSet1.getString("status");
        }

        if (communicationStatus.equals(SUCCESSFUL) && identityStatus.equals(ACCEPTED)) {
            Map<String,Object> requestBody = new HashMap();
            requestBody.put(REGISTRY_STATUS, ACTIVE);
            requestBody.put(PARTICIPANT_CODE, (String) participantDetails.get(PARTICIPANT_CODE));

            HttpResponse<String> httpResponse = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_UPDATE, JSONUtils.serialize(requestBody), getHeadersMap(headers));
            if (httpResponse.getStatus() != 200) {
                throw new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR, "Error while updating participant details: " + httpResponse.getBody());
            }
            if (!StringUtils.equals((String) participantDetails.get(REGISTRY_STATUS), ACTIVE) && StringUtils.equals((String) requestBody.getOrDefault(REGISTRY_STATUS, ""), ACTIVE)) {
                generateAndSetPassword((String) participantDetails.get(PARTICIPANT_CODE));
            }
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
        auditIndexer.createDocument(eventGenerator.getApplicantGetInfoEvent(requestBody, applicantCode, verifierCode, JSONUtils.deserialize(response.getBody(), Map.class), response.getStatus()));
        return new ResponseEntity<>(response.getBody(), HttpStatus.valueOf(response.getStatus()));
    }

    public ResponseEntity<Object> applicantVerify(HttpHeaders headers, Map<String, Object> requestBody) throws Exception {
        OnboardResponse response = new OnboardResponse((String) requestBody.get(PARTICIPANT_CODE), (String) requestBody.get(VERIFIER_CODE));
        String result;
        if (requestBody.containsKey(JWT_TOKEN)) {
            result = communicationVerify(headers,requestBody);
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
        HttpResponse<String> httpResp = HttpUtils.post(verifierDetails.get(ENDPOINT_URL) + APPLICANT_VERIFY, JSONUtils.serialize(requestBody), headers(verifierCode));
        if (httpResp.getStatus() == 200) {
            Map<String, Object> payorResp = JSONUtils.deserialize(httpResp.getBody(), Map.class);
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

    public ResponseEntity<Object> applicantSearch(Map<String, Object> requestBody, String fields, HttpHeaders headers) throws Exception {
        HttpResponse<String> response = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_SEARCH, JSONUtils.serialize(requestBody), new HashMap<>());
        Map<String, Object> responseMap = JSONUtils.deserialize(response.getBody(), Map.class);
        ArrayList<Map<String, Object>> participantList = JSONUtils.convert(responseMap.get(PARTICIPANTS), ArrayList.class);
        if (fields != null && fields.toLowerCase().contains(SPONSORS))
            addSponsors(participantList);
        if (fields != null && fields.toLowerCase().contains(COMMUNICATION))
            addCommunicationStatus(participantList);
        if (fields != null && fields.toLowerCase().contains(MOCK_PARTICIPANT))
            getMockParticipant(participantList, headers);
        if (fields != null && fields.toLowerCase().contains(ONBOARD_VALIDATION_PROPERTIES))
            getOnboardValidations(participantList);
        return new ResponseEntity<>(new RegistryResponse(participantList, ORGANISATION), HttpStatus.OK);
    }

    public Response userInvite(Map<String, Object> requestBody, HttpHeaders headers) throws Exception {
        logger.info("User invite: " + requestBody);
        String email = (String) requestBody.getOrDefault(EMAIL, "");
        String role = (String) requestBody.getOrDefault(ROLE, "");
        String code = (String) requestBody.getOrDefault(PARTICIPANT_CODE, "");
        String invitedBy = (String) requestBody.getOrDefault(INVITED_BY, "");
        if (isUserExistsInOrg(email, role, code, headers)) {
            throw new ClientException("User with " + role + " is already exist in organisation");
        }
        String token = generateInviteToken(code, email, role, invitedBy);
        URL url = new URL(String.format("%s%s?jwt_token=%s", hcxURL, userInviteEndpoint, token));
        Map<String, Object> participant = getParticipant(PARTICIPANT_CODE, code);
        // for user
        emailService.sendMail(email, userInviteSub, userInviteUserTemplate(email,(String) participant.getOrDefault(PARTICIPANT_NAME, ""), role, url));
        // for participant
        emailService.sendMail((String) participant.get(PRIMARY_EMAIL), userInviteSub, userInviteParticipantTemplate((String) participant.getOrDefault(PARTICIPANT_NAME, ""), role,email));
        String query = String.format("INSERT INTO %s (participant_code,user_email,invited_by,invite_status,created_on) VALUES ('%s','%s','%s','pending',%d)", onboardUserInviteTable, code, email, invitedBy, System.currentTimeMillis());
        postgreSQLClient.execute(query);
        auditIndexer.createDocument(eventGenerator.getOnboardUserInvite(requestBody,(String) participant.getOrDefault(PARTICIPANT_NAME, "")));
        logger.info("User invitation sent");
        return getSuccessResponse();
    }

    private String generateInviteToken(String code, String userEmail, String userRole, String invitedBy) throws NoSuchAlgorithmException, InvalidKeySpecException {
        long date = new Date().getTime();
        Map<String, Object> headers = new HashMap<>();
        headers.put(ALG, RS256);
        headers.put(TYPE, JWT);
        Map<String, Object> payload = new HashMap<>();
        payload.put(JTI, UUID.randomUUID());
        payload.put(ISS, hcxCode);
        payload.put(TYP, "invite");
        payload.put(PARTICIPANT_CODE, code);
        payload.put(EMAIL, userEmail);
        payload.put(ROLE, userRole);
        payload.put(INVITED_BY, invitedBy);
        payload.put(IAT, date);
        payload.put(EXP, new Date(date + expiryTime).getTime());
        return jwtUtils.generateJWS(headers, payload, privatekey);
    }

    public Response userInviteAccept(HttpHeaders headers, Map<String, Object> body) throws Exception {
        logger.info("User invite accepted: " + body);
        Token token = new Token((String) body.getOrDefault(JWT_TOKEN, ""));
        Map<String, Object> hcxDetails = getParticipant(PARTICIPANT_CODE, hcxCode);
        if (!jwtUtils.isValidSignature(token.getToken(), (String) hcxDetails.get(ENCRYPTION_CERT))) {
            throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
        }
        User user = JSONUtils.deserialize(body.get("user"), User.class);
        boolean isUserExists = isUserExists(user, headers);
        if (isUserExists){
            addUser(headers, getAddUserRequestBody(user.getUserId(), token.getParticipantCode(), token.getRole()));
        } else {
            user.setUserId(createUser(headers, user));
        }
        updateInviteStatus(user.getEmail(), "accepted");
        Map<String,Object> participantDetails = getParticipant(PARTICIPANT_CODE, token.getParticipantCode());
        // user
        emailService.sendMail(user.getEmail(), Collections.singletonList(token.getInvitedBy()), userInviteAcceptSub, userInviteAcceptTemplate(user.getUserId(), (String) participantDetails.get(PARTICIPANT_NAME), user.getUsername(),user.getRole()));
        // participant
        emailService.sendMail((String) participantDetails.get(PRIMARY_EMAIL),Collections.singletonList(token.getInvitedBy()),userInviteAcceptSub,userInviteAcceptParticipantTemplate((String) participantDetails.get(PARTICIPANT_NAME),user.getUsername(),user.getRole()));
        auditIndexer.createDocument(eventGenerator.getOnboardUserInviteAccepted(user,participantDetails));
        return getSuccessResponse();
    }

    private void addUser(HttpHeaders headers, String requestBody) throws Exception {
        HttpResponse<String> response = HttpUtils.post(hcxAPIBasePath + VERSION_PREFIX + PARTICIPANT_USER_ADD, requestBody, getHeadersMap(headers));
        if (response.getStatus() != 200) {
            Response resp = JSONUtils.deserialize(response.getBody(), Response.class);
            throw new ClientException(resp.getError().getCode(), resp.getError().getMessage());
        }
    }

    private Map<String, String> getHeadersMap(HttpHeaders headers) {
        Map<String, String> headersMap = new HashMap<>();
        String token = Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0);
        headersMap.put(AUTHORIZATION, token);
        return headersMap;
    }

    private String getAddUserRequestBody(String userId, String participantCode, String userRole) throws JsonProcessingException {
        Map<String, Object> body = new HashMap<>();
        body.put(PARTICIPANT_CODE, participantCode);
        Map<String, Object> user = new HashMap<>();
        user.put(USER_ID, userId);
        user.put(ROLE, userRole);
        List<Map<String, Object>> users = new ArrayList<>();
        users.add(user);
        body.put(USERS, users);
        return JSONUtils.serialize(body);
    }

    private boolean isUserExistsInOrg(String userEmail, String userRole, String participantCode, HttpHeaders headers) throws Exception {
        String body = "{ \"filters\": { \"email\": { \"eq\": \"" + userEmail + "\" }, \"tenant_roles.participant_code\": { \"eq\": \"" + participantCode + "\" }, \"tenant_roles.role\": { \"eq\": \"" + userRole + "\" } } }";
        return !userSearch(body, headers).isEmpty();
    }

    private boolean isUserExists(User user, HttpHeaders headers) throws Exception {
        String body = "{ \"filters\": { \"email\": { \"eq\": \"" + user.getEmail() + "\" } } }";
        Map<String, Object> userDetails = userSearch(body, headers);
        if (userDetails.isEmpty()) {
            return false;
        } else {
            user.setUserId((String) userDetails.get(USER_ID));
            return true;
        }
    }

    private void updateInviteStatus(String email, String status) throws Exception {
        String query = String.format("UPDATE %s SET invite_status='%s',updated_on=%d WHERE user_email='%s'", onboardUserInviteTable, status, System.currentTimeMillis(), email);
        postgreSQLClient.execute(query);
    }

    public Response userInviteReject(Map<String, Object> body) throws Exception {
        logger.info("User invite rejected: " + body);
        Token token = new Token((String) body.getOrDefault(JWT_TOKEN, ""));
        Map<String, Object> hcxDetails = getParticipant(PARTICIPANT_CODE, hcxCode);
        if (!jwtUtils.isValidSignature(token.getToken(), (String) hcxDetails.get(ENCRYPTION_CERT))) {
            throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Invalid JWT token signature");
        }
        User user = JSONUtils.deserialize(body.get("user"), User.class);
        updateInviteStatus(user.getEmail(), "rejected");
        Map<String,Object> participantDetails = getParticipant(PARTICIPANT_CODE, token.getParticipantCode());
        emailService.sendMail((String) participantDetails.get(PRIMARY_EMAIL), Collections.singletonList(token.getInvitedBy()), userInviteRejectSub, userInviteRejectTemplate(user.getEmail(), (String) participantDetails.get(PARTICIPANT_NAME)));
        auditIndexer.createDocument(eventGenerator.getOnboardUserInviteRejected(user, (String) hcxDetails.getOrDefault(PARTICIPANT_NAME,"")));
        return getSuccessResponse();
    }


    private Map<String, String> headers(String verifierCode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + jwtUtils.generateAuthToken(privatekey, hcxCode, expiryTime));
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

    private URL generateURL(Map<String, Object> participant, String type, String sub) throws Exception {
        String token = generateToken(sub, type, (String) participant.get(PARTICIPANT_NAME), (String) participant.get(PARTICIPANT_CODE));
        String url = String.format("%s/onboarding/verify?%s=%s&jwt_token=%s", hcxURL, type, sub, token);
        return new URL(url);
    }

    private String generateToken(String sub, String typ, String name, String code) throws NoSuchAlgorithmException, InvalidKeySpecException {
        long date = new Date().getTime();
        Map<String, Object> headers = new HashMap<>();
        headers.put(ALG, RS256);
        headers.put(TYPE, JWT);
        Map<String, Object> payload = new HashMap<>();
        payload.put(JTI, UUID.randomUUID());
        payload.put(ISS, hcxCode);
        payload.put(TYP, typ);
        payload.put(PARTICIPANT_NAME, name);
        payload.put(PARTICIPANT_CODE, code);
        payload.put(SUB, sub);
        payload.put(IAT, date);
        return jwtUtils.generateJWS(headers, payload, privatekey);
    }

    private String userInviteRejectTemplate(String email, String participantName) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("PARTICIPANT_NAME", participantName);
        model.put("EMAIL", email);
        return freemarkerService.renderTemplate("user-invite-reject-participant.ftl", model);
    }

    private String userInviteAcceptTemplate(String userId, String participantName, String username, String role) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", username);
        model.put("USER_ID", userId);
        model.put("PARTICIPANT_NAME", participantName);
        model.put("ENV", env);
        model.put("ROLE", role);
        return freemarkerService.renderTemplate("user-invite-accepted-user.ftl", model);
    }

    private String userInviteAcceptParticipantTemplate(String participantName, String username, String role) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("PARTICIPANT_NAME", participantName);
        model.put("USER_NAME", username);
        model.put("ROLE", role);
        return freemarkerService.renderTemplate("user-invite-accepted-participant.ftl", model);
    }


    private String userInviteUserTemplate(String email, String name, String role, URL signedURL) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_EMAIL", email);
        model.put("PARTICIPANT_NAME", name);
        model.put("USER_ROLE", role);
        model.put("USER_INVITE_URL", signedURL);
        return freemarkerService.renderTemplate("user-invite-request-user.ftl", model);
    }

    private String userInviteParticipantTemplate(String name, String role, String userEmail) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("PARTICIPANT_NAME", name);
        model.put("USER_ROLE", role);
        model.put("EMAIL", userEmail);
        return freemarkerService.renderTemplate("user-invite-request-participant.ftl", model);
    }


    private String linkTemplate(String name, String code, URL signedURL, int day, ArrayList<String> role, String userId) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", name);
        model.put("PARTICIPANT_CODE", code);
        model.put("URL", signedURL);
        model.put("role", role.get(0));
        model.put("DAY", day);
        model.put("USER_ID", userId);
        return freemarkerService.renderTemplate("send-email-verification-link.ftl", model);
    }

    private String regenerateLinkTemplate(String name, URL signedURL, int day) throws TemplateException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", name);
        model.put("URL", signedURL);
        model.put("DAY", day);
        return freemarkerService.renderTemplate("regenerate-send-link.ftl", model);
    }

    private String successTemplate(String participantName, Map<String, Object> mockProviderDetails, Map<String, Object> mockPayorDetails) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", participantName);
        model.put("MOCK_PROVIDER_CODE", mockProviderDetails.getOrDefault(PARTICIPANT_CODE, ""));
        model.put("MOCK_PROVIDER_USER_NAME", mockProviderDetails.getOrDefault(PRIMARY_EMAIL, ""));
        model.put("MOCK_PROVIDER_PASSWORD", mockProviderDetails.getOrDefault(PASSWORD, ""));
        model.put("MOCK_PAYOR_CODE", mockPayorDetails.getOrDefault(PARTICIPANT_CODE, ""));
        model.put("MOCK_PAYOR_USER_NAME", mockPayorDetails.getOrDefault(PRIMARY_EMAIL, ""));
        model.put("MOCK_PAYOR_PASSWORD", mockPayorDetails.getOrDefault(PASSWORD, ""));
        return freemarkerService.renderTemplate("onboard-success.ftl", model);
    }

    private String pocSuccessTemplate(String name) throws TemplateException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", name);
        model.put("ONBOARDING_SUCCESS_URL", onboardingSuccessURL);
        return freemarkerService.renderTemplate("onboard-poc-success.ftl", model);
    }

    public String commonTemplate(String templateName) throws Exception {
        return freemarkerService.renderTemplate(templateName, new HashMap<>());
    }

    private String verificationStatus(String name, String status) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", name);
        model.put("STATUS", status);
        return freemarkerService.renderTemplate("verification-status.ftl", model);
    }

    private String passwordGenerate(String participantName, String password, String email) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("PARTICIPANT_NAME", participantName);
        model.put("USERNAME", email);
        model.put("PASSWORD", password);
        return freemarkerService.renderTemplate("password-generate.ftl", model);
    }

    private void addSponsors(List<Map<String, Object>> participantsList) throws Exception {
        String selectQuery = String.format("SELECT * FROM %S WHERE applicant_email IN (%s)", onboardingVerifierTable, getParticipantCodeList(participantsList, PRIMARY_EMAIL));
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String, Object> sponsorMap = new HashMap<>();
        while (resultSet.next()) {
            Sponsor sponsorResponse = new Sponsor(resultSet.getString(APPLICANT_EMAIL), resultSet.getString(APPLICANT_CODE), resultSet.getString(VERIFIER_CODE), resultSet.getString(FORMSTATUS), resultSet.getLong("createdon"), resultSet.getLong("updatedon"));
            sponsorMap.put(resultSet.getString(APPLICANT_EMAIL), sponsorResponse);
        }
        filterSponsors(sponsorMap, participantsList);
    }

    private void addCommunicationStatus(List<Map<String, Object>> participantsList) throws Exception {
        String selectQuery = String.format("SELECT * FROM %s WHERE participant_code IN (%s)", onboardVerificationTable, getParticipantCodeList(participantsList, PARTICIPANT_CODE));
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String, Object> verificationMap = new HashMap<>();
        while (resultSet.next()) {
            Map<String, Object> verification = new HashMap<>();
            verification.put("status", resultSet.getString("status"));
            verification.put("emailVerified", resultSet.getBoolean("email_verified"));
            verification.put("phoneVerified", resultSet.getBoolean("phone_verified"));
            verificationMap.put(resultSet.getString(PARTICIPANT_CODE), verification);
        }
        filterDetails(verificationMap, participantsList, COMMUNICATION);
    }

    private void getOnboardValidations(ArrayList<Map<String, Object>> participantsList) throws Exception {
        String selectQuery = String.format("SELECT * FROM %s WHERE participant_code IN (%s)", onboardVerificationTable, getParticipantCodeList(participantsList, PARTICIPANT_CODE));
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String, Object> onboardValidations = new HashMap<>();
        Map<String, Object> validationsMap = new HashMap<>();
        while (resultSet.next()) {
            String jsonData = resultSet.getString(ONBOARD_VALIDATION_PROPERTIES);
            if (jsonData != null) {
                onboardValidations = JSONUtils.deserialize(jsonData, Map.class);
            }
            if (!onboardValidations.isEmpty()) {
                validationsMap.put(resultSet.getString(PARTICIPANT_CODE), onboardValidations);
            }
            addDefaultConfig(participantsList, onboardValidations, validationsMap, resultSet.getString(PARTICIPANT_CODE));
        }
        filterDetails(validationsMap, participantsList, ONBOARD_VALIDATION_PROPERTIES);
    }

    private void addDefaultConfig(ArrayList<Map<String,Object>> participantList, Map<String, Object> onboardValidations, Map<String,Object> validationMap, String code) {
        for (Map<String, Object> roleMap : participantList) {
            List<String> roles = (List<String>) roleMap.get(ROLES);
            if (roles.contains(PAYOR) && onboardValidations.isEmpty()) {
                validationMap.put(code,getSystemConfig());
            }
        }
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

    private void filterDetails(Map<String, Object> verificationMap, List<Map<String, Object>> participantsList, String type) {
        for (Map<String, Object> responseList : participantsList) {
            String code = (String) responseList.get(PARTICIPANT_CODE);
            if (verificationMap.containsKey(code))
                responseList.put(type, verificationMap.get(code));
        }
    }

    @Async
    private Map<String, Object> createMockParticipant(HttpHeaders headers, String role, Map<String, Object> participantDetails) throws Exception {
        String parentParticipantCode = (String) participantDetails.getOrDefault(PARTICIPANT_CODE, "");
        logger.info("creating Mock participant for :: parent participant code : " + parentParticipantCode + " :: Role: " + role);
        Map<String, Object> mockParticipant = getMockParticipantBody(participantDetails, role, parentParticipantCode);
        String privateKey = (String) mockParticipant.getOrDefault(PRIVATE_KEY, "");
        mockParticipant.remove(PRIVATE_KEY);
        String childParticipantCode = createEntity(PARTICIPANT_CREATE, JSONUtils.serialize(mockParticipant), getHeadersMap(headers), ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, PARTICIPANT_CODE);
        return updateMockDetails(mockParticipant, parentParticipantCode, childParticipantCode, privateKey);
    }

    private void getEmailAndName(String role, Map<String, Object> mockParticipant, Map<String, Object> participantDetails, String name) {
        mockParticipant.put(PRIMARY_EMAIL, SlugUtils.makeEmailSlug((String) participantDetails.getOrDefault(PRIMARY_EMAIL, ""), role));
        mockParticipant.put(PARTICIPANT_NAME, participantDetails.getOrDefault(PARTICIPANT_NAME, "") + " " + name);
    }

    private Map<String, Object> getMockParticipantBody(Map<String, Object> participantDetails, String role, String parentParticipantCode) throws Exception {
        Map<String, Object> mockParticipant = new HashMap<>();
        if (role.equalsIgnoreCase(PAYOR)) {
            mockParticipant.put(ROLES, new ArrayList<>(List.of(PAYOR)));
            mockParticipant.put(SCHEME_CODE, "default");
            mockParticipant.put(ENDPOINT_URL, mockPayorEndpointURL);
            getEmailAndName("mock_payor", mockParticipant, participantDetails, "Mock Payor");
        }
        if (role.equalsIgnoreCase(PROVIDER)) {
            mockParticipant.put(ROLES, new ArrayList<>(List.of(PROVIDER)));
            mockParticipant.put(ENDPOINT_URL, mockProviderEndpointURL);
            getEmailAndName("mock_provider", mockParticipant, participantDetails, "Mock Provider");
        }
        Map<String, Object> certificate = CertificateUtil.generateCertificates(parentParticipantCode, hcxURL);
        mockParticipant.put(SIGNING_CERT_PATH, certificate.getOrDefault(PUBLIC_KEY, ""));
        mockParticipant.put(ENCRYPTION_CERT, certificate.getOrDefault(PUBLIC_KEY, ""));
        mockParticipant.put(PRIVATE_KEY, certificate.getOrDefault(PRIVATE_KEY, ""));
        mockParticipant.put(REGISTRY_STATUS, ACTIVE);
        return mockParticipant;
    }

    private Map<String, Object> updateMockDetails(Map<String, Object> mockParticipant, String parentParticipantCode, String childParticipantCode, String privateKey) throws Exception {
        String childPrimaryEmail = (String) mockParticipant.get(PRIMARY_EMAIL);
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator.Builder().withinRange('0', 'z').filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build();
        String password = randomStringGenerator.generate(12) + "@";
        String query = String.format("INSERT INTO %s (parent_participant_code,child_participant_code,primary_email,password,private_key) VALUES ('%s','%s','%s','%s','%s');",
                mockParticipantsTable, parentParticipantCode, childParticipantCode, childPrimaryEmail, password, privateKey);
        postgresClientMockService.execute(query);
        Map<String, Object> mockParticipantDetails = new HashMap<>();
        mockParticipantDetails.put(PARTICIPANT_CODE, childParticipantCode);
        mockParticipantDetails.put(PRIMARY_EMAIL, childPrimaryEmail);
        mockParticipantDetails.put(PASSWORD, password);
        TimeUnit.SECONDS.sleep(3); // After creating participant, elasticsearch will retrieve data after one second hence added two seconds delay for search API.
        Map<String,Object> registryDetails = getParticipant(PARTICIPANT_CODE,childParticipantCode);
        setKeycloakPassword(childParticipantCode, password ,registryDetails);
        logger.info("created Mock participant for :: parent participant code  : " + parentParticipantCode + " :: child participant code  : " + childParticipantCode);
        return mockParticipantDetails;
    }

    private void setKeycloakPassword(String participantCode, String password , Map<String,Object> registryDetails) throws ClientException {
        try {
            ArrayList<String> osOwner = (ArrayList<String>) registryDetails.get(OS_OWNER);
            RealmResource realmResource = keycloak.realm(keycloackParticipantRealm);
            UserResource userResource = realmResource.users().get(osOwner.get(0));
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);
            userResource.resetPassword(passwordCred);
            logger.info("The Keycloak password for the os_owner :" + osOwner.get(0) + " has been successfully updated");
        } catch (Exception e) {
            throw new ClientException("Unable to set keycloak password : " + e.getMessage());
        }
    }

    private void getMockParticipant(List<Map<String, Object>> participantsList, HttpHeaders headers) throws Exception {
        validateRole(headers);
        String selectQuery = String.format("SELECT * FROM %s WHERE parent_participant_code IN (%s)", mockParticipantsTable, getParticipantCodeList(participantsList, PARTICIPANT_CODE));
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

    private String getParticipantCodeList(List<Map<String, Object>> participantsList, String key) {
        String participantCodeList = participantsList.stream().map(participant -> participant.get(key)).collect(Collectors.toList()).toString();
        return getParticipantWithQuote(participantCodeList);
    }
    private Map<String, Object> getSystemConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(EMAIL, emailConfig);
        configMap.put(PHONE, phoneConfig);
        return configMap;
    }

    private String convertMapJson(Map<String,Object> onboardValidations){
        Gson gson = new Gson();
        return gson.toJson(onboardValidations);
    }

    private String generateRandomPassword(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        return RandomStringUtils.random(length, characters);
    }

    public Response generateAndSetPassword(String participantCode) throws Exception {
        String password = generateRandomPassword(24);
        Map<String, Object> registryDetails = getParticipant(PARTICIPANT_CODE, participantCode);
        setKeycloakPassword(participantCode, password, registryDetails);
        emailService.sendMail((String) registryDetails.get(PRIMARY_EMAIL), passwordGenerateSub, passwordGenerate((String) registryDetails.get(PARTICIPANT_NAME),password,(String) registryDetails.get(PRIMARY_EMAIL)));
        return getSuccessResponse();
    }

    public void validateAdminRole(HttpHeaders headers, String participantCode) throws Exception {
        boolean result = false;
        String jwtToken = Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0);
        Token token = new Token(jwtToken);
        for (Map<String, String> roleMap : token.getTenantRoles()) {
            if (StringUtils.equals(roleMap.get(PARTICIPANT_CODE), participantCode) && StringUtils.equals(roleMap.get(ROLE), ADMIN)) {
                result = true;
            }
        }
        if (!result) {
            throw new ClientException(ErrorCodes.ERR_INVALID_JWT, "Only users with an admin role assigned to the " + participantCode  + " are authorized to generate the password.");
        }
    }

    private Response getSuccessResponse() {
        Response response = new Response();
        response.setStatus(SUCCESSFUL);
        return response;
    }

    private void updateStatus(String email, String status) throws Exception {
        String query = String.format("UPDATE %s SET status='%s',updatedOn=%d WHERE applicant_email='%s'", onboardingVerifierTable, status, System.currentTimeMillis(), email);
        postgreSQLClient.execute(query);
    }

    private void updateIdentityStatus(OnboardRequest request) throws Exception {
        if (request.getRoles().contains(PAYOR)){
            int random = new SecureRandom().nextInt(999999 - 100000 + 1) + 100000;
            request.setApplicantCode(String.valueOf(random));
        }
        String query = String.format("INSERT INTO %s (applicant_email,applicant_code,verifier_code,status,createdOn,updatedOn) VALUES ('%s','%s','%s','%s',%d,%d);",
                onboardingVerifierTable, request.getPrimaryEmail(), request.getApplicantCode(), request.getVerifierCode(), org.swasth.common.utils.Constants.PENDING, System.currentTimeMillis(), System.currentTimeMillis());
        postgreSQLClient.execute(query);
    }

}
