package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.utils.MockResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.swasth.common.utils.Constants.*;

class NotificationControllerTest extends BaseSpec {

    @Test
    void testNotificationSubscribeOneSender() throws Exception {
        doNothing().when(auditIndexer).createDocument(anyMap());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        when(postgreSQLClient.executeBatch()).thenReturn(new int[2]);
        String requestBody = getOnePayorSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertEquals(1, resObj.getSubscription_list().size());
        assertTrue(!resObj.getSubscription_list().get(0).isEmpty());
    }

    @Test
    void testNotificationSubscribeValidTopic() throws Exception {
        doNothing().when(auditIndexer).createDocument(anyMap());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        doNothing().when(postgreSQLClient).addBatch(anyString());
        when(postgreSQLClient.executeBatch()).thenReturn(new int[2]);
        String requestBody = getSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertEquals(2, resObj.getSubscription_list().size());
        assertTrue(!resObj.getSubscription_list().get(0).isEmpty());
        assertTrue(!resObj.getSubscription_list().get(1).isEmpty());
    }

    @Test
    void testNotificationUnSubscribeException() throws Exception {
        doThrow(new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR, "Test Internal Server Error")).when(postgreSQLClient).executeBatch();
        String requestBody = getSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_UNSUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(ErrorCodes.INTERNAL_SERVER_ERROR, resObj.getError().getCode());
        assertEquals("Test Internal Server Error", resObj.getError().getMessage());
    }

    @Test
    void testNotificationUnSubscribeSuccess() throws Exception {
        doNothing().when(auditIndexer).createDocument(anyMap());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        doNothing().when(postgreSQLClient).addBatch(anyString());
        when(postgreSQLClient.executeBatch()).thenReturn(new int[2]);
        String requestBody = getSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_UNSUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(2, resObj.getSubscription_list().size());
        assertTrue(!resObj.getSubscription_list().get(0).isEmpty());
        assertTrue(!resObj.getSubscription_list().get(1).isEmpty());
    }

    @Test
    void testNotificationUnSubscribeHcxSuccess() throws Exception {
        doNothing().when(auditIndexer).createDocument(anyMap());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        doNothing().when(postgreSQLClient).addBatch(anyString());
        when(postgreSQLClient.executeBatch()).thenReturn(new int[1]);
        String requestBody = getSubscriptionHcxRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_UNSUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(1, resObj.getSubscription_list().size());
        assertTrue(!resObj.getSubscription_list().get(0).isEmpty());
    }


    @Test
    void testNotificationSubscribeException() throws Exception {
        doThrow(new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR, "Test Internal Server Error")).when(postgreSQLClient).executeBatch();
        String requestBody = getSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(ErrorCodes.INTERNAL_SERVER_ERROR, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Test Internal Server Error"));
    }

    @Test
    void testNotificationSubscribeHcxValidTopic() throws Exception {
        doNothing().when(auditIndexer).createDocument(anyMap());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        doNothing().when(postgreSQLClient).addBatch(anyString());
        when(postgreSQLClient.executeBatch()).thenReturn(new int[1]);
        String requestBody = getSubscriptionHcxRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(1, resObj.getSubscription_list().size());
        assertTrue(!resObj.getSubscription_list().get(0).isEmpty());
    }

    @Test
    void testSubscriptionListEmpty() throws Exception {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(resObj.getSubscriptions().isEmpty());
        assertEquals(0, resObj.getCount());
    }

    @Test
    void testSubscriptionListWithOneActiveSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet(1);
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertEquals(1, resObj.getCount());
        assertEquals("subscription_id-001", resObj.getSubscriptions().get(0).getSubscription_id());
        assertEquals("topic_code-12345", resObj.getSubscriptions().get(0).getTopic_code());
        assertEquals(ACTIVE_CODE, resObj.getSubscriptions().get(0).getSubscription_status());
        assertEquals("hcx-apollo-12345", resObj.getSubscriptions().get(0).getSender_code());
        assertEquals("ICICI Lombard", resObj.getSubscriptions().get(0).getRecipient_code());
        assertEquals(1629057611000l, resObj.getSubscriptions().get(0).getExpiry());
        assertEquals(false, resObj.getSubscriptions().get(0).isIs_delegated());
    }

    @Test
    void testSubscriptionListWithOneInActiveSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet(0);
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertEquals(1, resObj.getCount());
        assertEquals("subscription_id-001", resObj.getSubscriptions().get(0).getSubscription_id());
        assertEquals("topic_code-12345", resObj.getSubscriptions().get(0).getTopic_code());
        assertEquals(INACTIVE_CODE, resObj.getSubscriptions().get(0).getSubscription_status());
        assertEquals("hcx-apollo-12345", resObj.getSubscriptions().get(0).getSender_code());
        assertEquals("ICICI Lombard", resObj.getSubscriptions().get(0).getRecipient_code());
        assertEquals(1629057611000l, resObj.getSubscriptions().get(0).getExpiry());
        assertEquals(false, resObj.getSubscriptions().get(0).isIs_delegated());
    }

    @Test
    void testSubscriptionListWithBothSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet();
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertEquals(3, resObj.getCount());
        assertEquals("subscription_id-001", resObj.getSubscriptions().get(0).getSubscription_id());
        assertEquals("topic_code-12345", resObj.getSubscriptions().get(0).getTopic_code());
        assertEquals(ACTIVE_CODE, resObj.getSubscriptions().get(0).getSubscription_status());
        assertFalse(resObj.getSubscriptions().get(0).isIs_delegated());
        assertEquals("subscription_id-002", resObj.getSubscriptions().get(1).getSubscription_id());
        assertEquals("topic_code-12346", resObj.getSubscriptions().get(1).getTopic_code());
        assertEquals(INACTIVE_CODE, resObj.getSubscriptions().get(1).getSubscription_status());
        assertFalse(resObj.getSubscriptions().get(1).isIs_delegated());
        assertEquals("subscription_id-003", resObj.getSubscriptions().get(2).getSubscription_id());
        assertEquals("topic_code-12347", resObj.getSubscriptions().get(2).getTopic_code());
        assertEquals(PENDING_CODE, resObj.getSubscriptions().get(2).getSubscription_status());
        assertTrue(resObj.getSubscriptions().get(2).isIs_delegated());
    }

    @Test
    void testSubscriptionListException() throws Exception {
        doThrow(Exception.class).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
        assertTrue(response.getContentAsString().contains(ErrorCodes.INTERNAL_SERVER_ERROR.name()));
    }

    @Test
    void testSubscriptionListWithFilters() throws Exception {
        ResultSet mockResultSet = getMockResultSet();
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequestWithValidFilters();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertNotNull(resObj.getTimestamp());
    }

    @Test
    void testSubscriptionListWithInvalidFilters() throws Exception {
        String requestBody = getSubscriptionListRequestWithInValidFilters();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Invalid notifications filters"));
    }

    @Test
    void testNotificationListEmpty() throws Exception {
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void testNotificationListData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_LIST).content(getNotificationListRequest()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertFalse(resObj.getNotifications().isEmpty());
    }

    @Test
    void testNotificationInvalidFilterProperties() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_LIST).content(getInvalidFilterRequest()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Invalid notifications filters"));
    }

    //subscription_id,subscription_status,topic_code,sender_code,recipient_code,expiry,is_delegated
    private ResultSet getMockResultSet(int status) throws SQLException {
        return MockResultSet.create(
                new String[]{"subscription_id", "subscription_status", "topic_code", "sender_code", "recipient_code", "expiry", "is_delegated"}, //columns
                new Object[][]{ // data
                        {"subscription_id-001", status, "topic_code-12345", "hcx-apollo-12345", "ICICI Lombard", 1629057611000l, false}
                });
    }

    private ResultSet getMockResultSet() throws SQLException {
        return MockResultSet.create(
                new String[]{"subscription_id", "subscription_status", "topic_code", "sender_code", "recipient_code", "expiry", "is_delegated"}, //columns
                new Object[][]{ // data
                        {"subscription_id-001", 1, "topic_code-12345", "hcx-apollo-12345", "ICICI Lombard", 1629057611000l, false},
                        {"subscription_id-002", 0, "topic_code-12346", "hcx-apollo-12346", "ICICI Lombard", 1629057611000l, false},
                        {"subscription_id-003", -1, "topic_code-12347", "hcx-apollo-12347", "ICICI Lombard", 1629057611000l, true}
                });
    }

    private ResultSet getSubscriptionsResultSet() throws SQLException {
        return MockResultSet.createStringMock(
                new String[]{"subscription_id"}, //columns
                new Object[][]{ // data
                        {"subscription-123"}
                });
    }

    private String getSubscriptionListRequest() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(FILTERS, new HashMap<>());
        obj.put(LIMIT, 10);
        obj.put(OFFSET, 0);
        return JSONUtils.serialize(obj);
    }

    private String getSubscriptionListRequestWithValidFilters() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(FILTERS, new HashMap<>() {{
            put("subscription_status", 2);
        }});
        obj.put(LIMIT, 10);
        obj.put(OFFSET, 2);
        return JSONUtils.serialize(obj);
    }

    private String getSubscriptionListRequestWithInValidFilters() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(FILTERS, new HashMap<>() {{
            put("expiry", 0);
        }});
        obj.put(LIMIT, 10);
        obj.put(OFFSET, 0);
        return JSONUtils.serialize(obj);
    }

    private String getNotificationListRequest() throws JsonProcessingException {
        Map<String, Object> filters = new HashMap<>();
        filters.put(PRIORITY, 1);
        return JSONUtils.serialize(Collections.singletonMap(FILTERS, filters));
    }

    private String getInvalidFilterRequest() throws JsonProcessingException {
        Map<String, Object> filters = new HashMap<>();
        filters.put("test", "123");
        return JSONUtils.serialize(Collections.singletonMap(FILTERS, filters));
    }

    private String getNotificationRequest(String topicCode, List<String> subscriptions) throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, topicCode);
        obj.put(RECIPIENT_ROLES, List.of("provider", "payor"));
        obj.put(RECIPIENT_CODES, List.of("test-user@hcx"));
        obj.put(SUBSCRIPTIONS, subscriptions);
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("message", "Payor system down for sometime");
        notificationData.put("duration", "2hrs");
        notificationData.put("startTime", "9PM");
        notificationData.put("date", "26th April 2022 IST");
        obj.put(NOTIFICATION_DATA, notificationData);
        return JSONUtils.serialize(obj);
    }

    @Test
    void testNotifySuccess() throws Exception {
        doReturn(getSubscriptionsResultSet()).when(postgreSQLClient).executeQuery(anyString());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        String requestBody = getNotificationRequest("notification-123", List.of("subscription-123"));
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_NOTIFY).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertNotNull(resObj.getNotificationRequestId());
    }

    @Test
    void testNotifyWithEmptySubscriptions() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        String requestBody = getNotificationRequest("notification-123", Collections.EMPTY_LIST);
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_NOTIFY).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertNotNull(resObj.getNotificationRequestId());
    }

    @Test
    void testNotifyFailure() throws Exception {
        doReturn(getSubscriptionsResultSet()).when(postgreSQLClient).executeQuery(anyString());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        String requestBody = getNotificationRequest("hcx-notification-001", List.of("subscription-124"));
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_NOTIFY).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertNotNull(resObj.getTimestamp());
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Invalid subscriptions list"));
    }

    private String getSubscriptionRequest() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, "be0e578d-b391-42f9-96f7-1e6bacd91c20");
        obj.put(RECIPIENT_CODE, "hcx-apollo-12345");
        List<String> sendersList = new ArrayList<>() {{
            add("payor1");
            add("payor2"); //1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e
        }};
        obj.put(SENDER_LIST, sendersList);
        return JSONUtils.serialize(obj);
    }

    private String getSubscriptionHcxRequest() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, "be0e578d-b391-42f9-96f7-1e6bacd91c20");
        obj.put(RECIPIENT_CODE, "hcx-apollo-12345");
        List<String> sendersList = new ArrayList<>() {{
            add("hcx-registry-code");
        }};
        obj.put(SENDER_LIST, sendersList);
        return JSONUtils.serialize(obj);
    }

    private String getOnePayorSubscriptionRequest() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, "NOTIFICATION@HCX01");
        obj.put(RECIPIENT_CODE, "hcx-apollo-12345");
        List<String> sendersList = new ArrayList<>() {{
            add("payor1");
        }};
        obj.put(SENDER_LIST, sendersList);
        return JSONUtils.serialize(obj);
    }

    private String getOnSubscriptionRequest() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(SUBSCRIPTION_ID, "subscription_id-001");
        obj.put(SUBSCRIPTION_STATUS, 1);
        return JSONUtils.serialize(obj);
    }

    private ResultSet getOnSubscriptionResultSet() throws SQLException {
        return MockResultSet.createStringMock(
                new String[]{"subscription_id"}, //columns
                new Object[][]{ // data
                        {"subscription_id-001"}
                });
    }

    @Test
    void testNotificationOnSubscribeUpdateFailure() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        doReturn(getMockResultSet(1)).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getOnSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_ON_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertEquals(ErrorCodes.ERR_INVALID_SUBSCRIPTION_ID, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Unable to update record with subscription id"));
    }

    @Test
    void testNotificationOnSubscribeException() throws Exception {
        doThrow(new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR, "Test Internal Server Error")).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getOnSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_ON_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(ErrorCodes.INTERNAL_SERVER_ERROR, resObj.getError().getCode());
        assertEquals("Test Internal Server Error", resObj.getError().getMessage());
    }

    @Test
    void testNotificationOnSubscribeNullResponse() throws Exception {
        doReturn(getSubscriptionUpdateEmptyResultSet()).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getOnSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_ON_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(ErrorCodes.ERR_INVALID_SUBSCRIPTION_ID, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Invalid subscription id:"));
    }

    @Test
    void testNotificationOnSubscribeSuccess() throws Exception {
        doReturn(getMockResultSet(1),getOnSubscriptionResultSet()).when(postgreSQLClient).executeQuery(anyString());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
        doNothing().when(auditIndexer).createDocument(anyMap());
        String requestBody = getOnSubscriptionRequest();
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_ON_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals("subscription_id-001", resObj.getSubscriptionId());
    }

    @Test
    void testSubscriptionUpdateSuccess() throws Exception {
        doReturn(getSubscriptionUpdateResultSet()).when(postgreSQLClient).executeQuery(anyString());
        doReturn(getSubscriptionUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateRequest("notif-participant-onboarded", 1, true)).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertEquals(1, resObj.getSubscriptionStatus());
    }

    private String getSubscriptionUpdateRequest(String topicCode, int subscriptionStatus , Object isDelegated) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(RECIPIENT_CODE,"payor01@hcx");
        obj.put(TOPIC_CODE,topicCode);
        obj.put(SENDER_CODE,"provider01@hcx");
        obj.put(SUBSCRIPTION_STATUS, subscriptionStatus);
        obj.put(IS_DELEGATED, isDelegated);
        return JSONUtils.serialize(obj);
    }

    private ResultSet getSubscriptionUpdateResultSet() throws SQLException {
        return MockResultSet.createIntStringMock(
                new String[]{"subscription_id", "subscription_status"}, //columns
                new Object[][]{ // data
                        {"subscription-123", 1},
                        {"subscription-123", 1}
                });
    }

    private Map<String,Object> getSubscriptionUpdateAuditLog() throws Exception {
        return JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"edata\":{\"prevStatus\":1,\"status\":0,\"props\":[\"subscription_status\",\"expiry\",\"is_delegated\"]},\"ets\":1659434908868,\"mid\":\"5ee2b9e1-ded6-4b56-afa8-3380107632e0\",\"object\":{\"id\":\"097e0185-eeb1-48f1-b2b0-b68774d02c6d\",\"type\":\"notification\"},\"cdata\":{\"action\":\"/notification/subscription/update\",\"recipient_code\":\"testpayor1.icici@swasth-hcx-dev\",\"sender_code\":\"testprovider1.apollo@swasth-hcx-dev\"}}", Map.class);
    }

    @Test
    void testSubscriptionUpdateWithInvalidTopicCode() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateRequest("invalid-topic-123", 1, true)).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_TOPIC_CODE, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Topic code is empty or invalid"));
    }

    @Test
    void testSubscriptionUpdateWithNoUpdateProps() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateMissingFieldsRequest("notif-participant-onboarded")).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Nothing to update"));
    }

    private String getSubscriptionUpdateMissingFieldsRequest(String topicCode) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(RECIPIENT_CODE,"payor01@hcx");
        obj.put(TOPIC_CODE,topicCode);
        obj.put(SENDER_CODE,"provider01@hcx");
        return JSONUtils.serialize(obj);
    }

    @Test
    void testSubscriptionUpdateWithInvalidExpiry() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateInvalidExpiryRequest("notif-participant-onboarded")).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Expiry cannot be past date"));
    }

    private String getSubscriptionUpdateInvalidExpiryRequest(String topicCode) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(RECIPIENT_CODE,"payor01@hcx");
        obj.put(TOPIC_CODE,topicCode);
        obj.put(SENDER_CODE,"provider01@hcx");
        obj.put(SUBSCRIPTION_STATUS, 1);
        obj.put(IS_DELEGATED, true);
        obj.put(EXPIRY, System.currentTimeMillis());
        return JSONUtils.serialize(obj);
    }

    @Test
    void testSubscriptionUpdateWithInvalidSubscriptionStatus() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateRequest("notif-participant-onboarded", 2, true)).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Subscription status value is invalid"));
    }

    @Test
    void testSubscriptionUpdateWithInvalidIsDelegated() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateRequest("notif-participant-onboarded", 1, "test")).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Is delegated value is invalid"));
    }

    @Test
    void testSubscriptionUpdateWithInvalidSubscriptionDetails() throws Exception {
        doReturn(getSubscriptionUpdateEmptyResultSet()).when(postgreSQLClient).executeQuery(anyString());
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_SUBSCRIPTION_UPDATE)
                .content(getSubscriptionUpdateRequest("notif-participant-onboarded", 1, true)).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, resObj.getError().getCode());
        assertTrue(resObj.getError().getMessage().contains("Subscription does not exist"));
    }

    private ResultSet getSubscriptionUpdateEmptyResultSet() throws SQLException {
        return MockResultSet.createIntMock(
                new String[]{"subscription_id", "subscription_status"}, //columns
                new Object[][]{ // data
                        {"subscription-123", 1}
                });
    }

}
