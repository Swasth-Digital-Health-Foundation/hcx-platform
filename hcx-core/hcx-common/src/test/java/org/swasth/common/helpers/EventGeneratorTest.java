package org.swasth.common.helpers;


import org.junit.Test;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;

import java.util.*;

import static org.junit.Assert.*;
import static org.swasth.common.utils.Constants.*;


public class EventGeneratorTest {

    private final EventGenerator eventGenerator = new EventGenerator(Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"), Arrays.asList("alg", "enc"), Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"), Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"),Arrays.asList("x-hcx-notification_id","x-hcx-notification_data","x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-correlation_id"),"test-tag");
    private final EventGenerator eventGeneratorTag  = new EventGenerator(Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"), Arrays.asList("alg", "enc"), Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"), Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-correlation_id"),Arrays.asList("x-hcx-notification_id","x-hcx-notification_data","x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-api_call_id", "x-hcx-timestamp", "x-hcx-correlation_id"),"");
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
    public void check_notify_audit_event() throws Exception {
        Map<String,Object> result = eventGenerator.createNotifyAuditEvent(getNotificationRequest());
        assertEquals(NOTIFICATION_NOTIFY,result.get(ACTION));
    }
    @Test
    public void check_generateAuditEventTagEmpty() throws Exception {
        Map<String,Object> result = eventGeneratorTag.generateAuditEvent(getRequest());
        assertEquals("/test", result.get(Constants.ACTION));
    }

    @Test
    public void check_generateAuditEvent_if_status_is_null() throws Exception {
        Map<String,Object> result = eventGenerator.generateAuditEvent(getJSONRequest(null));
        assertEquals("request.queued", result.get(Constants.STATUS));
    }

    @Test
    public void check_createNotifyEvent() throws Exception {
        String result = eventGenerator.createNotifyEvent("test-code-123", "provider01@hcx", PARTICIPANT_CODE, List.of("payor01@hcx"), System.currentTimeMillis(), "test message", "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCG+XLPYiCxrZq71IX+w7uoDGxGI7qy7XaDbL3BJE33ju7rjdrP7wsAOWRvM8BIyWuRZZhl9xG+u7l/7OsZAzGoqI7p+32x+r9IJVzboLDajk6tp/NPg1csc7f2M5Bu6rkLEvrKLz3dgy3Q928rMsD3rSmzBLelfKTo+aDXvCOiw1dMWsZZdkEpCTJxH39Nb2K4S59kO/R2GtSU/QMLq65m34XcMZpDtatA1u1S8JdZNNeMCO+NuFKBzIfvXUCQ8jkf7h612+UP1AYhoyCMFpzUZ9b7liQF9TYpX1Myr/tT75WKuRlkFlcALUrtVskL8KA0w6sA0nX5fORVsuVehVeDAgMBAAECggEAX1n1y5/M7PhxqWO3zYTFGzC7hMlU6XZsFOhLHRjio5KsImgyPlbm9J+W3iA3JLR2c17MTKxAMvg3UbIzW5YwDLAXViC+aW90like8mEQzzVdS7ysXG2ytcqCGUHQNStI0hP0a8T39XbodQl31ZKjU9VW8grRGe12Kse+4ukcW6yRVES+CkyO5BQB+vs3voZavodRGsk/YSt00PtIrFPJgkDuyzzcybKJD9zeJk5W3OGVK1z0on+NXKekRti5FBx/uEkT3+knkz7ZlTDNcyexyeiv7zSL/L6tcszV0Fe0g9vJktqnenEyh4BgbqABPzQR++DaCgW5zsFiQuD0hMadoQKBgQC+rekgpBHsPnbjQ2Ptog9cFzGY6LRGXxVcY7hKBtAZOKAKus5RmMi7Uv7aYJgtX2jt6QJMuE90JLEgdO2vxYG5V7H6Tx+HqH7ftCGZq70A9jFBaba04QAp0r4TnD6v/LM+PGVT8FKtggp+o7gZqXYlSVFm6YzI37G08w43t2j2aQKBgQC1Nluxop8w6pmHxabaFXYomNckziBNMML5GjXW6b0xrzlnZo0p0lTuDtUy2xjaRWRYxb/1lu//LIrWqSGtzu+1mdmV2RbOd26PArKw0pYpXhKFu/W7r6n64/iCisoMJGWSRJVK9X3D4AjPaWOtE+jUTBLOk0lqPJP8K6yiCA6ZCwKBgDLtgDaXm7HdfSN1/Fqbzj5qc3TDsmKZQrtKZw5eg3Y5CYXUHwbsJ7DgmfD5m6uCsCPa+CJFl/MNWcGxeUpZFizKn16bg3BYMIrPMao5lGGNX9p4wbPN5J1HDD1wnc2jULxupSGmLm7pLKRmVeWEvWl4C6XQ+ykrlesef82hzwcBAoGBAKGY3v4y4jlSDCXaqadzWhJr8ffdZUrQwB46NGb5vADxnIRMHHh+G8TLL26RmcET/p93gW518oGg7BLvcpw3nOZaU4HgvQjT0qDvrAApW0V6oZPnAQUlarTU1Uk8kV9wma9tP6E/+K5TPCgSeJPg3FFtoZvcFq0JZoKLRACepL3vAoGAMAUHmNHvDI+v0eyQjQxlmeAscuW0KVAQQR3OdwEwTwdFhp9Il7/mslN1DLBddhj6WtVKLXu85RIGY8I2NhMXLFMgl+q+mvKMFmcTLSJb5bJHyMz/foenGA/3Yl50h9dJRFItApGuEJo/30cG+VmYo2rjtEifktX4mDfbgLsNwsI=");
        assertNotNull(result);
    }

    public Request getRequest() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        Request request1 = new Request(obj, COVERAGE_ELIGIBILITY_CHECK);
        Map<String,Object> senderDetails = new HashMap<>();
        senderDetails.put(ROLES,List.of("payor"));
        senderDetails.put(PARTICIPANT_NAME,"new-payor-3");
        senderDetails.put(PRIMARY_EMAIL,"newpayor003@gmail.com");
        Map<String,Object> recipentDetails = new HashMap<>();
        recipentDetails.put(ROLES,List.of("payor"));
        recipentDetails.put(PARTICIPANT_NAME,"New payor 2");
        recipentDetails.put(PRIMARY_EMAIL,"newpayor002@gmail.com");
        obj.put(SENDERDETAILS,senderDetails);
        obj.put(RECIPIENTDETAILS,recipentDetails);
        Request request = new Request(obj, ACTION);
        request.setApiAction("/test");
        request.setErrorDetails(new HashMap<>());
        return request;
    }
    public Request getEmptyDetails() throws Exception {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        Map<String,Object> senderDetails = Collections.emptyMap();
        Map<String,Object> recipentDetails = Collections.emptyMap();
        obj.put(SENDERDETAILS,senderDetails);
        obj.put(RECIPIENTDETAILS, recipentDetails);
        Request request = new Request(obj, ACTION);
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
        obj.put(HCX_SENDER_CODE,"hcx-apollo-12345");
        obj.put(HCX_RECIPIENT_CODE,"hcx-gateway");
        obj.put(CORRELATION_ID, "5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put(API_CALL_ID, "1e83-460a-4f0b-b016-c22d820674e1");
        obj.put(TIMESTAMP, "2022-01-06T09:50:23+00");
        Map<String,Object> notificationHeaders = new HashMap<>();
        notificationHeaders.put(RECIPIENT_ROLES, List.of("payor"));
        notificationHeaders.put(RECIPIENT_CODES, List.of("test-user@hcx"));
        notificationHeaders.put(SUBSCRIPTIONS, List.of("hcx-notification-001:hcx-apollo-12345"));
        obj.put(NOTIFICATION_HEADERS, notificationHeaders);
        obj.put(PAYLOAD, "eyJhbGciOiJSUzI1NiJ9.eyJ0b3BpY19jb2RlIjoibm90aWYtcGFydGljaXBhbnQtb25ib2FyZGVkIiwibWVzc2FnZSI6IlBhcnRpY2lwYW50IGhhcyBzdWNjZXNzZnVsbHkgb25ib2FyZGVkIn0=.L14NMRVoQq7TMEUt0IiG36P0NgDH1Poz4Nbh5BRZ7BcFXQzUI4SBduIJKY-WFCMPdKBl_LjlSm9JpNULn-gwLiDQ8ipQ3fZhzOkdzyjg0kUfpYN_aLQVgMaZ8Nrw3WytXIHserNxmka3wJQuSLvPnz9aJoFABij2evurnTsKq3oNbR0Oac3FJrpPO2O8fKaXs0Pi5Stf81eqcJ3Xs7oncJqBzgbp_jWShX8Ljfrf_TvM1patR-_h4E0O0HoVb0zD7SQmlKYOy0hw1bli5vdCnkh0tc1dF9yYrTEgofOjRemycFz_wEJ6FjFO1RryaBETw7qQ8hdGLemD545yUxCUng");
        return new Request(obj, NOTIFICATION_NOTIFY);
    }

    @Test
    public void testGenerateMetadataEventNotificationSuccess() throws Exception {
        Request notificationReq = getNotificationRequest();
        notificationReq.setApiAction(NOTIFICATION_NOTIFY);
        String result = eventGenerator.generateMetadataEvent(notificationReq);
        assertNotNull(result);
        assertTrue(result.contains(QUEUED_STATUS));
    }

    @Test
    public void testGenerateSubscriptionEvent() throws Exception {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put(TOPIC_CODE, "hcx-notification-001");
        requestBody.put(SENDER_LIST, new ArrayList<>(){{add("hcx-participant-67890");add("Payor1"); add("Payor2");}});
        requestBody.put(RECIPIENT_CODE, "hcx-participant-12345");
        Request request = new Request(requestBody, NOTIFICATION_SUBSCRIBE);
        String result = eventGenerator.generateSubscriptionEvent(request ,new HashMap<>(){{put("icici-67890","subscription_1");put("Payor1","subscription_2");put("Payor2","subscription_3");}});
        assertNotNull(result);
        assertTrue(result.contains(QUEUED_STATUS));
        assertTrue(result.contains(NOTIFICATION_SUBSCRIBE));
        assertTrue(result.contains("hcx-participant-12345"));
        assertTrue(result.contains("hcx-notification-001"));
        assertTrue(result.contains("hcx-participant-67890"));
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
        assertEquals("hcx-apollo-12345",resultMap.get(HCX_RECIPIENT_CODE));
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
        String result = eventGenerator.generateOnSubscriptionEvent(NOTIFICATION_ON_SUBSCRIBE,"hcx-apollo-12345","icici-67890","hcx-apollo:icici-67890","Active");
        assertNotNull(result);
        assertTrue(result.contains(QUEUED_STATUS));
        assertTrue(result.contains(NOTIFICATION_ON_SUBSCRIBE));
        assertTrue(result.contains("hcx-apollo-12345"));
        assertTrue(result.contains("icici-67890"));
        assertTrue(result.contains("hcx-apollo:icici-67890"));
    }

    @Test
    public void testGenerateOnSubscriptionAuditEvent() throws Exception {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put(SUBSCRIPTION_ID, "subscription_id-001");
        requestBody.put(SUBSCRIPTION_STATUS, ACTIVE);
        requestBody.put(SENDER_CODE, "hcx-participant-67890");
        Request request = new Request(requestBody, NOTIFICATION_ON_SUBSCRIBE);
        Map<String,Object> resultMap = eventGenerator.generateOnSubscriptionAuditEvent(request, "hcx-participant-12345", "subscription_id-001", QUEUED_STATUS,"Active");
        assertNotNull(resultMap);
        assertEquals(AUDIT, resultMap.get(EID));
        assertNotNull(resultMap.get(MID));
        assertEquals(NOTIFICATION_ON_SUBSCRIBE, resultMap.get(ACTION));
        assertEquals(ACTIVE, resultMap.get(SUBSCRIPTION_STATUS));
        assertEquals("hcx-participant-12345", resultMap.get(HCX_RECIPIENT_CODE));
        assertEquals("hcx-participant-67890", resultMap.get(HCX_SENDER_CODE));
        assertEquals("subscription_id-001", resultMap.get(SUBSCRIPTION_ID));
        assertNotNull(resultMap.get(ETS));
        assertEquals(QUEUED_STATUS, resultMap.get(STATUS));
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

    @Test
    public void testGenerateSubscriptionUpdateAuditEvent() throws Exception {
        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put(SENDER_CODE, "hcx-participant-67890");
        requestBody.put(RECIPIENT_CODE, "hcx-participant-12345");
        requestBody.put(TOPIC_CODE, "topic-001");
        requestBody.put(SUBSCRIPTION_STATUS, ACTIVE);
        Request request = new Request(requestBody, NOTIFICATION_SUBSCRIPTION_UPDATE);

        Response response = new Response();
        response.setSubscriptionId("subscription_id-001");
        response.setSubscriptionStatus(ACTIVE);

        Map<String,Object> resultMap = eventGenerator.generateSubscriptionUpdateAuditEvent(request, response);
        assertNotNull(resultMap);
        assertEquals(AUDIT, resultMap.get(EID));
        assertNotNull(resultMap.get(MID));
        assertEquals(NOTIFICATION_SUBSCRIPTION_UPDATE, resultMap.get(ACTION));
        assertEquals(ACTIVE, resultMap.get(SUBSCRIPTION_STATUS));
        assertEquals("hcx-participant-12345", resultMap.get(HCX_RECIPIENT_CODE));
        assertEquals("hcx-participant-67890", resultMap.get(HCX_SENDER_CODE));
        assertEquals("subscription_id-001", resultMap.get(SUBSCRIPTION_ID));
        assertNotNull(resultMap.get(ETS));
        assertEquals(QUEUED_STATUS, resultMap.get(STATUS));
    }
    @Test
    public void getSenderDetails() throws Exception {
        Map<String, Object> output = eventGenerator.generateAuditEvent(getRequest());
        assertEquals("new-payor-3",getRequest().getSenderName());
        assertEquals("New payor 2",getRequest().getRecipientName());
        assertEquals("newpayor003@gmail.com",getRequest().getSenderPrimaryEmail());
        assertEquals("newpayor002@gmail.com" ,getRequest().getRecipientPrimaryEmail());
        assertEquals(List.of("payor"),getRequest().getSenderRole());
        assertEquals(List.of("payor"),getRequest().getSenderRole());
    }
    @Test
    public void getSenderDetailsEmptyCheck() throws Exception {
        eventGenerator.generateAuditEvent(getEmptyDetails());
        assertEquals(Collections.emptyMap(), getEmptyDetails().senderDetails());
        assertEquals(Collections.emptyMap(), getEmptyDetails().recipientDetails());
    }

    @Test
    public void emptyEventgeneartorConstructor(){
        String tag = "test-tag";
        EventGenerator eventGenerator1 = new EventGenerator(tag);
        assertNotNull(eventGenerator1);
    }
}
