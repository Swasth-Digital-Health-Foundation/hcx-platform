package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantResponse {
    private Long timestamp;
    private ResponseError error;
    @JsonProperty("participant_code")
    private String participantCode;
    private ArrayList<Object> participants;

    public ParticipantResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ParticipantResponse(ResponseError error) {
        this.timestamp = System.currentTimeMillis();
        this.error = error;
    }

    public ParticipantResponse(String participantCode) {
        this.participantCode = participantCode;
    }


    public ParticipantResponse(ArrayList<Object> participants) {
        this.timestamp = System.currentTimeMillis();
        this.participants = participants;
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

    public ArrayList<Object> getParticipants(){return participants;}
    public void setParticipants(ArrayList<Object> participants){this.participants = participants;}

}
