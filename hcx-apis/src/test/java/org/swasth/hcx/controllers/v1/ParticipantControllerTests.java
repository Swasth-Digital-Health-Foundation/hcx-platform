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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ParticipantControllerTests extends BaseSpec{

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
    void participant_search_not_found_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"id\": \"open-saber.registry.search\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_SEARCH).content(getSearchNotFoundFilter()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    void participant_search_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_SEARCH).content(getSearchFilter()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_create_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"paticipant_name\":\"test user\"}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        doReturn(getParticipantCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(), anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString(), anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_create_invalid_encryption_cert() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));
        doNothing().when(cloudStorageClient).putObject(anyString(),anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(),anyString(),anyString());
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(),anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content("{ \"participant_name\": \"Apollo Hospital\", \"primary_mobile\": \"6300009626\", \"primary_email\": \"Apollohospital@gmail.com\", \"roles\": [\"provider\"], \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"locality\": \"Nampally\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"https://677e6fd9-57cc-466c-80f6-ae0462762872.mock.pstmn.io\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ] }").header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS.name(), getResponseErrorCode(responseBody));
        assertEquals("Property 'encryption_cert' is missing or invalid", getResponseErrorMessage(responseBody));
    }

    @Test
    void participant_create_internal_server_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1653306628400, \"params\": { \"resmsgid\": \"\", \"msgid\": \"90546fa3-1bd7-4072-bd06-81ea688ea9af\", \"err\": \"\", \"status\": \"UNSUCCESSFUL\", \"errmsg\": \"Username already invited / registered for Organisation\" }, \"responseCode\": \"OK\" }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    void participant_create_payor_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content(getParticipantPayorBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_create_payor_scheme_missing_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content(getParticipantPayorSchemeBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_create_payor_scheme_not_allowed_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content(getParticipantPayorSchemeNotAllowedBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_create_endpoint_url_not_allowed_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content(getParticipantUrlNotAllowedBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_create_invalid_roles_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content("{ \"participant_name\": \"Apollo Hospital\", \"primary_mobile\": \"6300009626\", \"primary_email\": \"Apollohospital@gmail.com\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"locality\": \"Nampally\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"https://677e6fd9-57cc-466c-80f6-ae0462762872.mock.pstmn.io\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\" }").header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        int status = response.getStatus();
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS.name(), getResponseErrorCode(responseBody));
        assertEquals("Roles property cannot be null, empty or other than 'ArrayList'", getResponseErrorMessage(responseBody));
    }

    @Test
    void participant_create_invalid_primary_email_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_CREATE).content("{ \"participant_name\": \"test-user\", \"primary_mobile\": \"6300009626\", \"primary_email\": \"\", \"roles\": [ \"provider\" ], \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"locality\": \"Nampally\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"https://677e6fd9-57cc-466c-80f6-ae0462762872.mock.pstmn.io\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\" }").header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        int status = response.getStatus();
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_PARTICIPANT_DETAILS.name(), getResponseErrorCode(responseBody));
        assertEquals("Property 'primary_email' is missing or invalid", getResponseErrorMessage(responseBody));
    }

    @Test
    void participant_update_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"osid\":\"1-68c5deca-8299-4feb-b441-923bb649a9a3\",\"osOwner\":[\"0c5763de-732d-4fd4-8547-b3960c1320f5\"]}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getParticipantCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString(), anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString());
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(), anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_update_invalid_participant_code() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(),anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertNotNull(resObj.getTimestamp());
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, resObj.getError().getCode());
        assertEquals("Please provide valid participant code", resObj.getError().getMessage());
    }

    @Test
    void participant_update_not_found_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"osid\":\"1-68c5deca-8299-4feb-b441-923bb649a9a3\",\"osOwner\":[\"0c5763de-732d-4fd4-8547-b3960c1320f5\"]}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"NOT_FOUND\" } } }")
                .addHeader("Content-Type", "application/json"));
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(),anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(404, status);
    }

    @Test
    void participant_update_un_authorize_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"primary_email\":\"provider01@gmail.com\", \"osid\":\"1-68c5deca-8299-4feb-b441-923bb649a9a3\",\"osOwner\":[\"a507e23b-8280-46a7-b9e1-79afc7f864fc\"]}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString(), anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString());
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(), anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION, getUserToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_update_user_token_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"primary_email\":\"testprovider1@apollo.com\", \"osid\":\"testprovider1.apollo@swasth-hcx-dev\",\"osOwner\":[\"df4597b7-6fb5-4aac-ad03-5199e5c5ea15\"]}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getParticipantCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString(), anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString());
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(), anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateUserTokenBody()).header(HttpHeaders.AUTHORIZATION, getUserUpdatedToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_update_user_token_invalid_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"primary_email\":\"provider01@gmail.com\", \"osid\":\"1-68c5deca-8299-4feb-b441-923bb649a9a3\",\"osOwner\":[\"8527853c-b442-44db-aeda-dbbdcf472db\"]}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getParticipantCreateAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString(), anyString());
        doNothing().when(cloudStorageClient).putObject(anyString(), anyString());
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(), anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION, getNewParticipantToken()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_update_internal_server_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"INTERNAL SERVER ERROR\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    void participant_update_endpoint_url_not_allowed_scenario() throws Exception {
        doReturn(getUrl()).when(cloudStorageClient).getUrl(anyString(),anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_UPDATE).content(getParticipantUrlNotAllowedBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_delete_success() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"osid\":\"1-68c5deca-8299-4feb-b441-923bb649a9a3\", \"status\":\"Created\"}]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"id\": \"sunbird-rc.registry.delete\", \"ver\": \"1.0\", \"ets\": 1660126601629, \"params\": { \"resmsgid\": \"\", \"msgid\": \"4d5e904d-205c-4e07-a078-a2f213f3c5ed\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\" }")
                .addHeader("Content-Type", "application/json"));
        Mockito.when(redisCache.isExists(any())).thenReturn(true);
        doReturn(getParticipantDeleteAuditLog()).when(mockEventGenerator).createAuditLog(anyString(), anyString(), anyMap(), anyMap());
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_DELETE).content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_delete_invalid_participant_code() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_DELETE).content(getEmptyBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
        assertEquals(400, status);
        assertEquals(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, resObj.getError().getCode());
        assertEquals("Please provide valid participant code", resObj.getError().getMessage());
    }


     @Test
     void participant_read_success_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"testuser3@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]")
                 .addHeader("Content-Type", "application/json"));
         MvcResult mvcResult = mockMvc.perform(get(Constants.VERSION_PREFIX + "/participant/read/d2d56996-1b77-4abb-b9e9-0e6e7343c72e").header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         assertEquals(200, status);
     }

     @Test
     void participant_read_invalid_scenario() throws Exception {
         registryServer.enqueue(new MockResponse()
                 .setResponseCode(200)
                 .setBody("[]")
                 .addHeader("Content-Type", "application/json"));
         MvcResult mvcResult = mockMvc.perform(get(Constants.VERSION_PREFIX + "/participant/read/d2d56996-1b77-4abb-b9e9-0e6e7343c72e").content(getSearchFilter()).header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
         MockHttpServletResponse response = mvcResult.getResponse();
         int status = response.getStatus();
         Response resObj = JSONUtils.deserialize(response.getContentAsString(), Response.class);
         assertEquals(400, status);
         assertEquals(ErrorCodes.ERR_INVALID_PARTICIPANT_CODE, resObj.getError().getCode());
         assertEquals("Please provide valid participant code", resObj.getError().getMessage());
     }
}