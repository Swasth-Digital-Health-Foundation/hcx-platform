package org.swasth.common.dto;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ParticipantResponseTest {

    @Test
    public void ParticipantResponse() {
        ParticipantResponse response = new ParticipantResponse();
        assertNotNull(response.getTimestamp());
        assertNull(response.getError());
        assertNull(response.getParticipantCode());
        assertNull(response.getParticipants());
        assertNull(response.getApplicantEmail());
        assertNull(response.getApplicantCode());
        assertNull(response.getSponsorCode());
        assertNull(response.getStatus());
        assertNull(response.getCreatedon());
        assertNull(response.getUpdatedon());

        String participantCode = "123456";
        response = new ParticipantResponse(participantCode);
        assertNotNull(response.getTimestamp());
        assertNull(response.getError());
        assertEquals(participantCode, response.getParticipantCode());
        assertNull(response.getParticipants());
        assertNull(response.getApplicantEmail());
        assertNull(response.getApplicantCode());
        assertNull(response.getSponsorCode());
        assertNull(response.getStatus());
        assertNull(response.getCreatedon());
        assertNull(response.getUpdatedon());

        ArrayList<Object> participants = new ArrayList<>();
        response = new ParticipantResponse(participants);
        assertNotNull(response.getTimestamp());
        assertNull(response.getError());
        assertNull(response.getParticipantCode());
        assertEquals(participants, response.getParticipants());
        assertNull(response.getApplicantEmail());
        assertNull(response.getApplicantCode());
        assertNull(response.getSponsorCode());
        assertNull(response.getStatus());
        assertNull(response.getCreatedon());
        assertNull(response.getUpdatedon());

        String applicantEmail = "test@example.com";
        String applicantCode = "987654";
        String sponsorCode = "123456";
        String status = "active";
        String createdon = "2022-01-01";
        String updatedon = "2022-02-01";
        response = new ParticipantResponse(applicantEmail, applicantCode, sponsorCode, status, createdon, updatedon);
        assertNotNull(response.getTimestamp());
        assertNull(response.getError());
        assertNull(response.getParticipantCode());
        assertNull(response.getParticipants());
        assertEquals(applicantEmail, response.getApplicantEmail());
        assertEquals(applicantCode, response.getApplicantCode());
        assertEquals(sponsorCode, response.getSponsorCode());
        assertEquals(status, response.getStatus());

    }
}