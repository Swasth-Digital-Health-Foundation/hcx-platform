package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.Sponsor;
import org.swasth.common.exception.*;
import org.swasth.common.utils.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController extends BaseController {

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${redis.expires}")
    private int redisExpires;

    @Value("${participantCode.fieldSeparator}")
    private String fieldSeparator;

    @Value("${hcx.instanceName}")
    private String hcxInstanceName;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    protected IDatabaseService postgreSQLClient;

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> participantCreate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            validateCreateParticipant(requestBody);
            String primaryEmail = (String) requestBody.get(PRIMARY_EMAIL);
            String participantCode = SlugUtils.makeSlug(primaryEmail, "", fieldSeparator, hcxInstanceName);
            while (isParticipantCodeExists(participantCode)) {
                participantCode = SlugUtils.makeSlug(primaryEmail, String.valueOf(new SecureRandom().nextInt(1000)), fieldSeparator, hcxInstanceName);
            }
            requestBody.put(PARTICIPANT_CODE, participantCode);
            String url = registryUrl + "/api/v1/Organisation/invite";
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
            if (response.getStatus() == 200) {
                Map<String, Object> cdata = new HashMap<>();
                cdata.put(ACTION, PARTICIPANT_CREATE);
                cdata.putAll(requestBody);
                eventHandler.createAudit(eventGenerator.createAuditLog(participantCode, PARTICIPANT, cdata,
                        getEData(CREATED, "", Collections.emptyList())));
            }
            return responseHandler(response, participantCode);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_UPDATE)
    public ResponseEntity<Object> participantUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            validateUpdateParticipant(requestBody);
            String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
            Map<String, Object> participant = getParticipant(participantCode);
            String url = registryUrl + "/api/v1/Organisation/" + participant.get(OSID);
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
            if (response.getStatus() == 200) {
                deleteCache(participantCode);
            }
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private void deleteCache(String participantCode) throws Exception {
        if (redisCache.isExists(participantCode))
            redisCache.delete(participantCode);
    }

    private Map<String, Object> getParticipant(String participantCode) throws Exception {
        ResponseEntity<Object> searchResponse = participantSearch("",JSONUtils.deserialize("{ \"filters\": { \"participant_code\": { \"eq\": \" " + participantCode + "\" } } }", Map.class));
        ParticipantResponse participantResponse = (ParticipantResponse) Objects.requireNonNull(searchResponse.getBody());
        if (participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, INVALID_PARTICIPANT_CODE);
        return (Map<String, Object>) participantResponse.getParticipants().get(0);
    }

    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> participantSearch(@RequestParam(required = false) String fields,@RequestBody Map<String, Object> requestBody) {
        try {
            if (fields != null && fields.toLowerCase().contains(SPONSOR)) {
                return getSponsors(requestBody);
            }
            String url = registryUrl + "/api/v1/Organisation/search";
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }


    @PostMapping(PARTICIPANT_DELETE)
    public ResponseEntity<Object> participantDelete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey(PARTICIPANT_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, PARTICIPANT_CODE_MSG);
            String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
            Map<String, Object> participant = getParticipant(participantCode);
            String url = registryUrl + "/api/v1/Organisation/" + participant.get(OSID);
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.delete(url, headersMap);
            if (response.getStatus() == 200) {
                deleteCache(participantCode);
                Map<String, Object> cdata = new HashMap<>();
                cdata.put(ACTION, PARTICIPANT_DELETE);
                cdata.putAll(participant);
                eventHandler.createAudit(eventGenerator.createAuditLog(participantCode, PARTICIPANT, cdata,
                        getEData(INACTIVE, (String) participant.get(AUDIT_STATUS), Collections.emptyList())));
            }
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private boolean isParticipantCodeExists(String participantCode) throws Exception {
        ResponseEntity<Object> searchResponse = participantSearch("",JSONUtils.deserialize("{ \"filters\": { \"participant_code\": { \"eq\": \" " + participantCode + "\" } } }", Map.class));
        Object responseBody = searchResponse.getBody();
        if (responseBody != null) {
            if (responseBody instanceof Response) {
                Response response = (Response) responseBody;
                throw new ServerException(MessageFormat.format(PARTICIPANT_ERROR_MSG, response.getError().getMessage()));
            } else {
                ParticipantResponse participantResponse = (ParticipantResponse) responseBody;
                return !participantResponse.getParticipants().isEmpty();
            }
        } else {
            throw new ServerException(INVALID_REGISTRY_RESPONSE);
        }
    }

    private void validateCreateParticipant(Map<String, Object> requestBody) throws ClientException, CertificateException, IOException {
        List<String> notAllowedUrls = env.getProperty(HCX_NOT_ALLOWED_URLS, List.class, new ArrayList<String>());
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
        else if (!requestBody.containsKey(ENCRYPTION_CERT) || !(requestBody.get(ENCRYPTION_CERT) instanceof String))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, INVALID_ENCRYPTION_CERT);
        // add encryption certificate expiry
        requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(ENCRYPTION_CERT)));
    }

    private void validateUpdateParticipant(Map<String, Object> requestBody) throws ClientException, CertificateException, IOException {
        List<String> notAllowedUrls = env.getProperty(HCX_NOT_ALLOWED_URLS, List.class, new ArrayList<String>());
        if (notAllowedUrls.contains(requestBody.getOrDefault(ENDPOINT_URL, "")))
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_END_POINT);
        else if (requestBody.containsKey(ENCRYPTION_CERT))
            requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry((String) requestBody.get(ENCRYPTION_CERT)));
    }


    private Map<String, Object> getEData(String status, String prevStatus, List<String> props) {
        Map<String, Object> data = new HashMap<>();
        data.put(AUDIT_STATUS, status);
        data.put(PREV_STATUS, prevStatus);
        if (!props.isEmpty())
            data.put(PROPS, props);
        return data;
    }

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
    private ResponseEntity<Object> getSponsors(Map<String, Object> requestBody) throws Exception {
        String url = registryUrl + "/api/v1/Organisation/search";
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
        List<String> primaryEmailList = new ArrayList<>();
        List<Map<String, Object>> participantsList = JSONUtils.deserialize(response.getBody(), ArrayList.class);
        for (Map<String, Object> participants : participantsList) {
            primaryEmailList.add(participants.get("primary_email").toString());
        }
        // adding single quote to the all the 'primary_emails' in the list
        String primaryEmailWithQuote = "'" + StringUtils.join(primaryEmailList, "','") + "'";
        String selectQuery = String.format("SELECT * FROM onboarding WHERE applicant_email IN (%s);", String.join(",", primaryEmailWithQuote)); // need to remove '[]' from list
        ResultSet resultSet = (ResultSet) postgreSQLClient.executeQuery(selectQuery);
        List<Sponsor> sponsors = new ArrayList<>();
        while (resultSet.next()) {
            Sponsor sponsorResponse = new Sponsor(resultSet.getString("applicant_email"), resultSet.getString("applicant_code"), resultSet.getString("sponsor_code"), resultSet.getString("status"), resultSet.getLong("createdon"), resultSet.getLong("updatedon"));
            sponsors.add(sponsorResponse);
        }
        ArrayList<Object> modifiedResponseList = new ArrayList<>();
        for (Map<String, Object> responseList : participantsList) {
            boolean matchingPrimaryEmail = false;
            for (Sponsor sponsorList : sponsors) {
                if (responseList.get("primary_email").equals(sponsorList.getApplicantEmail())) {
                    Object sponsorDetails = JSONUtils.convert(sponsorList, Map.class);
                    responseList.put("sponsor_details", sponsorDetails);
                    modifiedResponseList.add(responseList);
                    matchingPrimaryEmail = true;
                    break;
                }
            }
            if (!matchingPrimaryEmail) {
                Map<String, Object> emptySponsorDetails = new HashMap<>();
                responseList.put("sponsor_details", emptySponsorDetails);
                modifiedResponseList.add(responseList);
            }
        }
        return getSuccessResponse(new ParticipantResponse(modifiedResponseList));
    }
}

