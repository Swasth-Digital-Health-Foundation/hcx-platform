package org.swasth.hcx.controllers.v1;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.Response;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class UserControllerTests extends BaseSpec {

    private MockWebServer registryServer =  new MockWebServer();

    @BeforeEach
    public void start() throws IOException {
        registryServer.start(InetAddress.getByName("localhost"),8082);
    }

    @AfterEach
    public void teardown() throws IOException, InterruptedException {
        registryServer.shutdown();
        Thread.sleep(2000);
    }

      @Test
      void user_create_success_scenario() throws Exception {
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("[{\"user_name\":\"test user\",\"tenant_roles\": [ {\"participant_code\":\"testprovider1.apollo@swasth-hcx-dev\",\"role\":\"admin\"}]}]")
                  .addHeader("Content-Type", "application/json"));
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("[]")
                  .addHeader("Content-Type", "application/json"));
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"User\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                  .addHeader("Content-Type", "application/json"));
          doReturn(getUserCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
          MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_CREATE).content(getUserCreateBody()).header(HttpHeaders.AUTHORIZATION, getUserUpdatedToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
          MockHttpServletResponse response = mvcResult.getResponse();
          int status = response.getStatus();
          assertEquals(200, status);
      }

      @Test
      void user_create_with_mobile_success_scenario() throws Exception {
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("[{\"user_name\":\"test user\",\"tenant_roles\": [ {\"participant_code\":\"testprovider1.apollo@swasth-hcx-dev\",\"role\":\"admin\"}]}]")
                  .addHeader("Content-Type", "application/json"));
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("[]")
                  .addHeader("Content-Type", "application/json"));
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"User\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                  .addHeader("Content-Type", "application/json"));
          doReturn(getUserCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
          MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_CREATE).content(getUserCreateBodyWithMobile()).header(HttpHeaders.AUTHORIZATION, getUserUpdatedToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
          MockHttpServletResponse response = mvcResult.getResponse();
          int status = response.getStatus();
          assertEquals(200, status);
      }

    @Test
    void user_create_with_admin_token_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"user_name\":\"test user\",\"tenant_roles\": [ {\"participant_code\":\"testprovider1.apollo@swasth-hcx-dev\",\"role\":\"admin\"}]}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"User\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        doReturn(getUserCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_CREATE).content(getUserCreateBody()).header(HttpHeaders.AUTHORIZATION, getAdminToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void user_create_with_participant_token_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"9620499129\", \"primary_email\": \"testprovider1@apollo.com\", \"roles\": [ \"provider\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"8527853c-b442-44db-aeda-dbbdcf472d9b\" ], \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"User\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        doReturn(getUserCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_CREATE).content(getUserCreateBody()).header(HttpHeaders.AUTHORIZATION, getNewParticipantToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void user_create_with_invalid_mobile_and_phone_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"9620499129\", \"primary_email\": \"testprovider1@apollo.com\", \"roles\": [ \"provider\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"8527853c-b442-44db-aeda-dbbdcf472d9b\" ], \"participant_code\": \"testprovider1.apollo@swasth-hcx-dev\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"User\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        doReturn(getUserCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_CREATE).content(getUserCreateInvalidMobilePhone()).header(HttpHeaders.AUTHORIZATION, getNewParticipantToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

      @Test
      void user_create_internal_server_scenario() throws Exception {
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(500)
                  .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1653306628400, \"params\": { \"resmsgid\": \"\", \"msgid\": \"90546fa3-1bd7-4072-bd06-81ea688ea9af\", \"err\": \"\", \"status\": \"UNSUCCESSFUL\", \"errmsg\": \"Username already invited / registered for Organisation\" }, \"responseCode\": \"OK\" }")
                  .addHeader("Content-Type", "application/json"));
          MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_CREATE).content(getUserCreateBody()).header(HttpHeaders.AUTHORIZATION,getUserUpdatedToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
          MockHttpServletResponse response = mvcResult.getResponse();
          int status = response.getStatus();
          assertEquals(500, status);
      }

     @Test
     void user_search_success_scenario_with_admin_token() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                 .addHeader("Content-Type", "application/json"));
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_SEARCH).content(getUserSearchFilter()).header(HttpHeaders.AUTHORIZATION,getAdminToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(200, status);
     }

    @Test
    void user_search_success_scenario_with_user_token() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_SEARCH).content(getUserSearchFilter()).header(HttpHeaders.AUTHORIZATION,getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void user_search_invalid_scenario_with_user_token() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_SEARCH).content(getUserInvalidSearchFilter()).header(HttpHeaders.AUTHORIZATION,getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

     @Test
     void user_search_invalid_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(500)
                 .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                 .addHeader("Content-Type", "application/json"));
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_SEARCH).content(getUserSearchNotfoundFilter()).header(HttpHeaders.AUTHORIZATION,getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(400, status);
     }

      @Test
      void user_read_success_scenario() throws Exception {
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(200)
                  .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                  .addHeader("Content-Type", "application/json"));
          MvcResult mvcResult = mockMvc.perform(get(Constants.VERSION_PREFIX + "/user/read/test-user-89.gmail@swasth-hcx").content(getUserSearchFilter()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
          MockHttpServletResponse response = mvcResult.getResponse();
          int status = response.getStatus();
          assertEquals(200, status);
      }

      @Test
      void user_read_invalid_scenario() throws Exception {
          registryServer.enqueue(new MockResponse()
                  .setResponseCode(500)
                  .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                  .addHeader("Content-Type", "application/json"));
          MvcResult mvcResult = mockMvc.perform(get(Constants.VERSION_PREFIX + "/user/read/test-user-89@gmail.com").content(getUserSearchFilter()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
          MockHttpServletResponse response = mvcResult.getResponse();
          int status = response.getStatus();
          assertEquals(500, status);
      }

     @Test
     void user_update_success_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\"}]")
                 .addHeader("Content-Type", "application/json"));
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("{ \"message\": \"success\" }")
                 .addHeader("Content-Type", "application/json"));
         Mockito.when(redisCache.isExists(any())).thenReturn(true);
         doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_UPDATE).content(getUserUpdateBody()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(200, status);
     }
     @Test
    void user_update_not_allowed_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\"}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_UPDATE).content(getUserUpdateNotAllowedBody()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
     void user_update_invalid_user_id() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[]")
                 .addHeader("Content-Type", "application/json"));
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_UPDATE).content(getInvalidUserId()).header(HttpHeaders.AUTHORIZATION,getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
         assertNotNull(resObj.getTimestamp());
         assertEquals(400, status);
         assertEquals(ErrorCodes.ERR_INVALID_USER_ID, resObj.getError().getCode());
         assertEquals("Please provide valid user id", resObj.getError().getMessage());
     }

     @Test
     void user_update_internal_server_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(500)
                 .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"INTERNAL SERVER ERROR\" } } }")
                 .addHeader("Content-Type", "application/json"));
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_UPDATE).content(getUserUpdateBody()).header(HttpHeaders.AUTHORIZATION,getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(500, status);
     }
    @Test
    void user_delete_internal_server_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"INTERNAL SERVER ERROR\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_DELETE).content(getUserUpdateBody()).header(HttpHeaders.AUTHORIZATION,getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }
     @Test
     void user_delete_success() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{\"osid\":\"1-68c5deca-8299-4feb-b441-923bb649a9a3\", \"status\":\"Created\"}]")
                 .addHeader("Content-Type", "application/json"));
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("{ \"id\": \"sunbird-rc.registry.delete\", \"ver\": \"1.0\", \"ets\": 1660126601629, \"params\": { \"resmsgid\": \"\", \"msgid\": \"4d5e904d-205c-4e07-a078-a2f213f3c5ed\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\" }")
                 .addHeader("Content-Type", "application/json"));
         Mockito.when(redisCache.isExists(any())).thenReturn(true);
         doReturn(getUserDeleteAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_DELETE).content(getUserUpdateBody()).header(HttpHeaders.AUTHORIZATION,getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(200, status);
     }

     @Test
     void user_delete_invalid_participant_code() throws Exception {
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_DELETE).content(getEmptyBody()).header(HttpHeaders.AUTHORIZATION,getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
         assertEquals(400, status);
         assertEquals(ErrorCodes.ERR_INVALID_USER_ID, resObj.getError().getCode());
         assertEquals("Please provide valid user id", resObj.getError().getMessage());
     }

     @Test
     void participant_add_user_success_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{ \"user_name\": \"test-abhiAdmin\",\"created_by\":\"test-123.yopmail@swasth-hcx\", \"mobile\": \"9620499129\", \"email\": \"test-123@yopmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-123.yopmail@swasth-hcx\",\"role\":\"admin\"}], \"osOwner\": [ \"1f6ec973-5a01-4889-93ac-a22d004081ac\" ], \"user_id\": \"test-123@yopmail.com\",\"osid\":\"35e76122-84e4-4740-94cd-f875e32f2c34\" }]")
                 .addHeader("Content-Type", "application/json"));
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("{ \"message\": \"success\" }")
                 .addHeader("Content-Type", "application/json"));
         doNothing().when(keycloakApiAccessService).addUserWithParticipant(anyString(),anyString(),anyString());
         Mockito.when(redisCache.isExists(any())).thenReturn(true);
         doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_ADD).content(getParticipantAddBody()).header(HttpHeaders.AUTHORIZATION, getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(200, status);
     }

    @Test
    void participant_add_user_partial_status_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-abhiAdmin\",\"created_by\":\"test-123.yopmail@swasth-hcx\", \"mobile\": \"9620499129\", \"email\": \"test-123@yopmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-123.yopmail@swasth-hcx\",\"role\":\"admin\"}], \"osOwner\": [ \"1f6ec973-5a01-4889-93ac-a22d004081ac\" ], \"user_id\": \"test-123@yopmail.com\",\"osid\":\"35e76122-84e4-4740-94cd-f875e32f2c34\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-abhiAdmin\",\"created_by\":\"test-123.yopmail@swasth-hcx\", \"mobile\": \"9620499129\", \"email\": \"test-456@yopmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-123.yopmail@swasth-hcx\",\"role\":\"admin\"}], \"osOwner\": [ \"1f6ec973-5a01-4889-93ac-a22d004081ac\" ], \"user_id\": \"test-456@yopmail.com\",\"osid\":\"35e76122-84e4-4740-94cd-f875e32f2c34\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        doNothing().when(keycloakApiAccessService).addUserWithParticipant(anyString(),anyString(),anyString());
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_ADD).content(getParticipantAddBodyPartialStatus()).header(HttpHeaders.AUTHORIZATION, getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(206, status);
    }

    @Test
    void participant_add_user_role_exist_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-abhiAdmin\",\"created_by\":\"test-123.yopmail@swasth-hcx\", \"mobile\": \"9620499129\", \"email\": \"test-123@yopmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-123.yopmail@swasth-hcx\",\"role\":\"admin\"}], \"osOwner\": [ \"1f6ec973-5a01-4889-93ac-a22d004081ac\" ], \"user_id\": \"test-123@yopmail.com\",\"osid\":\"35e76122-84e4-4740-94cd-f875e32f2c34\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        doNothing().when(keycloakApiAccessService).addUserWithParticipant(anyString(),anyString(),anyString());
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_ADD).content(getParticipantAddBodyRoleExist()).header(HttpHeaders.AUTHORIZATION, getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

     @Test
     void participant_add_user_invalid_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"testprovider1.apollo@swasth-hcx-dev\",\"role\":\"admin\"}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                 .addHeader("Content-Type", "application/json"));
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("{ \"message\": \"success\" }")
                 .addHeader("Content-Type", "application/json"));
         Mockito.when(redisCache.isExists(any())).thenReturn(true);
         doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_ADD).content(getParticipantAddBody()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(400, status);
     }

    @Test
    void participant_remove_user_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"testprovider1.apollo@swasth-hcx-dev\",\"role\":\"config-manger\"}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        doNothing().when(keycloakApiAccessService).removeUserWithParticipant(anyString(),anyString());
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_REMOVE).content(getParticipantAddBody()).header(HttpHeaders.AUTHORIZATION, getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }


     @Test
     void participant_remove_user_invalid_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-@swasth\",\"role\":\"admin\"}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                 .addHeader("Content-Type", "application/json"));
         Mockito.when(redisCache.isExists(any())).thenReturn(true);
         doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_REMOVE).content(getParticipantAddBody()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(400, status);
     }

     @Test
     void participant_remove_user_empty_tenant_body() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"test-user-89@gmail.com\", \"tenant_roles\": [], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                 .addHeader("Content-Type", "application/json"));
         Mockito.when(redisCache.isExists(any())).thenReturn(true);
         doReturn(getUserUpdateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
         MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_REMOVE).content(getParticipantAddBody()).header(HttpHeaders.AUTHORIZATION, getAddUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(400, status);
     }
}