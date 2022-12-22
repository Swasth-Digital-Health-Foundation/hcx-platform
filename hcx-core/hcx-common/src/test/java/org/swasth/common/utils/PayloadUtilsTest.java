package org.swasth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.swasth.common.utils.Constants.RECIPIENTDETAILS;
import static org.swasth.common.utils.Constants.SENDERDETAILS;


public class PayloadUtilsTest {


    @Test
    public void removeParticipantDetailsTest() throws JsonProcessingException {
        Map<String ,Object> output = new HashMap<>();
        output.put(SENDERDETAILS,"senderDetails");
        output.put(RECIPIENTDETAILS,"recipientDetails");
        Map<String,Object> result = PayloadUtils.removeParticipantDetails(output);
        assertNull(result.get(SENDERDETAILS));
        assertNull(result.get(RECIPIENTDETAILS));
    }
}
