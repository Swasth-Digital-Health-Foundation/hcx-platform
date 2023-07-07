package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.swasth.common.utils.Constants.*;

public class OnboardingRequestTest {

    @Test
    public void testOnboardRequestWithOTPPhoneBody() throws Exception {
        ArrayList<Map<String, Object>> body = JSONUtils.deserialize("[ { \"type\":\"onboard-through-jwt\" ,\"jwt\":\"eyJ0eXAiOiJqd3QiLCJhbGciOiJSUzI1NiJ9.eyJ2ZXJpZmllcl9jb2RlIjoidGVzdHByb3ZpZGVyMS5hcG9sbG9Ac3dhc3RoLWhjeC1kZXYiLCJhcHBsaWNhbnRfY29kZSI6InBjcHRAMDEiLCJleHAiOjE2NzE5NzE1MDY4MjcsImlhdCI6MTY3MTg4NTEwNjgyNywianRpIjoiMDUxM2Y4NjUtOGJiOS00Yjg5LWFjZmUtMzZjNTVlMzdmNjVkIn0.MWgffbA0ZZYlswsyEY9bprQ016rHvrbLerRuhfwXD3L2BFJ7c1dsR8NrHl93cpPvlP8rNcZ3PQ76cXrnvYLtnZoGtCD-R1_K2mJt8EeWGgCyhKXLqI9UqgIXodevvezvOut4Ki5qmKBXBuoqzRzHAIAI-yoqacY_aah--1lDXwOG0N67HCugV_MP9aWRltirGKvIEvDane0wztZy-0_Njl4Q3BYcMjt_zZJtAnn-bW59mUFTfFNQi3ttmFMrF7HvOp-XYeHriDic_iVBKuKY9KVH9p2L6PVxJgfNSrFYEo3_V2dApesiORDiUn307wIKT3pJ6IR9CZqut_7YYQQ-ZA\",\"otp\": \"443535\" }]",ArrayList.class);
        OnboardRequest request = new OnboardRequest(body);
        assertEquals("onboard-through-jwt", request.getType());
    }
    @Test
    public void testOnboardRequestWithOTPEmailBody() throws Exception {
        ArrayList<Map<String, Object>> body = JSONUtils.deserialize("[ { \"type\":\"onboard-through-verifier\" ,\"verifier_code\": \"testprovider1.apollo@swasth-hcx-dev\", \"applicant_code\": \"pcpt@01\"  }]", ArrayList.class);
        OnboardRequest request = new OnboardRequest(body);
        assertEquals("testprovider1.apollo@swasth-hcx-dev",request.getVerifierCode());
        assertEquals("pcpt@01",request.getApplicantCode());
    }

    @Test
    public void testOnboardRequestWithPayorCodeBody() throws Exception {
        ArrayList<Map<String,Object>> body = JSONUtils.deserialize("[ { \"type\":\"\" ,\"verifier_code\": \"testprovider1.apollo@swasth-hcx-dev\", \"participant\": { \"primary_email\": \"testhcx15@yopmail.com\", \"primary_mobile\": \"8522875773\", \"roles\": [ \"provider\" ], \"participant_name\": \"onboard test1\" } } ]", ArrayList.class);
        OnboardRequest request = new OnboardRequest(body);
        Map<String,Object> participantMap = (Map<String, Object>) request.getBody().get("participant");
        assertEquals("testprovider1.apollo@swasth-hcx-dev", request.getBody().get(VERIFIER_CODE));
        assertEquals("testhcx15@yopmail.com", participantMap.get(PRIMARY_EMAIL));
    }


    @Test
    public void testOnboardRequestVerifier() throws Exception {
        List<Map<String, Object>> body = new ArrayList<>();
        Map<String, Object> participant = new HashMap<>();
        participant.put(PRIMARY_EMAIL, "email");
        participant.put(PRIMARY_MOBILE, "mobile");
        participant.put(PARTICIPANT_NAME, "name");
        participant.put(PARTICIPANT, participant);
        participant.put(APPLICANT_CODE, "applicant_code");
        participant.put(VERIFIER_CODE, "verifier_code");
        participant.put(ROLES, Arrays.asList("provider"));
        participant.put(TERMS_AND_CONDITIONS_VERSION,"1");
        body.add(participant);
        OnboardRequest onboardRequest = new OnboardRequest(body);
        assertEquals(participant.get(PARTICIPANT_NAME), onboardRequest.getParticipantName());
        assertEquals(participant.get(PRIMARY_MOBILE), onboardRequest.getPrimaryMobile());
        assertEquals(participant.get(PARTICIPANT), onboardRequest.getParticipant());
        assertEquals(participant.get(PRIMARY_EMAIL), onboardRequest.getPrimaryEmail());
        assertEquals(participant.get(ROLES), onboardRequest.getRoles());
        assertEquals(participant.get(TERMS_AND_CONDITIONS_VERSION),onboardRequest.getTermsAndConditionsVersion());
    }
}