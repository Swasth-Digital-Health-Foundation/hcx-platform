package org.swasth.common.dto;

import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Token {

    private Map<String,Object> headers;
    private Map<String,Object> payload;

    public Token(String token) throws Exception {
        String removeBearer = token.replace("Bearer ", "");
        String tokenValues[] = removeBearer.split("\\.");
        headers = JSONUtils.decodeBase64String(tokenValues[0], Map.class);
        payload = JSONUtils.decodeBase64String(tokenValues[1], Map.class);
    }
    public Map<String,Object> getHeaders() {
        return headers;
    }

    public Map<String,Object> getPayload() {
        return payload;
    }

    public ArrayList<String> getRoles() {
        return (ArrayList<String>) ((Map<String,Object>) payload.get("realm_access")).get("roles");
    }

    public String getUsername() {
        return (String) payload.get("preferred_username");
    }

    public String getEntityType() {
        return (String) ((List<String>) payload.get("entity")).get(0);
    }

    public String getParticipantCode() {
        return (String) payload.getOrDefault("participant_code", "");
    }

    public String getUserId() {
        return (String) payload.getOrDefault("user_id", "");
    }
}
