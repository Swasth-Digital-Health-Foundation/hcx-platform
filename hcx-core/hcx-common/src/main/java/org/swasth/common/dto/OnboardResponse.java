package org.swasth.common.dto;

public class OnboardResponse {
    private final String primary_email;
    private final String primary_mobile;
    private final String status;
    private final long createdOn;
    private final long updatedOn;
    private final String state;

    public OnboardResponse(String primary_email, String primary_mobile, String status, long createdOn, long updatedOn, String state) {
        this.primary_email = primary_email;
        this.primary_mobile = primary_mobile;
        this.status = status;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.state = state;
    }

    public String getPrimary_email() { return primary_email;}
    public String getPrimary_mobile() { return primary_mobile;}
    public String getStatus() { return status;}
    public long getCreatedOn() { return createdOn;}
    public long getUpdatedOn() { return updatedOn;}
    public String getState(){ return state;}
}
