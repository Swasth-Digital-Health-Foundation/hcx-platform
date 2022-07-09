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
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.SlugUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.redis.cache.RedisCache;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

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

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> participantCreate(@RequestHeader HttpHeaders header,
        @RequestBody Map<String, Object> requestBody) throws Exception {
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
            return responseHandler(response, participantCode);
        } catch (Exception e) {
            return exceptionHandler(null, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            String url =  registryUrl + "/api/v1/Organisation/search";
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(null, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_UPDATE)
    public ResponseEntity<Object> participantUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            validateParticipant(requestBody);
            String participantCode = (String) requestBody.get(PARTICIPANT_CODE);
            ResponseEntity<Object> searchResponse = participantSearch(JSONUtils.deserialize("{ \"filters\": { \"participant_code\": { \"eq\": \" " + participantCode + "\" } } }", Map.class));
            ParticipantResponse participantResponse = (ParticipantResponse) Objects.requireNonNull(searchResponse.getBody());
            if(participantResponse.getParticipants().isEmpty())
                throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, "Please provide valid participant code");
            String url = registryUrl + "/api/v1/Organisation/" + ((Map<String,Object>) participantResponse.getParticipants().get(0)).get(OSID);
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
            if (response.getStatus() == 200) {
                if(redisCache.isExists(participantCode))
                    redisCache.delete(participantCode);
            }
            return responseHandler(response, null);
        } catch (Exception e) {
            return exceptionHandler(null, new Response(), e);
        }
    }

    private ResponseEntity<Object> responseHandler(HttpResponse<String> response, String participantCode) throws Exception {
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

    private String generateParticipantCode(String role, String participantName){
        return role + fieldSeparator + participantName + "@" + hcxInstanceName;
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

    private void validateParticipant(Map<String, Object> requestBody) throws ClientException {
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
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "primary_email does not exist or invalid");
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

}
