package io.hcxprotocol.utils;

import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.dto.HttpResponse;
import io.hcxprotocol.exception.ServerException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The common utils functionality used in HCX Integrator SDK.
 * <ol>
 *     <li>Generation of authentication token using HCX Gateway and Participant System Credentials.</li>
 *     <li>HCX Gateway Participant Registry Search.</li>
 * </ol>
 */

public class Utils {

    // TODO: In the initial version we are not handling the token caching, it will be handled in the next version
    public static String generateToken() throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded");
        Map<String,Object> fields = new HashMap<>();
        fields.put("client_id", "registry-frontend");
        fields.put("username", HCXIntegrator.getInstance().getUsername());
        fields.put("password", HCXIntegrator.getInstance().getPassword());
        fields.put("grant_type", "password");
        HttpResponse response = HttpUtils.post(HCXIntegrator.getInstance().getAuthBasePath(), headers, fields);
        Map<String, String> responseBody = null;
        responseBody = JSONUtils.deserialize(response.getBody(), Map.class);
        return responseBody.get("access_token");
    }

    public static Map<String,Object> searchRegistry(Object participantCode) throws Exception {
        String filter = "{\"filters\":{\"participant_code\":{\"eq\":\"" + participantCode + "\"}}}";
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, "Bearer " + generateToken());
        HttpResponse response = HttpUtils.post(HCXIntegrator.getInstance().getHCXProtocolBasePath() + "/participant/search", headers, filter);
        Map<String,Object> respMap;
        List<Map<String,Object>> details;
        if (response.getStatus() == 200) {
            respMap = JSONUtils.deserialize(response.getBody(), Map.class);
            details = (List<Map<String, Object>>) respMap.get(Constants.PARTICIPANTS);
        } else {
            throw new ServerException("Error in fetching the participant details" + response.getStatus());
        }
        return !details.isEmpty() ? details.get(0) : new HashMap<>();
    }

}
