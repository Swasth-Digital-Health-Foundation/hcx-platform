package org.swasth.hcx.service;

import kong.unirest.HttpResponse;
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
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Sponsor;
import org.swasth.common.exception.*;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

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

    public ParticipantResponse invite(Map<String, Object> requestBody, String registryUrl, HttpHeaders header, String code) throws Exception {
        String url = registryUrl + registryApiPath + INVITE;
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
        if (response.getStatus() == 200) {
            Map<String, Object> cdata = new HashMap<>();
            cdata.put(ACTION, PARTICIPANT_CREATE);
            cdata.putAll(requestBody);
            eventHandler.createAudit(eventGenerator.createAuditLog(code, PARTICIPANT, cdata,
                    getEData(CREATED, "", Collections.emptyList())));
            logger.info("Created participant :: participant code: {}", requestBody.get(PARTICIPANT_CODE));
        }
        return responseHandler(response, code);
    }

    public ParticipantResponse update(Map<String, Object> requestBody, Map<String, Object> participant, String registryUrl, HttpHeaders header, String code) throws Exception {
        String url = registryUrl + registryApiPath + participant.get(OSID);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
        if (response.getStatus() == 200) {
            deleteCache(code);
            logger.info("Updated participant :: participant code: {}", requestBody.get(PARTICIPANT_CODE));
        }
        return responseHandler(response, code);
    }

    public ParticipantResponse search(Map<String, Object> requestBody, String registryUrl, String fields) throws Exception {
        String url = registryUrl + registryApiPath + SEARCH;
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
        ArrayList<Map<String, Object>> participantList = JSONUtils.deserialize(response.getBody(), ArrayList.class);
        if (fields != null && fields.toLowerCase().contains(SPONSORS))
            addSponsors(participantList);
        if(fields != null && fields.toLowerCase().contains(VERIFICATION_STATUS))
            addVerificationStatus(participantList);
        if(response.getStatus() == 200){
            logger.info("Search is completed :: status code: {}", response.getStatus());
            return new ParticipantResponse(participantList);
        }
        return responseHandler(response, null);
    }

    public ResponseEntity<Object> read(String fields, String code, String registryUrl, String pathParam) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class), registryUrl, pathParam));
        ParticipantResponse searchResp = (ParticipantResponse) searchResponse.getBody();
        if (fields != null && fields.toLowerCase().contains(VERIFICATION_STATUS) && searchResp != null) {
            ((Map<String, Object>) searchResp.getParticipants().get(0)).putAll(getVerificationStatus(code));
        }
        logger.info("Read participant is completed");
        return getSuccessResponse(searchResp);
    }

    public ParticipantResponse delete(Map<String, Object> participant, String registryUrl, HttpHeaders header, String code) throws Exception {
        String url = registryUrl + registryApiPath + participant.get(OSID);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        HttpResponse<String> response = HttpUtils.delete(url, headersMap);
        if (response.getStatus() == 200) {
            deleteCache(code);
            Map<String, Object> cdata = new HashMap<>();
            cdata.put(ACTION, PARTICIPANT_DELETE);
            cdata.putAll(participant);
            eventHandler.createAudit(eventGenerator.createAuditLog(code, PARTICIPANT, cdata,
                    getEData(INACTIVE, (String) participant.get(AUDIT_STATUS), Collections.emptyList())));
            logger.info("Participant deleted :: participant code: {}", code);
        }
        return responseHandler(response, code);
    }

    public void getCertificates(Map<String, Object> requestBody, String participantCode, String key) {
        if (requestBody.getOrDefault(key, "").toString().startsWith("-----BEGIN CERTIFICATE-----") && requestBody.getOrDefault(key, "").toString().endsWith("-----END CERTIFICATE-----")) {
            String certificateData = requestBody.getOrDefault(key, "").toString().replace(" ", "\n").replace("-----BEGIN\nCERTIFICATE-----", "-----BEGIN CERTIFICATE-----").replace("-----END\nCERTIFICATE-----", "-----END CERTIFICATE-----");
            cloudClient.putObject(participantCode, bucketName);
            cloudClient.putObject(bucketName, participantCode + "/" + key + ".pem", certificateData);
            requestBody.put(key, cloudClient.getUrl(bucketName, participantCode + "/" + key + ".pem").toString());
        }
    }

    public Map<String, Object> getVerificationStatus(String code) throws Exception {
        String selectQuery = String.format("SELECT status FROM %s WHERE participant_code ='%s'", onboardOtpTable, code);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String, Object> responseMap = new HashMap<>();
        while (resultSet.next()) {
            responseMap.put(FORMSTATUS, resultSet.getString("status"));
        }
        return Collections.singletonMap(VERIFICATION_STATUS, responseMap);
    }

    public void addSponsors(List<Map<String, Object>> participantsList) throws Exception {
        String primaryEmailList = participantsList.stream().map(participant -> participant.get(PRIMARY_EMAIL)).collect(Collectors.toList()).toString();
        String primaryEmailWithQuote = getParticipantWithQuote(primaryEmailList);
        String selectQuery = String.format("SELECT * FROM %S WHERE applicant_email IN (%s)", onboardingTable, primaryEmailWithQuote);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String, Object> sponsorMap = new HashMap<>();
        while (resultSet.next()) {
            Sponsor sponsorResponse = new Sponsor(resultSet.getString(APPLICANT_EMAIL), resultSet.getString(APPLICANT_CODE), resultSet.getString(VERIFIER_CODE), resultSet.getString(FORMSTATUS), resultSet.getLong("createdon"), resultSet.getLong("updatedon"));
            sponsorMap.put(resultSet.getString(APPLICANT_EMAIL), sponsorResponse);
        }
        filterSponsors(sponsorMap, participantsList);
    }

    public void addVerificationStatus(List<Map<String, Object>> participantsList) throws Exception {
        String participantCodeList = participantsList.stream().map(participant -> participant.get(PARTICIPANT_CODE)).collect(Collectors.toList()).toString();
        String participantCodeQuote = getParticipantWithQuote(participantCodeList);
        String selectQuery = String.format("SELECT * FROM %s WHERE participant_code IN (%s)", onboardOtpTable, participantCodeQuote);
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        Map<String,Object> verificationMap = new HashMap<>();
        while (resultSet.next()) {
            verificationMap.put(resultSet.getString(PARTICIPANT_CODE),resultSet.getString("status"));
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
            if(verificationMap.containsKey(code))
                responseList.put("verificationStatus", verificationMap.get(code));
        }
    }
    public ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

    public ParticipantResponse responseHandler(HttpResponse<String> response, String code) throws Exception {
        switch (response.getStatus()) {
            case 200:
                if (response.getBody().isEmpty()) {
                    return new ParticipantResponse("");
                } else {
                    if (response.getBody().startsWith("["))
                        return new ParticipantResponse(JSONUtils.deserialize(response.getBody(), ArrayList.class));
                    else
                        return new ParticipantResponse(code);
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

    public void getCertificatesUrl(Map<String, Object> requestBody, String code) {
        getCertificates(requestBody, code, ENCRYPTION_CERT);
        if(requestBody.containsKey(SIGNING_CERT_PATH))
            getCertificates(requestBody, code, SIGNING_CERT_PATH);
    }

    public Map<String, Object> getParticipant(String code, String registryUrl) throws Exception {
        ResponseEntity<Object> searchResponse = getSuccessResponse(search(JSONUtils.deserialize(getRequestBody(code), Map.class), registryUrl, ""));
        ParticipantResponse participantResponse = (ParticipantResponse) Objects.requireNonNull(searchResponse.getBody());
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
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
}