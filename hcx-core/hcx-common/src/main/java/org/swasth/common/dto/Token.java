package org.swasth.common.dto;

import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Token {

    private Map<String,Object> headers;
    private Map<String,Object> payload;
    private String token;

    public Token(String rawtoken) throws Exception {
        if(rawtoken.contains("Bearer")){
            this.token = rawtoken.replace("Bearer ", "");
        } else {
            this.token = rawtoken;
        }
        String tokenValues[] = token.split("\\.");
        headers = JSONUtils.decodeBase64String(tokenValues[0], Map.class);
        payload = JSONUtils.decodeBase64String(tokenValues[1], Map.class);
    }
    
    public String getToken(){
        return token;
    }

    public Map<String,Object> getHeaders() {
        return headers;
    }

    public Map<String,Object> getPayload() {
        return payload;
    }

    public ArrayList<String> getRoles() {
        return (ArrayList<String>) ((Map<String,Object>) payload.get("realm_access")).getOrDefault("roles", new ArrayList<>());
    }

    public String getUsername() {
        return (String) payload.get("preferred_username");
    }

    public String getEntityType() {
        if (payload.containsKey("entity")) {
            return ((List<String>) payload.get("entity")).get(0);
        }
        return "";
    }

    public String getParticipantCode() {
        return (String) payload.getOrDefault("participant_code", "");
    }

    public String getUserId() {
        return (String) payload.getOrDefault("user_id", "");
    }

    public ArrayList<Map<String,String>> getTenantRoles() {
        return (ArrayList<Map<String,String>>) ((Map<String,Object>) payload.get("realm_access")).getOrDefault("tenant_roles", new ArrayList<>());
    }

    public String getSubject() {
        return (String) payload.get("sub");
    }

    public String getRole() {
        return (String) payload.get("role");
    }

    public String getInvitedBy() {
        return (String) payload.getOrDefault(Constants.INVITED_BY, "");
    }
}
