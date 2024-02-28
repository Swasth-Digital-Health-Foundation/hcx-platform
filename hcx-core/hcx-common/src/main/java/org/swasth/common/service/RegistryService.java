package org.swasth.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryService {

    private final String registryUrl;

    public RegistryService(String registryUrl){
        this.registryUrl = registryUrl;
    }

    public List<Map<String,Object>> getDetails(String requestBody) throws Exception {
        String url = registryUrl + "/api/v1/Organisation/search";
        HttpResponse<String> response;
        try {
            response = HttpUtils.post(url, requestBody, new HashMap<>());
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.ERR_SERVICE_UNAVAILABLE, "Error connecting to registry service: " + e.getMessage());
        }
        List<Map<String,Object>> details;
        if (response.getStatus() == 200) {
            details = JSONUtils.deserialize(response.getBody(), List.class);
        } else {
            throw new Exception("Error in fetching the participant details" + response.getBody());
        }
        return details;
    }

    public boolean updateStatusOnCertificateRevocation(String osid) throws JsonProcessingException, ServerException {
        String url = registryUrl + "/api/v1/Organisation/" + osid;
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", "Inactive");
        HttpResponse<String> response;
        try {
            response = HttpUtils.put(url, JSONUtils.serialize(requestBody), new HashMap<>());
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.ERR_SERVICE_UNAVAILABLE, "Error connecting to registry service: " + e.getMessage());
        }
        return response.getStatus() == 200;
    }
}
