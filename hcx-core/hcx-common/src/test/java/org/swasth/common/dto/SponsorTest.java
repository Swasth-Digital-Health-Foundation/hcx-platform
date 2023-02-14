package org.swasth.common.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SponsorTest {


    @Test
    public void testParticipantResponseFields() {
        String applicantEmail = "test@example.com";
        String applicantCode = "987654";
        String verifierCode = "123456";
        String status = "active";
        Long createdon = (long) (2022 - 1 - 1);
        Long updatedon = (long) (2022 - 2 - 1);
        Sponsor response = new Sponsor(applicantEmail, applicantCode, verifierCode, status, createdon, updatedon);
        assertEquals(applicantEmail, response.getApplicantEmail());
        assertEquals(applicantCode, response.getApplicantCode());
        assertEquals(verifierCode, response.getVerifierCode());
        assertEquals(status, response.getStatus());
        assertEquals(createdon, response.getCreatedon());
        assertEquals(updatedon, response.getUpdatedon());
    }
}
