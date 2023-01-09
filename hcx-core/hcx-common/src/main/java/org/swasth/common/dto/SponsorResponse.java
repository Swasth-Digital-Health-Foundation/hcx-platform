package org.swasth.common.dto;

public class SponsorResponse {

    private final String applicantEmail;
    private final String applicantCode;
    private final String sponsorCode;
    private final String status;
    private final Long createdon;
    private final Long updatedon;

    public SponsorResponse(String applicantEmail, String applicantCode, String sponsorCode, String status, Long createdon, Long updatedon) {
        this.applicantEmail = applicantEmail;
        this.applicantCode = applicantCode;
        this.sponsorCode = sponsorCode;
        this.status = status;
        this.createdon = Long.valueOf(createdon);
        this.updatedon = Long.valueOf(updatedon);
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

    public Long getCreatedon() {
        return createdon;
    }

    public Long getUpdatedon() {
        return updatedon;
    }
}
