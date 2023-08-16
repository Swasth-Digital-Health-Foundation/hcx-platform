package org.swasth.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.mockito.Mockito;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.swasth.common.utils.Constants.*;

public class ResponseTest {

    @Test
    public void testCreateResponse() throws Exception {
        Response response = new Response(new Request(getRequestBody(), Constants.COVERAGE_ELIGIBILITY_CHECK, getAuthorizationHeader()));
        assertEquals("26b1060c-1e83-4600-9612-ea31e0ca5091", response.getApiCallId());
    }

    @Test
    public void testCreateResponseWithCorrelationId() throws Exception {
        Response response = new Response(new Request(getRequestBody(), Constants.COVERAGE_ELIGIBILITY_CHECK, getAuthorizationHeader()).getCorrelationId());
        assertEquals("5e934f90-111d-4f0b-b016-c22d820674e1", response.getCorrelationId());
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
        assertEquals(ACTIVE, response.getSubscriptions().get(0).getSubscription_status());
        assertEquals(INACTIVE, response.getSubscriptions().get(1).getSubscription_status());
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
        subscription.setSubscription_status(ACTIVE);
        Subscription inActiveSubscription = new Subscription();
        inActiveSubscription.setSubscription_id("hcx-notification-subscription-67890");
        inActiveSubscription.setTopic_code("hcx-notification-001");
        inActiveSubscription.setSubscription_status(INACTIVE);
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
        response.setSubscriptionStatus("Active");
        response.setSubscriptionId("636109b8-3d95-4b55-a6b6-34daf53a6ee7");
        assertEquals("Active", response.getSubscriptionStatus());
        assertEquals("636109b8-3d95-4b55-a6b6-34daf53a6ee7", response.getSubscriptionId());
    }

    @Test
    public void testCreateResponseWithResult(){
        Map<String,Object> result = new HashMap<>();
        result.put(HEALTHY, true);
        Response response = new Response(result);
        response.setStatus(SUCCESSFUL.toUpperCase());
        assertEquals(SUCCESSFUL.toUpperCase(), response.getStatus());
        assertEquals(true, response.getResult().get(HEALTHY));
    }

    public String getAuthorizationHeader() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMYU9HdVRrYVpsVEtzaERwUng1R25JaXUwV1A1S3VGUUoyb29WMEZnWGx3In0.eyJleHAiOjE2NDcwNzgwNjksImlhdCI6MTY0Njk5MTY2OSwianRpIjoiNDcyYzkwOTAtZWQ4YS00MDYxLTg5NDQtMzk4MjhmYzBjM2I4IiwiaXNzIjoiaHR0cDovL2E5ZGQ2M2RlOTFlZTk0ZDU5ODQ3YTEyMjVkYThiMTExLTI3Mzk1NDEzMC5hcC1zb3V0aC0xLmVsYi5hbWF6b25hd3MuY29tOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhlYWx0aC1jbGFpbS1leGNoYW5nZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIwYzU3NjNkZS03MzJkLTRmZDQtODU0Ny1iMzk2MGMxMzIwZjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiIxMThhMTRmMS04OTAxLTQxZTMtYWE5Zi1iNWFjMjYzNjkzMzIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwczovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cHM6Ly9uZGVhci54aXYuaW4iLCJodHRwOi8vbG9jYWxob3N0OjQyMDAiLCJodHRwOi8vbmRlYXIueGl2LmluIiwiaHR0cDovLzIwLjE5OC42NC4xMjgiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkhJRS9ISU8uSENYIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeCBhZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3ggYWRtaW4ifQ.SwDJNGkHOs7MrArqwdkArLkRDgPIU3SHwMdrppmG2JHQkpYRLqFpfmFPgIYNAyi_b_ZQnXKwuhT6ABNEV2-viJWTPLYe4z5JkeUGNurnrkSoMMObrd0s1tLYjdgu5j5kLaeUBeSeULTkdBfAM9KZX5Gn6Ri6AKs6uFq22hJOmhtw3RTyX-7kozG-SzSfIyN_-7mvJBZjBR73gaNJyEms4-aKULAnQ6pYkj4hzzlac2WCucq2zZnipeupBOJzx5z27MLdMs8lfNRTTqkQVhoUK0DhDxyj9N_TzbycPdykajhOrerKfpEnYcZpWfC-bJJSDagnP9D407OqoxoE3_niHw";
    }

}
