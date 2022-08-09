package org.swasth.hcx.service;

import kong.unirest.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.exception.AuthorizationException;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ResourceNotFoundException;
import org.swasth.common.exception.ServerException;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ParticipantService {

    @Value("${registry.basePath}")
    public String registryUrl;

    public ResponseEntity<Object> search(Map<String, Object> requestBody) throws Exception {
        String url =  registryUrl + "/api/v1/Organisation/search";
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
        return responseHandler(response, null);
    }

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
