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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.swasth.common.utils.Constants.*;

class NotificationControllerTest extends BaseSpec {

    @Test
     void testNotificationSubscribeSuccessForHCX() throws Exception {
        doReturn(true).when(postgreSQLClient).execute(anyString());
        String requestBody = getSubscriptionRequest("hcx-registry-code");
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(resObj.getApiCallId(),"1fa85f64-5717-4562-b3fc-2c963f66afa6");
        assertEquals(resObj.getCorrelationId(),"2fa85f64-5717-4562-b3fc-2c963f66afa6");
    }

    @Test
    void testNotificationSubscribeSuccessForOthers() throws Exception {
        doReturn(true).when(postgreSQLClient).execute(anyString());
        String requestBody = getSubscriptionRequest("hcx-star-insurance-001");
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(resObj.getApiCallId(),"1fa85f64-5717-4562-b3fc-2c963f66afa6");
        assertEquals(resObj.getCorrelationId(),"2fa85f64-5717-4562-b3fc-2c963f66afa6");
    }

    private String getSubscriptionRequest(String recipientCode) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(NOTIFICATION_ID,"24e975d1-054d-45fa-968e-c91b1043d0a5");
        obj.put(SENDER_CODE,"hcx-apollo-12345");
        obj.put(RECIPIENT_CODE,recipientCode);
        obj.put(API_CALL_ID,"1fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(CORRELATION_ID,"2fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(WORKFLOW_ID,"3fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(TIMESTAMP,"1629057611000");
        obj.put(STATUS,"request.queued");
        return JSONUtils.serialize(obj);
    }

    @Test
    void testNotificationUnSubscribeSuccess() throws Exception {
        doReturn(true).when(postgreSQLClient).execute(anyString());
        String requestBody = getSubscriptionRequest("hcx-registry-code");
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_UNSUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(resObj.getApiCallId(),"1fa85f64-5717-4562-b3fc-2c963f66afa6");
        assertEquals(resObj.getCorrelationId(),"2fa85f64-5717-4562-b3fc-2c963f66afa6");
    }

    @Test
    void testNotificationSubscribeException() throws Exception {
        doThrow(new ClientException(ErrorCodes.INTERNAL_SERVER_ERROR,"Test Internal Server Error")).when(postgreSQLClient).execute(anyString());
        String requestBody = getSubscriptionRequest("hcx-registry-code");
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(resObj.getError().getCode(),ErrorCodes.INTERNAL_SERVER_ERROR);
        assertEquals(resObj.getError().getMessage(),"Test Internal Server Error");
    }

    @Test
    void testSubscriptionListEmpty() throws Exception {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(resObj.getSubscriptions().isEmpty());
        assertEquals(0,resObj.getSubscriptionCount());
    }

    @Test
    void testSubscriptionListWithOneActiveSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet(1);
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertEquals(1,resObj.getSubscriptionCount());
        assertEquals("hcx-notification-001:hcx-apollo-12345", resObj.getSubscriptions().get(0).getSubscriptionId());
        assertEquals("hcx-notification-001", resObj.getSubscriptions().get(0).getNotificationId());
        assertEquals(ACTIVE, resObj.getSubscriptions().get(0).getStatus());
    }

    @Test
    void testSubscriptionListWithOneInActiveSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet(0);
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertEquals(1,resObj.getSubscriptionCount());
        assertEquals("hcx-notification-001:hcx-apollo-12345", resObj.getSubscriptions().get(0).getSubscriptionId());
        assertEquals("hcx-notification-001", resObj.getSubscriptions().get(0).getNotificationId());
        assertEquals(IN_ACTIVE, resObj.getSubscriptions().get(0).getStatus());

    }

    @Test
    void testSubscriptionListWithBothSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet();
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(200, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertEquals(2, resObj.getSubscriptionCount());
        assertEquals("hcx-notification-001:hcx-apollo-12345", resObj.getSubscriptions().get(0).getSubscriptionId());
        assertEquals("hcx-notification-001", resObj.getSubscriptions().get(0).getNotificationId());
        assertEquals(ACTIVE, resObj.getSubscriptions().get(0).getStatus());
        assertEquals("hcx-notification-002:hcx-apollo-12345", resObj.getSubscriptions().get(1).getSubscriptionId());
        assertEquals("hcx-notification-002", resObj.getSubscriptions().get(1).getNotificationId());
        assertEquals(IN_ACTIVE, resObj.getSubscriptions().get(1).getStatus());

    }

    @Test
    void testSubscriptionListException() throws Exception {
        doThrow(Exception.class).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getSubscriptionListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIPTION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
        assertTrue(response.getContentAsString().contains(ErrorCodes.INTERNAL_SERVER_ERROR.name()));
    }

    @Test
    void testNotificationListEmpty() throws Exception {
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void testNotificationListData() throws Exception {
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertFalse(resObj.getNotifications().isEmpty());
    }

    private ResultSet getMockResultSet(int status) throws SQLException {
        return MockResultSet.create(
                new String[]{"notificationId", "recipientId", "status","mode"}, //columns
                new Object[][]{ // data
                        {"hcx-notification-001", "hcx-apollo-12345", status,"API"}
                });
    }

    private ResultSet getMockResultSet() throws SQLException {
        return MockResultSet.create(
                new String[]{"notificationId", "recipientId", "status","mode"}, //columns
                new Object[][]{ // data
                        {"hcx-notification-001", "hcx-apollo-12345", 1,"API"},
                        {"hcx-notification-002", "hcx-apollo-12345", 0,"API"}
                });
    }

    private String getSubscriptionListRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(SENDER_CODE,"hcx-apollo-12345");
        return JSONUtils.serialize(obj);
    }

    private String getNotificationListRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        return JSONUtils.serialize(obj);
    }

    private String getNotificationRequest(String notificationId) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(NOTIFICATION_ID,notificationId);
        obj.put(SENDER_CODE,"hcx-apollo-12345");
        obj.put(RECIPIENT_CODE,"hcx-star-insurance-001");
        obj.put(API_CALL_ID,"1fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(CORRELATION_ID,"2fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(WORKFLOW_ID,"3fa85f64-5717-4562-b3fc-2c963f66afa6");
        obj.put(TIMESTAMP,"1629057611000");
        obj.put(STATUS,"request.queued");
        Map<String,Object> notificationData = new HashMap<>();
        notificationData.put("message","Payor system down for sometime");
        notificationData.put("duration","2hrs");
        notificationData.put("startTime","9PM");
        notificationData.put("date","26th April 2022 IST");
        obj.put(NOTIFICATION_DATA,notificationData);
        return JSONUtils.serialize(obj);
    }

    @Test
    public void testNotificationRequestSuccess() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getNotificationRequest("24e975d1-054d-45fa-968e-c91b1043d0a5");
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_REQUEST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertEquals("1fa85f64-5717-4562-b3fc-2c963f66afa6",resObj.getApiCallId());
        assertEquals("2fa85f64-5717-4562-b3fc-2c963f66afa6",resObj.getCorrelationId());
    }

    @Test
    public void testNotificationRequestFailure() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getNotificationRequest("hcx-notification-001");
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_REQUEST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertEquals(resObj.getError().getCode(),ErrorCodes.ERR_INVALID_NOTIFICATION_ID);
        assertTrue(resObj.getError().getMessage().contains("Invalid NotificationId."));
    }

}
