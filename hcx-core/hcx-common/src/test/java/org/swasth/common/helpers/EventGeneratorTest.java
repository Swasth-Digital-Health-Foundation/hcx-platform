package org.swasth.common.helpers;


import org.junit.Test;
import org.swasth.common.dto.Request;
import org.swasth.common.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    public Request getRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        Request request = new Request(obj);
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
        return new Request(obj);
    }

    public Request getRedirectJSONRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-redirect_to","1-74f6cb29-4116-42d0-9fbb-adb65e6a64a");
        obj.put("x-hcx-status","response.redirect");
        return new Request(obj);
    }

    public Request getJWERequestWithNoStatus() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0wNlQwOTo1MDoyMyswMCIsCiJ4LWhjeC13b3JrZmxvd19pZCI6IjFlODMtNDYwYS00ZjBiLWIwMTYtYzIyZDgyMDY3NGUxIgp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        Request request = new Request(obj);
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
        obj.put(NOTIFICATION_ID,"hcx-notification-001");
        obj.put(SENDER_CODE,"hcx-apollo-12345");
        obj.put(RECIPIENT_CODE,"hcx-star-insurance-001");
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
        return new Request(obj);
    }

    @Test
    public void testGenerateMetadataEventNotificationSuccess() throws Exception {
        Request notificationReq = getNotificationRequest();
        notificationReq.setApiAction(NOTIFICATION_REQUEST);
        String result = eventGenerator.generateMetadataEvent(notificationReq);
        assertNotNull(result);
        //HCX should add status if the status is not present in the request
        assertTrue(result.contains(QUEUED_STATUS));
        //HCX should add triggerType as API
        assertTrue(result.contains(TRIGGER_VALUE));

    }

}
