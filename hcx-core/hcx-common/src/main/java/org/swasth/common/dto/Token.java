package org.swasth.common.dto;

import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Map;

public class Token {

    private Map<String,Object> headers;
    private Map<String,Object> payload;

    public Token(String token) throws Exception {
        String tokenValues[] = token.split("\\.");
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
        return (String) ((Map<String,Object>) payload.get("realm_access")).get("preferred_username");
    }
}
