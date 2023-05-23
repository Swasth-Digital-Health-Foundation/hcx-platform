package org.swasth.hcx.service;

import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.ICloudService;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.exception.*;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.common.utils.SlugUtils;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.*;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Service
public class ParticipantService {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantService.class);
    @Value("${certificates.bucketName}")
    private String bucketName;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardOtpTable;

    @Value("${postgres.onboardingTable}")
    private String onboardingTable;
    @Value("${registry.apiPath}")
    private String registryApiPath;
    @Value("${registry.basePath}")
    private String registryURL;

    @Value("${participantCode.fieldSeparator}")
    private String fieldSeparator;

    @Value("${hcx.instanceName}")
    private String hcxInstanceName;

    @Autowired
    protected IDatabaseService postgreSQLClient;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private ICloudService cloudClient;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private Environment env;
    @Autowired
    private EventGenerator eventGenerator;
    @Autowired
    private EventHandler eventHandler;

    public RegistryResponse invite(Map<String, Object> requestBody, HttpHeaders header, String code, String entity) throws Exception {
        String url = registryURL + registryApiPath + entity + "/" + INVITE;
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
        if (response.getStatus() == 200) {
            if (StringUtils.equals(entity, ORGANISATION)) {
                generatePcptAudit(code, PARTICIPANT_CREATE, requestBody, CREATED);
                logger.info("Created participant :: participant code: {}", requestBody.get(PARTICIPANT_CODE));
            } else {
                generateUserAudit(code, requestBody,USER_CREATE);
                logger.info("Created user :: user id: {}", code);
            }
        }
        return responseHandler(response, code, entity);
    }

    public RegistryResponse update(Map<String, Object> requestBody, Map<String, Object> participant, HttpHeaders header, String code, String entity) throws Exception {
        String url = registryURL + registryApiPath + entity + "/" + participant.get(OSID);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
        if (response.getStatus() == 200) {
            if (StringUtils.equals(entity, ORGANISATION)) {
                deleteCache(code);
                generatePcptAudit(code, PARTICIPANT_UPDATE, requestBody, "");
                logger.info("Updated participant :: participant code: {}", requestBody.get(PARTICIPANT_CODE));
            } else {
                generateUserAudit(code, requestBody,USER_UPDATE);
                logger.info("Updated user :: user id: {}", code);
            }
        }
        return responseHandler(response, code, entity);
    }

    public RegistryResponse search(Map<String, Object> requestBody, String entity) throws Exception {
        String url = registryURL + registryApiPath + entity + "/" + SEARCH;
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
        if (response.getStatus() == 200) {
            logger.info("Search is completed :: status code: {}", response.getStatus());
        }
        return responseHandler(response, null, entity);
    }

    public Map<String, Object> read(String code, String entity) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class), entity));
        RegistryResponse searchResp = (RegistryResponse) searchResponse.getBody();
        logger.info("Read participant is completed");
        if (searchResp != null && !searchResp.getParticipants().isEmpty())
            return (Map<String, Object>) searchResp.getParticipants().get(0);
        else
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, "Please provide valid participant code");

    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders header, String code, String entity) throws Exception {
        String url = registryURL + registryApiPath  + entity + "/" + registryDetails.get(OSID);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> response = HttpUtils.delete(url, headersMap);
        if (response.getStatus() == 200) {
            deleteCache(code);
            if (StringUtils.equals(entity, ORGANISATION)) {
                generatePcptAudit(code, PARTICIPANT_DELETE, registryDetails, INACTIVE);
                logger.info("Participant deleted :: participant code: {}", code);
            } else {
                generateUserAudit(code, registryDetails,USER_DELETE);
                logger.info("User deleted :: userId: {}", code);
                RegistryResponse registryResponse = new RegistryResponse(code, entity);
                registryResponse.setStatus(INACTIVE);
                return registryResponse;
            }
        }
        return responseHandler(response, code, entity);
    }

    public void getCertificates(Map<String, Object> requestBody, String participantCode, String key) {
        if (requestBody.getOrDefault(key, "").toString().startsWith("-----BEGIN CERTIFICATE-----") && requestBody.getOrDefault(key, "").toString().endsWith("-----END CERTIFICATE-----")) {
            String certificateData = requestBody.getOrDefault(key, "").toString().replace(" ", "\n").replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----").replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");
            cloudClient.putObject(participantCode, bucketName);
            cloudClient.putObject(bucketName, participantCode + "/" + key + ".pem", certificateData);
            requestBody.put(key, cloudClient.getUrl(bucketName, participantCode + "/" + key + ".pem").toString());
        }
    }

    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

    public RegistryResponse responseHandler(HttpResponse<String> response, String code, String entity) throws Exception {
        switch (response.getStatus()) {
            case 200:
                if (response.getBody().isEmpty()) {
                    return new RegistryResponse("", entity);
                } else {
                    if (response.getBody().startsWith("["))
                        return new RegistryResponse(JSONUtils.deserialize(response.getBody(), ArrayList.class), entity);
                    else
                        return new RegistryResponse(code, entity);
                }
            case 400:
                throw new ClientException(getErrorMessage(response));
            case 401:
                throw new AuthorizationException(getErrorMessage(response));
            case 404:
                throw new ResourceNotFoundException(getErrorMessage(response));
            default:
                throw new ServerException(getErrorMessage(response));
        }
    }

    public void deleteCache(String code) throws Exception {
        if (redisCache.isExists(code))
            redisCache.delete(code);
    }

    public Map<String, Object> getEData(String status, String prevStatus, List<String> props) {
        Map<String, Object> data = new HashMap<>();
        data.put(AUDIT_STATUS, status);
        data.put(PREV_STATUS, prevStatus);
        if (!props.isEmpty())
            data.put(PROPS, props);
        return data;
    }

    private Map<String, Object> getCData(String action, Map<String, Object> registryDetails) {
        Map<String, Object> cdata = new HashMap<>();
        cdata.put(ACTION, action);
        cdata.putAll(registryDetails);
        return cdata;
    }
    public void getCertificatesUrl(Map<String, Object> requestBody, String code) {
        getCertificates(requestBody, code, ENCRYPTION_CERT);
        if (requestBody.containsKey(SIGNING_CERT_PATH))
            getCertificates(requestBody, code, SIGNING_CERT_PATH);
    }

    public Map<String, Object> getParticipant(String code, String entity) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class), entity));
        RegistryResponse registryResponse = (RegistryResponse) Objects.requireNonNull(searchResponse.getBody());
        if (registryResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) registryResponse.getParticipants().get(0);
    }

    public void validate(Map<String, Object> requestBody, boolean isCreate) throws ClientException, CertificateException, IOException {
        List<String> notAllowedUrls = env.getProperty(HCX_NOT_ALLOWED_URLS, List.class, new ArrayList<String>());
        if (isCreate) {
            if (!requestBody.containsKey(ROLES) || !(requestBody.get(ROLES) instanceof ArrayList) || ((ArrayList<String>) requestBody.get(ROLES)).isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_ROLES_PROPERTY);
            else if (((ArrayList<String>) requestBody.get(ROLES)).contains(PAYOR) && !requestBody.containsKey(SCHEME_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, MISSING_SCHEME_CODE);
            else if (!((ArrayList<String>) requestBody.get(ROLES)).contains(PAYOR) && requestBody.containsKey(SCHEME_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, UNKNOWN_PROPERTY);
            else if (notAllowedUrls.contains(requestBody.get(ENDPOINT_URL)))
                throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_END_POINT);
            else if (!requestBody.containsKey(PRIMARY_EMAIL) || !(requestBody.get(PRIMARY_EMAIL) instanceof String)
                    || !EmailValidator.getInstance().isValid((String) requestBody.get(PRIMARY_EMAIL)))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_EMAIL);
        } else {
            if (notAllowedUrls.contains(requestBody.getOrDefault(ENDPOINT_URL, "")))
                throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_END_POINT);
            if (requestBody.containsKey(ENCRYPTION_CERT))
                requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(ENCRYPTION_CERT)));
            if (requestBody.containsKey(SIGNING_CERT_PATH))
                requestBody.put(SIGNING_CERT_PATH_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(SIGNING_CERT_PATH)));
        }
    }

    public void validateCertificates(Map<String, Object> requestBody) throws ClientException, CertificateException, IOException {
        if (!requestBody.containsKey(ENCRYPTION_CERT) || !(requestBody.get(ENCRYPTION_CERT) instanceof String))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_ENCRYPTION_CERT);
        if (requestBody.containsKey(SIGNING_CERT_PATH))
            requestBody.put(SIGNING_CERT_PATH_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(SIGNING_CERT_PATH)));
        requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(ENCRYPTION_CERT)));
    }

    public String getRequestBody(String code) {
        return "{ \"filters\": { \"participant_code\": { \"eq\": \" " + code + "\" } } }";
    }

    public Map<String, Object> getUser(String userId, String entity) throws Exception {
        logger.info("searching for :: user id : {}", userId);
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getUserRequest(userId), Map.class), entity));
        RegistryResponse registryResponse = (RegistryResponse) Objects.requireNonNull(searchResponse.getBody());
        if (registryResponse.getUsers().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_USER_ID, INVALID_USER_ID);
        return (Map<String, Object>) registryResponse.getUsers().get(0);
    }

    public String getUserRequest(String userId) {
        return "{ \"filters\": { \"user_id\": { \"eq\": \" " + userId + "\" } } }";
    }

    public void generatePcptAudit(String code, String action, Map<String, Object> requestBody, String registryStatus) throws Exception {
        eventHandler.createAudit(eventGenerator.createAuditLog(code, PARTICIPANT, getCData(action, requestBody),
                getEData(registryStatus, "", Collections.emptyList())));
    }

    public void generateUserAudit(String userId, Map<String, Object> requestBody,String action) throws Exception {
        eventHandler.createAudit(eventGenerator.createAuditLog(userId, USER, getCData(action, requestBody), new HashMap<>()));
    }

    public String createUserId(Map<String, Object> requestBody) throws ClientException {
        if (requestBody.containsKey(EMAIL) || requestBody.containsKey(MOBILE)) {
            if (requestBody.containsKey(EMAIL) && EmailValidator.getInstance().isValid((String) requestBody.get(EMAIL) )) {
                return SlugUtils.makeSlug((String) requestBody.get(EMAIL), "", fieldSeparator, hcxInstanceName);
            } else if (requestBody.containsKey(MOBILE)) {
                return requestBody.get(MOBILE) + "@" + hcxInstanceName;
            }
        }
        throw new ClientException(ErrorCodes.ERR_INVALID_USER_DETAILS,INVALID_USER_DETAILS);
    }

}