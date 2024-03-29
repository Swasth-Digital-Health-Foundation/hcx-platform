package org.swasth.common.dto;

public class Sponsor {

    private final String applicantEmail;
    private final String applicantCode;
    private final String verifierCode;
    private final String status;
    private final Long createdon;
    private final Long updatedon;

    public Sponsor(String applicantEmail, String applicantCode, String verifierCode, String status, Long createdon, Long updatedon) {
        this.applicantEmail = applicantEmail;
        this.applicantCode = applicantCode;
        this.verifierCode = verifierCode;
        this.status = status;
        this.createdon = createdon;
        this.updatedon = updatedon;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }
    public String getVerifierCode() {
        return verifierCode;
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
