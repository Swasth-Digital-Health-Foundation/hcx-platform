package org.swasth.hcx.controllers.v1;

import kong.unirest.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.HttpUtils;
import org.swasth.common.JsonUtils;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.hcx.controllers.BaseController;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping(value = "/participant")
public class ParticipantController  extends BaseController {
    @Value("${registry.basePath}")
    private String registryUrl;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Object> participantCreate(@RequestBody Map<String, Object> requestBody) throws Exception {
        String url =  registryUrl + "/api/v1/Organisation/invite";
        HttpResponse response = HttpUtils.post(url, JsonUtils.serialize(requestBody),new HashMap<>());
        if (response != null && response.getStatus() == 200) {
            Map<String, Object> result = JsonUtils.deserialize((String) response.getBody(), HashMap.class);
            String participantCode = (String) ((Map<String, Object>) ((Map<String, Object>) result.get("result")).get("Organisation")).get("participant_code");
            ParticipantResponse resp = new ParticipantResponse(participantCode);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } else if(response.getStatus() == 400) {
            return new ResponseEntity<>(errorResponse(response), HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<>(errorResponse(response), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Object> participantSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        String url =  registryUrl + "/api/v1/Organisation/search";
        HttpResponse response = HttpUtils.post(url, JsonUtils.serialize(requestBody), new HashMap<>());
        ArrayList<Object> result = JsonUtils.deserialize((String) response.getBody(), ArrayList.class);
        ParticipantResponse resp = new ParticipantResponse();
        if(result.isEmpty()) {
            ResponseError error = new ResponseError(null,"Resource Not Found",null);
            resp.setError(error);
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        } else{
            resp.setParticipants(result);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<Object> participantUpdate(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) throws Exception {
        String url =  registryUrl + "/api/v1/Organisation/"+requestBody.get("participant_code");
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization",header.get("Authorization").get(0));
        HttpResponse response = HttpUtils.put(url, JsonUtils.serialize(requestBody), headersMap);
        if (response.getStatus() == 200) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else if(response.getStatus() == 401){
            return new ResponseEntity<>(errorResponse(response), HttpStatus.UNAUTHORIZED);
        }
        else if(response.getStatus() == 404){
            return new ResponseEntity<>(errorResponse(response), HttpStatus.NOT_FOUND);
        }
        else {
            return new ResponseEntity<>(errorResponse(response), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ParticipantResponse errorResponse(HttpResponse response) throws Exception {
        Map<String, Object> result = JsonUtils.deserialize((String) response.getBody(), HashMap.class);
        String message = (String) ((Map<String, Object>) result.get("params")).get("errmsg");
        ResponseError error = new ResponseError(null,message,null);
        ParticipantResponse resp = new ParticipantResponse(error);
        return resp;
    }
}
