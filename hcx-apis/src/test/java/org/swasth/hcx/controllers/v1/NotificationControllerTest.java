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
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
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
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
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
        doThrow(new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR,"Test Internal Server Error")).when(postgreSQLClient).executeBatch();
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
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
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
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
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
        doThrow(new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR,"Test Internal Server Error")).when(postgreSQLClient).executeBatch();
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
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
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
        assertEquals(0,resObj.getCount());
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
        assertEquals(1,resObj.getCount());
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
        assertEquals(1,resObj.getCount());
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
                new String[]{"subscription_id", "subscription_status", "topic_code","sender_code","recipient_code","expiry","is_delegated"}, //columns
                new Object[][]{ // data
                        {"subscription_id-001", status,"topic_code-12345", "hcx-apollo-12345","ICICI Lombard",1629057611000l,false}
                });
    }

    private ResultSet getMockResultSet() throws SQLException {
        return MockResultSet.create(
                new String[]{"subscription_id", "subscription_status", "topic_code","sender_code","recipient_code","expiry","is_delegated"}, //columns
                new Object[][]{ // data
                        {"subscription_id-001", 1,"topic_code-12345", "hcx-apollo-12345","ICICI Lombard",1629057611000l,false},
                        {"subscription_id-002", 0,"topic_code-12346", "hcx-apollo-12346","ICICI Lombard",1629057611000l,false},
                        {"subscription_id-003", -1,"topic_code-12347", "hcx-apollo-12347","ICICI Lombard",1629057611000l,true}
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
        Map<String,Object> obj = new HashMap<>();
        obj.put(FILTERS,new HashMap<>());
        obj.put(LIMIT, 10);
        obj.put(OFFSET,0);
        return JSONUtils.serialize(obj);
    }

    private String getNotificationListRequest() throws JsonProcessingException {
        Map<String,Object> filters = new HashMap<>();
        filters.put(PRIORITY, 0);
        return JSONUtils.serialize(Collections.singletonMap(FILTERS, filters));
    }

    private String getInvalidFilterRequest() throws JsonProcessingException {
        Map<String,Object> filters = new HashMap<>();
        filters.put("test", "123");
        return JSONUtils.serialize(Collections.singletonMap(FILTERS, filters));
    }

    private String getNotificationRequest(String topicCode, List<String> subscriptions) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, topicCode);
        obj.put(RECIPIENT_ROLES, List.of("provider","payor"));
        obj.put(RECIPIENT_CODES, List.of("test-user@hcx"));
        obj.put(SUBSCRIPTIONS, subscriptions);
        Map<String,Object> notificationData = new HashMap<>();
        notificationData.put("message","Payor system down for sometime");
        notificationData.put("duration","2hrs");
        notificationData.put("startTime","9PM");
        notificationData.put("date","26th April 2022 IST");
        obj.put(NOTIFICATION_DATA,notificationData);
        return JSONUtils.serialize(obj);
    }

    @Test
    void testNotifySuccess() throws Exception {
        doReturn(getSubscriptionsResultSet()).when(postgreSQLClient).executeQuery(anyString());
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getNotificationRequest("notification-123", List.of("subscription-123"));
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_NOTIFY).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertNotNull(resObj.getNotificationId());
    }

    @Test
    void testNotifyWithEmptySubscriptions() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getNotificationRequest("notification-123", Collections.EMPTY_LIST);
        MvcResult mvcResult = mockMvc.perform(post(VERSION_PREFIX + NOTIFICATION_NOTIFY).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertNotNull(resObj.getNotificationId());
    }

    @Test
    void testNotifyFailure() throws Exception {
        doReturn(getSubscriptionsResultSet()).when(postgreSQLClient).executeQuery(anyString());
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
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
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE,"be0e578d-b391-42f9-96f7-1e6bacd91c20");
        obj.put(RECIPIENT_CODE,"hcx-apollo-12345");
        List<String> sendersList = new ArrayList<>(){{
            add("payor1");add("payor2"); //1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e
        }};
        obj.put(SENDER_LIST,sendersList);
        return JSONUtils.serialize(obj);
    }

    private String getSubscriptionHcxRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE,"be0e578d-b391-42f9-96f7-1e6bacd91c20");
        obj.put(RECIPIENT_CODE,"hcx-apollo-12345");
        List<String> sendersList = new ArrayList<>(){{
            add("hcx-registry-code");
        }};
        obj.put(SENDER_LIST,sendersList);
        return JSONUtils.serialize(obj);
    }

    private String getOnePayorSubscriptionRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE,"NOTIFICATION@HCX01");
        obj.put(RECIPIENT_CODE,"hcx-apollo-12345");
        List<String> sendersList = new ArrayList<>(){{
            add("payor1");
        }};
        obj.put(SENDER_LIST,sendersList);
        return JSONUtils.serialize(obj);
    }

}
