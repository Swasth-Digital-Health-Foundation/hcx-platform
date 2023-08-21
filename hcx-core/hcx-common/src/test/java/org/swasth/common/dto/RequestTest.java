package org.swasth.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.swasth.common.utils.Constants.*;

 public class RequestTest {

    @Test
    public void check_payload() throws Exception {
        Request request = new Request(getRequestBody(), COVERAGE_ELIGIBILITY_CHECK, getAuthorizationHeader());
        request.setStatus(Constants.COMPLETE_STATUS);
        request.setMid("test_123");
        assertEquals(COMPLETE_STATUS,request.getStatus());
    }

    @Test
    public void check_plain_payload() throws Exception {
        Request request = new Request(getPlainRequestBody(), COVERAGE_ELIGIBILITY_CHECK, getAuthorizationHeader());
        assertNotNull(request);
    }

    @Test(expected = ClientException.class)
    public void check_exception_payload() throws Exception {
        new Request(null, COVERAGE_ELIGIBILITY_CHECK, getAuthorizationHeader());
    }

    public Map<String, Object> getRequestBody() {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS05ODc1Ni1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJyZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MTAxIn0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return obj;
    }

    public Map<String, Object> getPlainRequestBody() {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-status","response.error");
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-workflow_id","1e83-460a-4f0b-b016-c22d820674e1");
        obj.put("x-hcx-error_details", new HashMap<>() {{
            put("code","ERR_INVALID_ENCRYPTION");
            put("error","");
            put("trace","Recipient Invalid Encryption");
        }});
        return obj;
    }

    @Test
    public void testNotificationPayload() throws Exception {
        Request request = new Request(getNotificationRequest(), NOTIFICATION_NOTIFY, getAuthorizationHeader());
        assertEquals("notif-participant-onboarded", request.getTopicCode());
        assertNotNull(request.getNotificationMessage());
        assertEquals(PARTICIPANT_CODE, request.getRecipientType());
        assertEquals(List.of("test-user@hcx"), request.getRecipients());
    }

     @Test
     public void testNotificationPayloadWithInvalidCorrId() throws Exception {
         Request request = new Request(getInvalidCorrIdNotificationRequest(), NOTIFICATION_NOTIFY, getAuthorizationHeader());
         assertNotNull(request.getCorrelationId());
     }

    private Map<String,Object> getNotificationRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        Map<String,Object> notificationHeaders = new HashMap<>();
        notificationHeaders.put(SENDER_CODE, "hcx-apollo-12345");
        notificationHeaders.put(RECIPIENT_TYPE, PARTICIPANT_CODE);
        notificationHeaders.put(RECIPIENTS, List.of("test-user@hcx"));
        notificationHeaders.put("correlation_id", "1234");
        obj.put(NOTIFICATION_HEADERS, notificationHeaders);
        obj.put(PAYLOAD, JSONUtils.encodeBase64String(JSONUtils.serialize(notificationHeaders)) + ".eyJ0b3BpY19jb2RlIjoibm90aWYtcGFydGljaXBhbnQtb25ib2FyZGVkIiwibWVzc2FnZSI6IlBhcnRpY2lwYW50IGhhcyBzdWNjZXNzZnVsbHkgb25ib2FyZGVkIn0=.L14NMRVoQq7TMEUt0IiG36P0NgDH1Poz4Nbh5BRZ7BcFXQzUI4SBduIJKY-WFCMPdKBl_LjlSm9JpNULn-gwLiDQ8ipQ3fZhzOkdzyjg0kUfpYN_aLQVgMaZ8Nrw3WytXIHserNxmka3wJQuSLvPnz9aJoFABij2evurnTsKq3oNbR0Oac3FJrpPO2O8fKaXs0Pi5Stf81eqcJ3Xs7oncJqBzgbp_jWShX8Ljfrf_TvM1patR-_h4E0O0HoVb0zD7SQmlKYOy0hw1bli5vdCnkh0tc1dF9yYrTEgofOjRemycFz_wEJ6FjFO1RryaBETw7qQ8hdGLemD545yUxCUng");
        return obj;
    }

     private Map<String,Object> getInvalidCorrIdNotificationRequest() throws JsonProcessingException {
         Map<String,Object> obj = new HashMap<>();
         Map<String,Object> notificationHeaders = new HashMap<>();
         notificationHeaders.put(SENDER_CODE, "hcx-apollo-12345");
         notificationHeaders.put(RECIPIENT_TYPE, PARTICIPANT_CODE);
         notificationHeaders.put(RECIPIENTS, List.of("test-user@hcx"));
         notificationHeaders.put("correlation_id", "8c365958-9d3e-4e00-ac58-e6a7c837b74c");
         obj.put(NOTIFICATION_HEADERS, notificationHeaders);
         obj.put(PAYLOAD, JSONUtils.encodeBase64String(JSONUtils.serialize(notificationHeaders)) + ".eyJ0b3BpY19jb2RlIjoibm90aWYtcGFydGljaXBhbnQtb25ib2FyZGVkIiwibWVzc2FnZSI6IlBhcnRpY2lwYW50IGhhcyBzdWNjZXNzZnVsbHkgb25ib2FyZGVkIn0=.L14NMRVoQq7TMEUt0IiG36P0NgDH1Poz4Nbh5BRZ7BcFXQzUI4SBduIJKY-WFCMPdKBl_LjlSm9JpNULn-gwLiDQ8ipQ3fZhzOkdzyjg0kUfpYN_aLQVgMaZ8Nrw3WytXIHserNxmka3wJQuSLvPnz9aJoFABij2evurnTsKq3oNbR0Oac3FJrpPO2O8fKaXs0Pi5Stf81eqcJ3Xs7oncJqBzgbp_jWShX8Ljfrf_TvM1patR-_h4E0O0HoVb0zD7SQmlKYOy0hw1bli5vdCnkh0tc1dF9yYrTEgofOjRemycFz_wEJ6FjFO1RryaBETw7qQ8hdGLemD545yUxCUng");
         return obj;
     }

     @Test
     public void testSubscriptionPayload() throws Exception {
         Request request = new Request(getSubscriptionRequest(), NOTIFICATION_SUBSCRIBE, getAuthorizationHeader());
         assertEquals("hcx-notification-001",request.getTopicCode());
         assertEquals("hcx-apollo-12345", request.getRecipientCode());
         assertEquals(2,request.getSenderList().size());
         assertEquals("Payor1",request.getSenderList().get(0));
         assertEquals("Payor2",request.getSenderList().get(1));
     }

     private Map<String,Object> getSubscriptionRequest() {
         Map<String,Object> obj = new HashMap<>();
         obj.put(RECIPIENT_CODE,"hcx-apollo-12345");
         obj.put(TOPIC_CODE,"hcx-notification-001");
         obj.put(SENDER_LIST,new ArrayList<>(){
             { add("Payor1"); add("Payor2");}
         });
         return obj;
     }

     @Test
     public void testSubscriptionUpdatePayload() throws Exception {
         Request request = new Request(getSubscriptionUpdateRequest(), NOTIFICATION_SUBSCRIPTION_UPDATE, getAuthorizationHeader());
         assertEquals("hcx-notification-001",request.getTopicCode());
         assertEquals("payor01@hcx", request.getRecipientCode());
         assertEquals("provider01@hcx", request.getSenderCode());
         assertEquals(ACTIVE, request.getSubscriptionStatus());
         assertTrue(request.getIsDelegated());
         assertNotNull(request.getExpiry());
     }

     private Map<String,Object> getSubscriptionUpdateRequest() {
         Map<String,Object> obj = new HashMap<>();
         obj.put(RECIPIENT_CODE,"payor01@hcx");
         obj.put(TOPIC_CODE,"hcx-notification-001");
         obj.put(SENDER_CODE,"provider01@hcx");
         obj.put(SUBSCRIPTION_STATUS, ACTIVE);
         obj.put(EXPIRY, System.currentTimeMillis());
         obj.put(IS_DELEGATED, true);
         return obj;
     }

     public String getAuthorizationHeader() {
         return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMYU9HdVRrYVpsVEtzaERwUng1R25JaXUwV1A1S3VGUUoyb29WMEZnWGx3In0.eyJleHAiOjE2NDcwNzgwNjksImlhdCI6MTY0Njk5MTY2OSwianRpIjoiNDcyYzkwOTAtZWQ4YS00MDYxLTg5NDQtMzk4MjhmYzBjM2I4IiwiaXNzIjoiaHR0cDovL2E5ZGQ2M2RlOTFlZTk0ZDU5ODQ3YTEyMjVkYThiMTExLTI3Mzk1NDEzMC5hcC1zb3V0aC0xLmVsYi5hbWF6b25hd3MuY29tOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhlYWx0aC1jbGFpbS1leGNoYW5nZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIwYzU3NjNkZS03MzJkLTRmZDQtODU0Ny1iMzk2MGMxMzIwZjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiIxMThhMTRmMS04OTAxLTQxZTMtYWE5Zi1iNWFjMjYzNjkzMzIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwczovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cHM6Ly9uZGVhci54aXYuaW4iLCJodHRwOi8vbG9jYWxob3N0OjQyMDAiLCJodHRwOi8vbmRlYXIueGl2LmluIiwiaHR0cDovLzIwLjE5OC42NC4xMjgiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkhJRS9ISU8uSENYIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeCBhZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3ggYWRtaW4ifQ.SwDJNGkHOs7MrArqwdkArLkRDgPIU3SHwMdrppmG2JHQkpYRLqFpfmFPgIYNAyi_b_ZQnXKwuhT6ABNEV2-viJWTPLYe4z5JkeUGNurnrkSoMMObrd0s1tLYjdgu5j5kLaeUBeSeULTkdBfAM9KZX5Gn6Ri6AKs6uFq22hJOmhtw3RTyX-7kozG-SzSfIyN_-7mvJBZjBR73gaNJyEms4-aKULAnQ6pYkj4hzzlac2WCucq2zZnipeupBOJzx5z27MLdMs8lfNRTTqkQVhoUK0DhDxyj9N_TzbycPdykajhOrerKfpEnYcZpWfC-bJJSDagnP9D407OqoxoE3_niHw";
     }
}
