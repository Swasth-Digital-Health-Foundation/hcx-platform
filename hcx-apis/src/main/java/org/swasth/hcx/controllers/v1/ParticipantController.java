package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseController;

import java.util.*;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(value = "/v1/participant")
public class ParticipantController  extends BaseController {

    @Value("${registry.basePath}")
    private String registryUrl;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Object> participantCreate(@RequestHeader HttpHeaders header,
        @RequestBody Map<String, Object> requestBody) throws Exception {
        if(((ArrayList) requestBody.get(ROLES)).contains(PAYOR) && !requestBody.containsKey(SCHEME_CODE)) {
            return new ResponseEntity<>(errorResponse(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "scheme_code is missing", null), HttpStatus.BAD_REQUEST);
        }
        if (!((ArrayList) requestBody.get(ROLES)).contains(PAYOR) && requestBody.containsKey(SCHEME_CODE)) {
            return new ResponseEntity<>(errorResponse(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS, "unknown property, 'scheme_code' is not allowed", null), HttpStatus.BAD_REQUEST);
        }
        if (validateEndpointUrl(requestBody))
            return new ResponseEntity<>(errorResponse(ErrorCodes.ERR_INVALID_PAYLOAD, "end point url should not be the HCX Gateway/APIs URL", null), HttpStatus.BAD_REQUEST);

        String url =  registryUrl + "/api/v1/Organisation/invite";
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, header.get(AUTHORIZATION).get(0));
        HttpResponse response = HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
        if (response != null && response.getStatus() == 200) {
            Map<String, Object> result = JSONUtils.deserialize((String) response.getBody(), HashMap.class);
            String participantCode = (String) ((Map<String, Object>) ((Map<String, Object>) result.get("result"))
                .get("Organisation")).get(OSID);
            ParticipantResponse resp = new ParticipantResponse(participantCode);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } else if(response.getStatus() == 400) {
            return new ResponseEntity<>(getError(response), HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<>(getError(response), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        String url =  registryUrl + "/api/v1/Organisation/search";
        Map<String,Object> filters = (Map<String, Object>) requestBody.get(FILTERS);
        if (filters.containsKey(PARTICIPANT_CODE)) {
          filters.put(OSID, filters.get(PARTICIPANT_CODE));
          filters.remove(PARTICIPANT_CODE);
        }
        Map<String,Object> updatedRequestBody = new HashMap<>(Collections.singletonMap(FILTERS, filters));
        HttpResponse response = HttpUtils.post(url, JSONUtils.serialize(updatedRequestBody), new HashMap<>());
        ArrayList<Object> result = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
        if(result.isEmpty()) {
            return new ResponseEntity<>(errorResponse(null,"Resource Not Found",null), HttpStatus.NOT_FOUND);
          } else {
            for (Object obj: result) {
                Map<String, Object> objMap = (Map<String, Object>) obj;
                objMap.put(PARTICIPANT_CODE, "1-" + objMap.get(OSID));
                objMap.remove(OSID);
            }
            return new ResponseEntity<>(new ParticipantResponse(result), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<Object> participantUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) throws Exception {
      String url = registryUrl + "/api/v1/Organisation/" + requestBody.get(PARTICIPANT_CODE);
        if (validateEndpointUrl(requestBody))
            return new ResponseEntity<>(errorResponse(ErrorCodes.ERR_INVALID_PAYLOAD, "end point url should not be the HCX Gateway/APIs URL", null), HttpStatus.BAD_REQUEST);
        requestBody.remove(PARTICIPANT_CODE);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION,header.get(AUTHORIZATION).get(0));
        HttpResponse response = HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
        if (response.getStatus() == 200) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else if(response.getStatus() == 401){
            return new ResponseEntity<>(getError(response), HttpStatus.UNAUTHORIZED);
        }
        else if(response.getStatus() == 404){
            return new ResponseEntity<>(getError(response), HttpStatus.NOT_FOUND);
        }
        else {
            return new ResponseEntity<>(getError(response), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ParticipantResponse getError(HttpResponse response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize((String) response.getBody(), HashMap.class);
        String message = (String) ((Map<String, Object>) result.get("params")).get("errmsg");
        return errorResponse(null, message, null);
    }

    private ParticipantResponse errorResponse(ErrorCodes code, String message, Throwable trace){
        ResponseError error = new ResponseError(code, message, trace);
        ParticipantResponse resp = new ParticipantResponse(error);
        return resp;
    }

    private boolean validateEndpointUrl(@RequestBody Map<String, Object> requestBody) {
        List<String> notAllowedUrls = env.getProperty(HCX_NOT_ALLOWED_URLS, List.class, new ArrayList<String>());
        return notAllowedUrls.contains(requestBody.get(ENDPOINT_URL));
    }

}
