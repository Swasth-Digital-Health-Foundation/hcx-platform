package org.swasth.common.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OnboardResponseTest {

    @Test
    public void testParticipantResponseFields() {
        String applicantCode = "987654";
        String verifierCode = "123456";
        String result = "accepted";
        OnboardResponse response = new OnboardResponse(applicantCode, verifierCode);
        response.setResult("accepted");
        assertEquals(result,response.getResult());
    }
}
