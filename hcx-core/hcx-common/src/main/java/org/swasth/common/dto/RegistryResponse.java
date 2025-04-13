package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.ORGANISATION;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistryResponse {
    private Long timestamp;
    private ResponseError error;
    @JsonProperty("participant_code")
    private String participantCode;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("status")
    private String status;
    private ArrayList<Object> participants;
    private ArrayList<Map<String,Object>> users;


    public RegistryResponse() {
        this.timestamp = System.currentTimeMillis();
        System.out.println("RegistryResponse constructor called");
    }

    public RegistryResponse(ResponseError error) {
        this.timestamp = System.currentTimeMillis();
        System.out.println("Setting timestamp from long RegistryResponse: " + this.timestamp);
        this.error = error;
        System.out.println("RegistryResponse constructor called with error: " + error);
    }

    public RegistryResponse(String value, String entity) {
        System.out.println("RegistryResponse constructor called with entity: " + entity);
        if (StringUtils.equals(entity, ORGANISATION)) {
            this.participantCode = value;
            System.out.println("RegistryResponse constructor called with entity: " + value);
        } else {
            this.timestamp = System.currentTimeMillis();
            this.userId = value;
            System.out.println("RegistryResponse constructor called with entity: " + value + " " + this.timestamp);
        }
        System.out.println("RegistryResponse constructor completed for entity: " + entity);
    }

    public <T> RegistryResponse(List<T> response, String entity) {
        System.out.println("RegistryResponse constructor called with entity: " + response);
        this.timestamp = System.currentTimeMillis();
        System.out.println("Setting timestamp from long RegistryResponse: " + this.timestamp);
        if (StringUtils.equals(entity, ORGANISATION)) {
            this.participants = (ArrayList<Object>) response;
            System.out.println("RegistryResponse constructor called with entity: " + response);
        } else {
            this.users = (ArrayList<Map<String, Object>>) response;
            System.out.println("RegistryResponse constructor called with entity: " + response);
        }
        System.out.println("RegistryResponse constructor completed for entity: " + entity);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        System.out.println("Setting timestamp from long " + timestamp);
        this.timestamp = timestamp;
    }

    // To handle issue of receiving string timestamp from the api call
    public void setTimestamp(String timestamp) {
        System.out.println("Setting timestamp from string: " + timestamp);
        // this.timestamp = OffsetDateTime.parse(timestamp).toInstant().toEpochMilli();
        this.timestamp = System.currentTimeMillis();
    }

    public String getParticipantCode() {
        return participantCode;
    }

    public void setParticipantCode(String participantCode) {
        this.participantCode = participantCode;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Object> getParticipants() {
        return participants;
    }

    public List<Map<String,Object>> getUsers() {
        return users;
    }

    public String getUserId() {
        return userId;
    }

}
