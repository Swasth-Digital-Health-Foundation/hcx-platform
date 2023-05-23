package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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
    private ArrayList<Object> users;

    public RegistryResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public RegistryResponse(ResponseError error) {
        this.timestamp = System.currentTimeMillis();
        this.error = error;
    }

    public RegistryResponse(String value, String entity) {
        if (StringUtils.equals(entity,ORGANISATION)) {
            this.participantCode = value;
        } else {
            this.timestamp = System.currentTimeMillis();
            this.userId = value;
        }
    }

    public <T> RegistryResponse(ArrayList<T> response, String entity) {
        this.timestamp = System.currentTimeMillis();
        if (StringUtils.equals(entity,ORGANISATION)) {
            this.participants = (ArrayList<Object>) response;
        } else {
            this.users = (ArrayList<Object>) response;
        }
    }

    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getParticipantCode() { return participantCode; }
    public void setParticipantCode(String correlationId) {
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
    public ArrayList<Object> getParticipants(){return participants;}
    public ArrayList<Object> getUsers(){return users;}
    public void setParticipants(ArrayList<Object> participants){this.participants = participants;}

}
