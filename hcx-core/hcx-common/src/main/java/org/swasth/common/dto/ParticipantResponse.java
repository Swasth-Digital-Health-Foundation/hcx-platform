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
    private String applicant_email;
    private String applicant_code;
    private String sponsor_code;
    private String status;
    private String createdon;
    private String updatedon;

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
    public ParticipantResponse(String applicant_email, String applicant_code, String sponsor_code, String status, String createdon, String updatedon) {
        this.applicant_email = applicant_email;
        this.applicant_code = applicant_code;
        this.sponsor_code = sponsor_code;
        this.status = status;
        this.createdon = createdon;
        this.updatedon = updatedon;
    }
    public String getApplicantEmail() {
        return applicant_email;
    }
    public String getApplicant_code() {
        return applicant_code;
    }
    public String getSponsorCode() {
        return sponsor_code;
    }
    public String getStatus() {
        return status;
    }
    public String getUpdatedon() {
        return updatedon;
    }
    public String getCreatedon() {
        return createdon;
    }
    public String getSponsor_code() {
        return sponsor_code;
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
