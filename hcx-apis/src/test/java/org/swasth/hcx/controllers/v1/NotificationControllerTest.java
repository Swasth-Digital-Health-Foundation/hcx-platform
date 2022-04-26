package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.utils.MockResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.swasth.common.utils.Constants.*;

public class NotificationControllerTest extends BaseSpec {

    @Test
    public void testNotificationSubscribeSuccess() throws Exception {
        doReturn(true).when(postgreSQLClient).execute(anyString());
        String requestBody = getNotificationRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_SUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        assertTrue(response.getContentAsString().contains("hcx-notification-001:hcx-apollo-12345"));
        assertTrue(response.getContentAsString().contains(ACTIVE));
    }

    private String getNotificationRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(PARTICIPANT_CODE,"hcx-apollo-12345");
        obj.put(NOTIFICATION_ID,"hcx-notification-001");
        return JSONUtils.serialize(obj);
    }

    @Test
    public void testNotificationUnSubscribeSuccess() throws Exception {
        doReturn(true).when(postgreSQLClient).execute(anyString());
        String requestBody = getNotificationRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_UNSUBSCRIBE).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
        assertTrue(response.getContentAsString().contains("hcx-notification-001:hcx-apollo-12345"));
        assertTrue(response.getContentAsString().contains(IN_ACTIVE));
    }

    @Test
    public void testNotificationListEmpty() throws Exception {
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertTrue(resObj.getSubscriptions().isEmpty());
        assertTrue(resObj.getSubscriptionCount() == 0);
    }

    @Test
    public void testNotificationListWithOneActiveSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet(1);

        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertTrue(resObj.getSubscriptionCount() == 1);
        assertEquals("hcx-notification-001:hcx-apollo-12345", resObj.getSubscriptions().get(0).getSubscriptionId());
        assertEquals("hcx-notification-001", resObj.getSubscriptions().get(0).getNotificationId());
        assertEquals(ACTIVE, resObj.getSubscriptions().get(0).getStatus());
    }

    @Test
    public void testNotificationListWithOneInActiveSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet(0);

        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertTrue(resObj.getSubscriptionCount() == 1);
        assertEquals("hcx-notification-001:hcx-apollo-12345", resObj.getSubscriptions().get(0).getSubscriptionId());
        assertEquals("hcx-notification-001", resObj.getSubscriptions().get(0).getNotificationId());
        assertEquals(IN_ACTIVE, resObj.getSubscriptions().get(0).getStatus());

    }

    @Test
    public void testNotificationListWithBothSubscription() throws Exception {
        ResultSet mockResultSet = getMockResultSet();

        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        String requestBody = getNotificationListRequest();
        MvcResult mvcResult = mockMvc.perform(post(NOTIFICATION_LIST).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(202, status);
        assertTrue(!resObj.getSubscriptions().isEmpty());
        assertTrue(resObj.getSubscriptionCount() == 2);
        assertEquals("hcx-notification-001:hcx-apollo-12345", resObj.getSubscriptions().get(0).getSubscriptionId());
        assertEquals("hcx-notification-001", resObj.getSubscriptions().get(0).getNotificationId());
        assertEquals(ACTIVE, resObj.getSubscriptions().get(0).getStatus());
        assertEquals("hcx-notification-002:hcx-apollo-12345", resObj.getSubscriptions().get(1).getSubscriptionId());
        assertEquals("hcx-notification-002", resObj.getSubscriptions().get(1).getNotificationId());
        assertEquals(IN_ACTIVE, resObj.getSubscriptions().get(1).getStatus());

    }

    private ResultSet getMockResultSet(int status) throws SQLException {
        return MockResultSet.create(
                new String[]{"notificationid", "recipientid", "status"}, //columns
                new Object[][]{ // data
                        {"hcx-notification-001", "hcx-apollo-12345", status}
                });
    }

    private ResultSet getMockResultSet() throws SQLException {
        return MockResultSet.create(
                new String[]{"notificationid", "recipientid", "status"}, //columns
                new Object[][]{ // data
                        {"hcx-notification-001", "hcx-apollo-12345", 1},
                        {"hcx-notification-002", "hcx-apollo-12345", 0}
                });
    }

    private String getNotificationListRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(PARTICIPANT_CODE,"hcx-apollo-12345");
        return JSONUtils.serialize(obj);
    }

}
