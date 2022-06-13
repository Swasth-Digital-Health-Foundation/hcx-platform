package org.swasth.hcx.controllers;

import kong.unirest.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.*;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.SlugUtils;

import java.security.SecureRandom;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

@RestController()
public class ParticipantController  extends BaseController {

    @Value("${registry.basePath}")
    private String registryUrl;

    @PostMapping(PARTICIPANT_CREATE)
    public ResponseEntity<Object> participantCreate(@RequestHeader HttpHeaders header,
        @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            validateParticipant(requestBody);
            String participantCode = SlugUtils.makeSlug(((ArrayList<String>) requestBody.get(ROLES)).get(0) + " " + requestBody.get(PARTICIPANT_NAME));
            String updatedParticipantCode = participantCode;
            while(isParticipantCodeExists(updatedParticipantCode)){
                updatedParticipantCode = participantCode + "-" + new SecureRandom().nextInt(1000);
            }
            requestBody.put(PARTICIPANT_CODE, updatedParticipantCode);
            String url =  registryUrl + "/api/v1/Organisation/invite";
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
            if (response.getStatus() == 200) {
                ParticipantResponse resp = new ParticipantResponse(updatedParticipantCode);
                return new ResponseEntity<>(resp, HttpStatus.OK);
            } else if(response.getStatus() == 400) {
                throw new ClientException(getErrorMessage(response));
            } else {
                throw new ServerException(getErrorMessage(response));
            }
        } catch (Exception e) {
            return exceptionHandler(null, new Response(), e);
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

    @PostMapping(PARTICIPANT_SEARCH)
    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            String url =  registryUrl + "/api/v1/Organisation/search";
            Map<String,Object> filters = (Map<String, Object>) requestBody.get(FILTERS);
            if (filters.containsKey(PARTICIPANT_CODE)) {
              filters.put(OSID, filters.get(PARTICIPANT_CODE));
              filters.remove(PARTICIPANT_CODE);
            }
            Map<String,Object> updatedRequestBody = new HashMap<>(Collections.singletonMap(FILTERS, filters));
            HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(updatedRequestBody), new HashMap<>());
            if (response.getStatus() == 200) {
                ArrayList<Object> result = JSONUtils.deserialize(response.getBody(), ArrayList.class);
                if (!result.isEmpty()) {
                    for (Object obj: result) {
                        Map<String, Object> objMap = (Map<String, Object>) obj;
                        objMap.put(PARTICIPANT_CODE, objMap.get(OSID));
                        objMap.remove(OSID);
                    }
                }
                return new ResponseEntity<>(new ParticipantResponse(result), HttpStatus.OK);
            } else {
                throw new ServerException(getErrorMessage(response));
            }
        } catch (Exception e) {
            return exceptionHandler(null, new Response(), e);
        }
    }

    @PostMapping(PARTICIPANT_UPDATE)
    public ResponseEntity<Object> participantUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            validateParticipant(requestBody);
            String url = registryUrl + "/api/v1/Organisation/" + requestBody.get(PARTICIPANT_CODE);
            requestBody.remove(PARTICIPANT_CODE);
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
            HttpResponse<String> response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
            if (response.getStatus() == 200) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else if (response.getStatus() == 401) {
                throw new AuthorizationException(getErrorMessage(response));
            } else if (response.getStatus() == 404) {
                throw new ResourceNotFoundException(getErrorMessage(response));
            } else {
                throw new ServerException(getErrorMessage(response));
            }
        } catch (Exception e) {
            return exceptionHandler(null, new Response(), e);
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
        else if (!requestBody.containsKey(PARTICIPANT_NAME) || !(requestBody.get(PARTICIPANT_NAME) instanceof String) || ((String) requestBody.get(PARTICIPANT_NAME)).isEmpty())
            throw new ClientException(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "participant_name property cannot be null, empty or other than 'String'");
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

}
