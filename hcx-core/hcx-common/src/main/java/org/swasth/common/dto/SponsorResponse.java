package org.swasth.common.dto;

public class SponsorResponse {

    private String applicantEmail;
    private final String applicantCode;
    private final String sponsorCode;
    private final String status;
    private final String createdon;
    private final String updatedon;

    public SponsorResponse(String applicantEmail, String applicantCode, String sponsorCode, String status, String createdon, String updatedon) {
        this.applicantEmail = applicantEmail;
        this.applicantCode = applicantCode;
        this.sponsorCode = sponsorCode;
        this.status = status;
        this.createdon = createdon;
        this.updatedon = updatedon;
    }
    public String getApplicantEmail() {
        return applicantEmail;
    }
    public String getSponsorCode() {
        return sponsorCode;
    }

    public String getApplicantCode() {
        return applicantCode;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedon() {
        return createdon;
    }

    public String getUpdatedon() {
        return updatedon;
    }
}
