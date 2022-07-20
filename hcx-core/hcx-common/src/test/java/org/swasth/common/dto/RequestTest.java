package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.swasth.common.utils.Constants.*;

 public class RequestTest {

    @Test
    public void check_payload() throws Exception {
        Request request = new Request(getRequestBody(), COVERAGE_ELIGIBILITY_CHECK);
        request.setStatus(Constants.COMPLETE_STATUS);
        request.setMid("test_123");
        assertEquals(COMPLETE_STATUS,request.getStatus());
    }

    @Test
    public void check_plain_payload() throws Exception {
        Request request = new Request(getPlainRequestBody(), COVERAGE_ELIGIBILITY_CHECK);
        assertNotNull(request);
    }

    @Test(expected = ClientException.class)
    public void check_exception_payload() throws Exception {
        new Request(null, COVERAGE_ELIGIBILITY_CHECK);
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
        Request request = new Request(getNotificationRequest(), NOTIFICATION_NOTIFY);
        request.setNotificationId("f07250bf-a64b-4537-9182-c233e78d2847");
        assertEquals("hcx-apollo-12345", request.getSenderCode());
        assertEquals("f07250bf-a64b-4537-9182-c233e78d2847", request.getNotificationId());
        assertEquals("notification-123", request.getTopicCode());
        assertTrue(request.getRecipientCodes().contains("test-user@hcx"));
        assertTrue(request.getRecipientRoles().contains("payor"));
        assertTrue(request.getSubscriptions().contains("hcx-notification-001:hcx-apollo-12345"));
        assertTrue(((String) request.getNotificationData().get(MESSAGE)).contains("Payor system down for sometime"));
    }

    private Map<String,Object> getNotificationRequest() {
        Map<String,Object> obj = new HashMap<>();
        obj.put(SENDER_CODE,"hcx-apollo-12345");
        obj.put(TOPIC_CODE, "notification-123");
        obj.put(RECIPIENT_ROLES, List.of("payor"));
        obj.put(RECIPIENT_CODES, List.of("test-user@hcx"));
        obj.put(SUBSCRIPTIONS, List.of("hcx-notification-001:hcx-apollo-12345"));
        Map<String,Object> notificationData = new HashMap<>();
        notificationData.put("message","Payor system down for sometime");
        notificationData.put("duration","2hrs");
        notificationData.put("startTime","9PM");
        notificationData.put("date","26th April 2022 IST");
        obj.put(NOTIFICATION_DATA,notificationData);
        return obj;
    }

     @Test
     public void testSubscriptionPayload() throws Exception {
         Request request = new Request(getSubscriptionRequest(), NOTIFICATION_SUBSCRIBE);
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
         Request request = new Request(getSubscriptionUpdateRequest(), NOTIFICATION_SUBSCRIPTION_UPDATE);
         assertEquals("hcx-notification-001",request.getTopicCode());
         assertEquals("payor01@hcx", request.getRecipientCode());
         assertEquals("provider01@hcx", request.getSenderCode());
         assertEquals(1, request.getSubscriptionStatus());
         assertTrue(request.getIsDelegated());
         assertNotNull(request.getExpiry());
     }

     private Map<String,Object> getSubscriptionUpdateRequest() {
         Map<String,Object> obj = new HashMap<>();
         obj.put(RECIPIENT_CODE,"payor01@hcx");
         obj.put(TOPIC_CODE,"hcx-notification-001");
         obj.put(SENDER_CODE,"provider01@hcx");
         obj.put(SUBSCRIPTION_STATUS, 1);
         obj.put(EXPIRY, System.currentTimeMillis());
         obj.put(IS_DELEGATED, true);
         return obj;
     }
}
