package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpResponse;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.*;
import org.swasth.common.utils.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.redis.cache.RedisCache;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class ParticipantController  extends BaseController {

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

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> participantCreate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            validateParticipant(requestBody);
            String primaryEmail = (String) requestBody.get(PRIMARY_EMAIL);
            String participantCode = SlugUtils.makeSlug(primaryEmail, "", fieldSeparator, hcxInstanceName);
            while(isParticipantCodeExists(participantCode)){
                participantCode = SlugUtils.makeSlug(primaryEmail, String.valueOf(new SecureRandom().nextInt(1000)), fieldSeparator, hcxInstanceName);
            }
            requestBody.put(PARTICIPANT_CODE, participantCode);
            String url =  registryUrl + "/api/v1/Organisation/invite";
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
            if(response.getStatus() == 200) {
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
            validateParticipant(requestBody);
            String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
            Map<String,Object> participant = getParticipant(participantCode);
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
        if(redisCache.isExists(participantCode))
            redisCache.delete(participantCode);
    }

    private Map<String,Object> getParticipant(String participantCode) throws Exception {
        ResponseEntity<Object> searchResponse = participantSearch(JSONUtils.deserialize("{ \"filters\": { \"participant_code\": { \"eq\": \" " + participantCode + "\" } } }", Map.class));
        ParticipantResponse participantResponse = (ParticipantResponse) Objects.requireNonNull(searchResponse.getBody());
        if(participantResponse.getParticipants().isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, "Please provide valid participant code");
        return (Map<String,Object>) participantResponse.getParticipants().get(0);
    }

    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) {
        try {
            String url =  registryUrl + "/api/v1/Organisation/search";
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }


    @PostMapping(PARTICIPANT_DELETE)
    public ResponseEntity<Object> participantDelete(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            if(!requestBody.containsKey(PARTICIPANT_CODE))
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, "Please provide valid participant code");
            String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
            Map<String,Object> participant = getParticipant(participantCode);
            String url =  registryUrl + "/api/v1/Organisation/" + participant.get(OSID);
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.delete(url, headersMap);
            if(response.getStatus() == 200) {
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
        ResponseEntity<Object> searchResponse = participantSearch(JSONUtils.deserialize("{ \"filters\": { \"participant_code\": { \"eq\": \" " + participantCode + "\" } } }", Map.class));
        Object responseBody = searchResponse.getBody();
        if (responseBody != null) {
            if(responseBody instanceof Response){
                Response response = (Response) responseBody;
                throw new ServerException("Error in creating participant :: Exception: " + response.getError().getMessage());
            } else {
                ParticipantResponse participantResponse = (ParticipantResponse) responseBody;
                return !participantResponse.getParticipants().isEmpty();
            }
        } else {
            throw new ServerException("Error in creating participant, invalid response from registry");
        }
    }

    private void validateParticipant(Map<String, Object> requestBody) throws ClientException, CertificateException, IOException {
        List<String> notAllowedUrls = env.getProperty(HCX_NOT_ALLOWED_URLS, List.class, new ArrayList<String>());
        if (!requestBody.containsKey(ROLES) || !(requestBody.get(ROLES) instanceof ArrayList) || ((ArrayList<String>) requestBody.get(ROLES)).isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "roles property cannot be null, empty or other than 'ArrayList'");
        else if(((ArrayList<String>) requestBody.get(ROLES)).contains(PAYOR) && !requestBody.containsKey(SCHEME_CODE))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "scheme_code property is missing");
        else if (!((ArrayList<String>) requestBody.get(ROLES)).contains(PAYOR) && requestBody.containsKey(SCHEME_CODE))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "unknown property, 'scheme_code' is not allowed");
        else if (notAllowedUrls.contains(requestBody.get(ENDPOINT_URL)))
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "end point url should not be the HCX Gateway/APIs URL");
        else if (!requestBody.containsKey(PRIMARY_EMAIL) || !(requestBody.get(PRIMARY_EMAIL) instanceof String)
                || !EmailValidator.getInstance().isValid((String) requestBody.get(PRIMARY_EMAIL)))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "primary_email is missing or invalid");
        else if (!requestBody.containsKey(ENCRYPTION_CERT) || !(requestBody.get(ENCRYPTION_CERT) instanceof String))
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "encryption_cert is missing or invalid");
        // add encryption certificate expiry
        String url = (String) requestBody.get(ENCRYPTION_CERT);
        requestBody.put(ENCRYPTION_CERT_EXPIRY, jwtUtils.getCertificateExpiry(url));
    }

    private Map<String,Object> getEData(String status, String prevStatus, List<String> props) {
        Map<String,Object> data = new HashMap<>();
        data.put(AUDIT_STATUS, status);
        data.put(PREV_STATUS, prevStatus);
        if(!props.isEmpty())
            data.put(PROPS, props);
        return data;
    }

    //TODO Remove this unnecessary code post moving changes into service layer
    public ResponseEntity<Object> responseHandler(HttpResponse<String> response, String participantCode) throws Exception {
        if (response.getStatus() == 200) {
            if (response.getBody().isEmpty()) {
                return getSuccessResponse("");
            } else {
                if (response.getBody().startsWith("["))
                    return getSuccessResponse(new ParticipantResponse(JSONUtils.deserialize(response.getBody(), ArrayList.class)));
                else
                    return getSuccessResponse(new ParticipantResponse(participantCode));
            }
        } else if (response.getStatus() == 400) {
            throw new ClientException(getErrorMessage(response));
        } else if (response.getStatus() == 401) {
            throw new AuthorizationException(getErrorMessage(response));
        } else if (response.getStatus() == 404) {
            throw new ResourceNotFoundException(getErrorMessage(response));
        } else {
            throw new ServerException(getErrorMessage(response));
        }
    }

    private ResponseEntity<Object> getSuccessResponse(Object response){
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

}
