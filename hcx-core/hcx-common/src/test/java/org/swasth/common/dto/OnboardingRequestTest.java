package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.swasth.common.utils.Constants.*;

public class OnboardingRequestTest {

    @Test
    public void testOnboardRequestWithOTPPhoneBody() throws Exception {
        ArrayList<Map<String,Object>> body = JSONUtils.deserialize("[ { \"type\":\"phone-otp-validation\" ,\"primary_mobile\":\"8522875123\",\"otp\": \"443535\" } , { \"type\":\"email-otp-validation\" ,\"primary_email\": \"testhcx20@yopmail.com\", \"otp\": \"282515\" }]",ArrayList.class);
        OnboardRequest request = new OnboardRequest(body);
        assertEquals("8522875123", request.getBody().get(PRIMARY_MOBILE));
    }
    @Test
    public void testOnboardRequestWithOTPEmailBody() throws Exception {
        ArrayList<Map<String,Object>> body = JSONUtils.deserialize("[ { \"type\":\"email-otp-validation\" ,\"primary_email\": \"testhcx20@yopmail.com\", \"otp\": \"282515\" },{ \"type\":\"phone-otp-validation\" ,\"primary_mobile\":\"8522875123\",\"otp\": \"443535\" }]",ArrayList.class);
        OnboardRequest request = new OnboardRequest(body);
        assertEquals("testhcx20@yopmail.com", request.getBody().get(PRIMARY_EMAIL));
    }

    @Test
    public void testOnboardRequestWithPayorCodeBody() throws Exception {
        ArrayList<Map<String,Object>> body = JSONUtils.deserialize("[ { \"verifier_code\": \"testprovider1.apollo@swasth-hcx-dev\", \"participant\": { \"primary_email\": \"testhcx15@yopmail.com\", \"primary_mobile\": \"8522875773\", \"roles\": [ \"provider\" ], \"participant_name\": \"onboard test1\" } } ]", ArrayList.class);
        OnboardRequest request = new OnboardRequest(body);
        Map<String,Object> participantMap = (Map<String, Object>) request.getBody().get("participant");
        assertEquals("testprovider1.apollo@swasth-hcx-dev", request.getBody().get(VERIFIER_CODE));
        assertEquals("testhcx15@yopmail.com", participantMap.get(PRIMARY_EMAIL));
    }


}
