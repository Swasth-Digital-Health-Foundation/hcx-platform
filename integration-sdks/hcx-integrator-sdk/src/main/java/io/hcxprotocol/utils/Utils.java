package io.hcxprotocol.utils;

import io.hcxprotocol.dto.HCXIntegrator;
import kong.unirest.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // TODO: In initial version we are not handling the token caching, it will be handled in next version
    public static String generateToken() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded");
        Map<String,Object> fields = new HashMap<>();
        fields.put("client_id", "registry-frontend");
        fields.put("username", HCXIntegrator.getInstance().getUsername());
        fields.put("password", HCXIntegrator.getInstance().getPassword());
        fields.put("grant_type", "password");
        HttpResponse<String> response = HttpUtils.post(HCXIntegrator.getInstance().getAuthBasePath(), headers, fields);
        Map<String, String> responseBody = JSONUtils.deserialize(response.getBody(), Map.class);
        return responseBody.get("access_token");
    }

    public static Map<String,Object> searchRegistry(Object participantCode) throws Exception {
        String filter = "{\"filters\":{\"participant_code\":{\"eq\":\"" + participantCode + "\"}}}";
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, "Bearer " + generateToken());
        HttpResponse<String> response = HttpUtils.post(HCXIntegrator.getInstance().getHCXProtocolBasePath() + "/participant/search", headers, filter);
        List<Map<String,Object>> resArray = JSONUtils.deserialize(response.getBody(), ArrayList.class);
        return resArray.get(0);
    }

}
