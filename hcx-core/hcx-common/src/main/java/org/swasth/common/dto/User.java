package org.swasth.common.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import static org.swasth.common.utils.Constants.*;

public class User {

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_id")
    private String userId;

    private String email;

    private String mobile;

    private ArrayList<Map<String,Object>> tenantRoles;
    
    @JsonProperty("created_by")
    private String createdBy;

    public User(String userName, String email, String mobile, String createdBy) {
        this.userName = userName;
        this.email = email;
        this.mobile = mobile;
        this.createdBy = createdBy;
    }

    public String getUsername(){
        return this.userName;
    }

    public String getEmail(){
        return this.email;
    }

    public String getUserId(){
        return this.userId;
    }

    public ArrayList<Map<String,Object>> getTenantRoles(){
        return tenantRoles;
    }

    public void addTenantRole(String participantCode, String role){
        tenantRoles = new ArrayList<>();
        Map<String,Object> tenantRole = new HashMap<>();
        tenantRole.put(PARTICIPANT_CODE, participantCode);
        tenantRole.put(ROLE, ADMIN);
        tenantRoles.add(tenantRole);
    }

    public void setUserId(String userId){
        this.userId = userId;
    }
    
}
