package org.swasth.common.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ParticipantResponseTest {

    @Test
    public void testParticipantResponseFields() {
        String applicantEmail = "test@example.com";
        String applicantCode = "987654";
        String sponsorCode = "123456";
        String status = "active";
        String createdon = "2022-01-01";
        String updatedon = "2022-02-01";
        ParticipantResponse response = new ParticipantResponse(applicantEmail, applicantCode, sponsorCode, status, createdon, updatedon);
        assertEquals(applicantEmail, response.getApplicantEmail());
        assertEquals(applicantCode, response.getApplicantCode());
        assertEquals(sponsorCode, response.getSponsorCode());
        assertEquals(status, response.getStatus());
        assertEquals(createdon, response.getCreatedon());
        assertEquals(updatedon, response.getUpdatedon());
    }

}