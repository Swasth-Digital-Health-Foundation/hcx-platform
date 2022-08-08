package org.swasth.common.helpers;


import org.junit.Test;
import org.swasth.common.dto.Request;
import org.swasth.common.utils.Constants;

import java.util.*;

import static org.junit.Assert.*;
import static org.swasth.common.utils.Constants.*;


public class EventGeneratorTest {

    private final EventGenerator eventGenerator = new EventGenerator(Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"), Arrays.asList("alg", "enc"), Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"), Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"),Arrays.asList("x-hcx-notification_id","x-hcx-notification_data","x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-correlation_id"));

    @Test
    public void check_generatePayloadEvent() throws Exception {
        String result = eventGenerator.generatePayloadEvent(getRequest());
        assertNotNull(result);
    }

    @Test
    public void check_generateMetadataEvent() throws Exception {
        String result = eventGenerator.generateMetadataEvent(getRequest());
        assertNotNull(result);
    }

    @Test
    public void check_generateMetadataEvent_for_on_action_request() throws Exception {
        Request request = getRequest();
        request.setApiAction( Constants.COVERAGE_ELIGIBILITY_ONCHECK);
        String result = eventGenerator.generateMetadataEvent(request);
        assertNotNull(result);
    }

    @Test
    public void check_generateMetadataEvent_JSON() throws Exception {
        String result = eventGenerator.generateMetadataEvent( getJSONRequest("response.error"));
        assertNotNull(result);
    }

    @Test
    public void check_generateMetadataEvent_JSON_Redirect() throws Exception {
        String result = eventGenerator.generateMetadataEvent(getRedirectJSONRequest());
        assertNotNull(result);
    }

    @Test
    public void check_generateMetadataEvent_JSON_Empty_Headers() throws Exception {
        String result = eventGenerator.generateMetadataEvent(getJSONRequest(""));
        assertNotNull(result);
    }

    @Test
    public void check_generateAuditEvent() throws Exception {
        Map<String,Object> result = eventGenerator.generateAuditEvent(getRequest());
        assertEquals("/test", result.get(Constants.ACTION));
    }

    @Test
    public void check_generateAuditEvent_if_status_is_null() throws Exception {
        Map<String,Object> result = eventGenerator.generateAuditEvent(getJSONRequest(null));
        assertEquals("request.queued", result.get(Constants.STATUS));
    }

    @Test
    public void check_createNotifyEvent() throws Exception {
        String result = eventGenerator.createNotifyEvent("test-code-123", "provider01@hcx", List.of("payor01@hcx"), Collections.emptyList(), Collections.emptyList(), Collections.singletonMap(PARTICIPANT_CODE, "provider01@hcx"));
        assertNotNull(result);
    }

    public Request getRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        Request request = new Request(obj, COVERAGE_ELIGIBILITY_CHECK);
        request.setApiAction("/test");
        return request;
    }

    public Request getJSONRequest(String status) throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-status",status);
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-error_details", new HashMap<>() {{
            put("code","ERR_INVALID_ENCRYPTION");
            put("message","");
            put("trace","Recipient Invalid Encryption");
        }});
        return new Request(obj, COVERAGE_ELIGIBILITY_ONCHECK);
    }

    public Request getRedirectJSONRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-redirect_to","1-74f6cb29-4116-42d0-9fbb-adb65e6a64a");
        obj.put("x-hcx-status","response.redirect");
        return new Request(obj, COVERAGE_ELIGIBILITY_CHECK);
    }

    public Request getJWERequestWithNoStatus() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0wNlQwOTo1MDoyMyswMCIsCiJ4LWhjeC13b3JrZmxvd19pZCI6IjFlODMtNDYwYS00ZjBiLWIwMTYtYzIyZDgyMDY3NGUxIgp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        Request request = new Request(obj, COVERAGE_ELIGIBILITY_CHECK);
        request.setApiAction("/test");
        return request;
    }

    @Test
    public void testGenerateMetadataEventWithNoStatusHeader() throws Exception {
        String result = eventGenerator.generateMetadataEvent(getJWERequestWithNoStatus());
        assertNotNull(result);
        //Check the status is present, even the request does not have status
        assertTrue(result.contains("request.queued"));
    }

    @Test
    public void testGenerateMetadataEventWithDifferentStatusHeader() throws Exception {
        //Request has "x-hcx-status":"request.initiate"
        String result = eventGenerator.generateMetadataEvent(getRequest());
        assertNotNull(result);
        //Check the status is present with the value request.queued generated by HCX, even after the request has different status
        assertTrue(result.contains("request.queued"));
    }

    @Test
    public void testGenerateMetadataEventWithStatusHeaderOnAction() throws Exception {
        //Request has "x-hcx-status":"request.initiate"
        String result = eventGenerator.generateMetadataEvent(getJSONRequest("response.error"));
        assertNotNull(result);
        //Check the status is present with the value request.queued generated by HCX, even after the request has different status
        assertTrue(result.contains("response.error"));
    }

    private Request getNotificationRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put(NOTIFICATION_REQ_ID,"hcx-notification-001");
        obj.put(HCX_SENDER_CODE,"hcx-apollo-12345");
        obj.put(HCX_RECIPIENT_CODE,"hcx-star-insurance-001");
        obj.put(API_CALL_ID,"1fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(CORRELATION_ID,"2fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(WORKFLOW_ID,"3fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(TIMESTAMP,"1629057611000");
        Map<String,Object> notificationData = new HashMap<>();
        notificationData.put("message","Payor system down for sometime");
        notificationData.put("duration","2hrs");
        notificationData.put("startTime","9PM");
        notificationData.put("date","26th April 2022 IST");
        obj.put(NOTIFICATION_DATA,notificationData);
        return new Request(obj, NOTIFICATION_NOTIFY);
    }

    @Test
    public void testGenerateMetadataEventNotificationSuccess() throws Exception {
        Request notificationReq = getNotificationRequest();
        notificationReq.setApiAction(NOTIFICATION_NOTIFY);
        String result = eventGenerator.generateMetadataEvent(notificationReq);
        assertNotNull(result);
        //HCX should add status if the status is not present in the request
        assertTrue(result.contains(QUEUED_STATUS));
        //HCX should add triggerType as API
        assertTrue(result.contains(TRIGGER_VALUE));

    }

    @Test
    public void testGenerateSubscriptionEvent() throws Exception {
        String result = eventGenerator.generateSubscriptionEvent(NOTIFICATION_SUBSCRIBE,"hcx-apollo-12345","hcx-notification-001",new ArrayList<>(){{add("icici-67890");add("Payor1"); add("Payor2");}});
        System.out.println(result);
        assertNotNull(result);
        assertTrue(result.contains(QUEUED_STATUS));
        assertTrue(result.contains(NOTIFICATION_SUBSCRIBE));
        assertTrue(result.contains("hcx-apollo-12345"));
        assertTrue(result.contains("hcx-notification-001"));
        assertTrue(result.contains("icici-67890"));
    }

    @Test
    public void testGenerateSubscriptionAuditEvent() throws Exception {
        Request subscriptionReq = getSubscriptionRequest();
        Map<String,Object> resultMap = eventGenerator.generateSubscriptionAuditEvent(subscriptionReq,QUEUED_STATUS,new ArrayList<>(){{add("icici-67890");}});
        assertNotNull(resultMap);
        assertEquals(AUDIT,resultMap.get(EID));
        assertNotNull(resultMap.get(MID));
        assertEquals(NOTIFICATION_SUBSCRIBE,resultMap.get(ACTION));
        assertEquals("hcx-notification-001",resultMap.get(TOPIC_CODE));
        assertEquals("hcx-apollo-12345",resultMap.get(RECIPIENT_CODE));
        assertNotNull(resultMap.get(Constants.SENDER_LIST));
    }

    private Request getSubscriptionRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put(RECIPIENT_CODE,"hcx-apollo-12345");
        obj.put(TOPIC_CODE,"hcx-notification-001");
        obj.put(SENDER_LIST,new ArrayList<>(){
            { add("Payor1"); add("Payor2");}
        });
        return new Request(obj,NOTIFICATION_SUBSCRIBE);
    }

    @Test
    public void testGenerateOnSubscriptionEvent() throws Exception {
        String result = eventGenerator.generateOnSubscriptionEvent(NOTIFICATION_ON_SUBSCRIBE,"hcx-apollo-12345","icici-67890","hcx-apollo:icici-67890",1);
        assertNotNull(result);
        assertTrue(result.contains(QUEUED_STATUS));
        assertTrue(result.contains(NOTIFICATION_ON_SUBSCRIBE));
        assertTrue(result.contains("hcx-apollo-12345"));
        assertTrue(result.contains("icici-67890"));
        assertTrue(result.contains("hcx-apollo:icici-67890"));
    }

    @Test
    public void testGenerateOnSubscriptionAuditEvent() {
        Map<String,Object> resultMap = eventGenerator.generateOnSubscriptionAuditEvent(NOTIFICATION_ON_SUBSCRIBE,"hcx-apollo-12345","subscription_id-001",QUEUED_STATUS,"icici-67890",1);
        assertNotNull(resultMap);
        assertEquals(AUDIT,resultMap.get(EID));
        assertNotNull(resultMap.get(MID));
        assertEquals(NOTIFICATION_ON_SUBSCRIBE,resultMap.get(ACTION));
        assertEquals(1,resultMap.get(SUBSCRIPTION_STATUS));
        assertEquals("hcx-apollo-12345",resultMap.get(RECIPIENT_CODE));
        assertEquals("icici-67890",resultMap.get(Constants.SENDER_CODE));
        assertEquals("subscription_id-001",resultMap.get(SUBSCRIPTION_ID));
        assertNotNull(resultMap.get(ETS));
        assertEquals(QUEUED_STATUS,resultMap.get(NOTIFY_STATUS));
    }

    @Test
    public void testCreateAuditLog() {
        Map<String,Object> resultMap = eventGenerator.createAuditLog("provider01@hcx","participant",
                Collections.singletonMap(ACTION, PARTICIPANT_CREATE), Collections.singletonMap(AUDIT_STATUS, CREATED));
        assertNotNull(resultMap);
        assertEquals(AUDIT, resultMap.get(EID));
        assertNotNull(resultMap.get(ETS));
        assertNotNull(resultMap.get(MID));
        assertEquals("provider01@hcx", ((Map<String,Object>) resultMap.get(OBJECT)).get(ID));
        assertEquals("participant", ((Map<String,Object>) resultMap.get(OBJECT)).get(TYPE));
        assertEquals(PARTICIPANT_CREATE, ((Map<String,Object>) resultMap.get(CDATA)).get(ACTION));
        assertEquals(CREATED, ((Map<String,Object>) resultMap.get(EDATA)).get(AUDIT_STATUS));
    }

}
