package io.hcxprotocol.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hcxprotocol.dto.HCXIntegrator;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static String generateToken() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded");
        Map<String,Object> fields = new HashMap<>();
        fields.put("client_id", "registry-frontend");
        fields.put("username", HCXIntegrator.getInstance().getKeycloakUsername());
        fields.put("password", HCXIntegrator.getInstance().getKeycloakPassword());
        fields.put("grant_type", "password");
        HttpResponse<String> response = HttpUtils.post(HCXIntegrator.getInstance().getHCXBaseUrl(), headers, fields);
        Map<String, String> responseBody = JSONUtils.deserialize(response.getBody(), Map.class);
        return responseBody.get("access_token");
    }

    public static Map<String,Object> searchRegistry(Object participantCode) throws Exception {
        String filter = "{\"filters\":{\"participant_code\":{\"eq\":\"" + participantCode + "\"}}}";
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, "Bearer " + generateToken());
        HttpResponse<String> response = HttpUtils.post(HCXIntegrator.getInstance().getHCXBaseUrl() + "/participant/search", headers, filter);
        List<Map<String,Object>> resArray = JSONUtils.deserialize(response.getBody(), ArrayList.class);
        return resArray.get(0);
    }

}
