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
public class ParticipantService extends BaseRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantService.class);
    @Value("${certificates.bucketName}")
    private String bucketName;

    @Value("${postgres.onboardingOtpTable}")
    private String onboardOtpTable;

    @Value("${postgres.onboardingTable}")
    private String onboardingTable;
    @Value("${registry.organisation-api-path}")
    private String registryOrgnisationPath;

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

    public RegistryResponse create(Map<String, Object> requestBody, HttpHeaders header, String code) throws Exception {
        HttpResponse<String> response = registryInvite(requestBody, header, registryOrgnisationPath);
        if (response.getStatus() == 200) {
            generatePcptAudit(code, PARTICIPANT_CREATE, requestBody, CREATED);
            logger.info("Created participant :: participant code: {}", requestBody.get(PARTICIPANT_CODE));
        }
        return responseHandler(response, code, ORGANISATION);
    }

    public RegistryResponse update(Map<String, Object> requestBody, Map<String, Object> registryDetails, HttpHeaders header, String code) throws Exception {
        HttpResponse<String> response = registryUpdate(requestBody, registryDetails, header, registryOrgnisationPath);
        if (response.getStatus() == 200) {
            deleteCache(code);
            generatePcptAudit(code, PARTICIPANT_UPDATE, requestBody, "");
            logger.info("Updated participant :: participant code: {}", requestBody.get(PARTICIPANT_CODE));
        }
        return responseHandler(response, code, ORGANISATION);
    }

    public RegistryResponse search(Map<String, Object> requestBody) throws Exception {
        return registrySearch(requestBody, registryOrgnisationPath,ORGANISATION);
    }

    public Map<String, Object> read(String code) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class)));
        RegistryResponse searchResp = (RegistryResponse) searchResponse.getBody();
        logger.info("Read participant is completed");
        if (searchResp != null && !searchResp.getParticipants().isEmpty())
            return (Map<String, Object>) searchResp.getParticipants().get(0);
        else
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, "Please provide valid participant code");

    }

    public RegistryResponse delete(Map<String, Object> registryDetails, HttpHeaders header, String code) throws Exception {
        HttpResponse<String> response = registryDelete(registryDetails, header, registryOrgnisationPath);
        if (response.getStatus() == 200) {
            deleteCache(code);
            generatePcptAudit(code, PARTICIPANT_DELETE, registryDetails, INACTIVE);
            logger.info("Participant deleted :: participant code: {}", code);
        }
        return responseHandler(response, code, ORGANISATION);
    }

    private void getCertificates(Map<String, Object> requestBody, String participantCode, String key) {
        if (requestBody.getOrDefault(key, "").toString().startsWith("-----BEGIN CERTIFICATE-----") && requestBody.getOrDefault(key, "").toString().endsWith("-----END CERTIFICATE-----")) {
            String certificateData = requestBody.getOrDefault(key, "").toString().replace(" ", "\n").replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----").replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");
            cloudClient.putObject(participantCode, bucketName);
            cloudClient.putObject(bucketName, participantCode + "/" + key + ".pem", certificateData);
            requestBody.put(key, cloudClient.getUrl(bucketName, participantCode + "/" + key + ".pem").toString());
        }
    }


    private void deleteCache(String code) throws Exception {
        if (redisCache.isExists(code))
            redisCache.delete(code);
    }

    private Map<String, Object> getEData(String status, String prevStatus, List<String> props) {
        Map<String, Object> data = new HashMap<>();
        data.put(AUDIT_STATUS, status);
        data.put(PREV_STATUS, prevStatus);
        if (!props.isEmpty())
            data.put(PROPS, props);
        return data;
    }

    public void getCertificatesUrl(Map<String, Object> requestBody, String code) {
        getCertificates(requestBody, code, ENCRYPTION_CERT);
        if (requestBody.containsKey(SIGNING_CERT_PATH))
            getCertificates(requestBody, code, SIGNING_CERT_PATH);
    }

    public Map<String, Object> getParticipant(String code) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class)));
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

    private String getRequestBody(String code) {
        return "{ \"filters\": { \"participant_code\": { \"eq\": \" " + code + "\" } } }";
    }

    private void generatePcptAudit(String code, String action, Map<String, Object> requestBody, String registryStatus) throws Exception {
        eventHandler.createAudit(eventGenerator.createAuditLog(code, PARTICIPANT, getCData(action, requestBody),
                getEData(registryStatus, "", Collections.emptyList())));
    }


}