package org.swasth.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.mockito.Mockito;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.swasth.common.utils.Constants.*;

public class ResponseTest {

    @Test
    public void testCreateResponse() throws Exception {
        Response response = new Response(new Request(getRequestBody(), Constants.COVERAGE_ELIGIBILITY_CHECK));
        assertEquals("26b1060c-1e83-4600-9612-ea31e0ca5091", response.getApiCallId());
    }

    @Test
    public void testErrorResponse() {
        Response errorResp = new Response(new ResponseError(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid payload", null));
        assertEquals(ErrorCodes.ERR_INVALID_PAYLOAD, errorResp.getError().getCode());
    }

    @Test
    public void testNotificationResponse() {
        Response response = new Response();
        response.setNotifications(List.of(Collections.singletonMap(Constants.TOPIC_CODE, "notification-123")));
        assertEquals("notification-123", (response.getNotifications().get(0)).get(Constants.TOPIC_CODE));
    }

    @Test
    public void testSubscription(){
        Response mockResponse = Mockito.mock(Response.class);
        assertNotNull(mockResponse);
    }

    @Test
    public void testSubscriptionList(){
        Response response = new Response();
        List<Subscription> subscriptionList = getSubscriptionList();
        response.setSubscriptions(subscriptionList);
        response.setCount(subscriptionList.size());
        assertEquals(2,response.getSubscriptions().size());
        assertEquals(2,response.getCount().intValue());
        assertEquals(ACTIVE_CODE, response.getSubscriptions().get(0).getSubscription_status());
        assertEquals(INACTIVE_CODE, response.getSubscriptions().get(1).getSubscription_status());
    }

    @Test
    public void testSubscriptionIds(){
        Response response = new Response();
        List<String> subscriptionList = getSubscriptionIds();
        response.setSubscription_list(subscriptionList);
        assertEquals(2,response.getSubscription_list().size());
        assertEquals("hcx-notification-subscription-12345", response.getSubscription_list().get(0));
        assertEquals("hcx-notification-subscription-67890", response.getSubscription_list().get(1));
    }

    private List<Subscription> getSubscriptionList()  {
        List<Subscription> subscriptionList = new ArrayList<>();
        Subscription subscription = new Subscription();
        subscription.setSubscription_id("hcx-notification-subscription-12345");
        subscription.setTopic_code("hcx-notification-001");
        subscription.setSubscription_status(ACTIVE_CODE);
        Subscription inActiveSubscription = new Subscription();
        inActiveSubscription.setSubscription_id("hcx-notification-subscription-67890");
        inActiveSubscription.setTopic_code("hcx-notification-001");
        inActiveSubscription.setSubscription_status(INACTIVE_CODE);
        subscriptionList.add(subscription);
        subscriptionList.add(inActiveSubscription);
        return subscriptionList;
    }

    public Map<String,Object> getRequestBody() throws JsonProcessingException {
        return Collections.singletonMap("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS05ODc1Ni1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJyZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MTAxIn0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
    }

    private List<String> getSubscriptionIds(){
        List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add("hcx-notification-subscription-12345");
        subscriptionIds.add("hcx-notification-subscription-67890");
        return subscriptionIds;
    }

    @Test
    public void testSubscriptionUpdateResponse(){
        Response response = new Response();
        response.setSubscriptionStatus(1);
        response.setSubscriptionId("636109b8-3d95-4b55-a6b6-34daf53a6ee7");
        assertEquals(1, response.getSubscriptionStatus());
        assertEquals("636109b8-3d95-4b55-a6b6-34daf53a6ee7", response.getSubscriptionId());
    }
}
