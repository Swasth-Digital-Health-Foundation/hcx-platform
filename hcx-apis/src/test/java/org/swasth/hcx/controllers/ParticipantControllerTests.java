package org.swasth.hcx.controllers;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .setResponseCode(404)
                .setBody("[ ]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/search").content(getSearchNotFoundFilter()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(404, status);
    }

    @Test
    void participant_search_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/search").content(getSearchFilter()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_create_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_create_bad_request_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_create_internal_server_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
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
        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantPayorBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
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
        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantPayorSchemeBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_create_payor_scheme_not_allowed_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{ \"id\": \"open-saber.registry.invite\", \"ver\": \"1.0\", \"ets\": 1637227738534, \"params\": { \"resmsgid\": \"\", \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\", \"err\": \"\", \"status\": \"SUCCESSFUL\", \"errmsg\": \"\" }, \"responseCode\": \"OK\", \"result\": { \"Organisation\": { \"osid\": \"1-17f02101-b560-4bc1-b3ab-2dac04668fd2\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantPayorSchemeNotAllowedBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    void participant_update_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"message\": \"success\" }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/update").content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void participant_update_not_found_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"NOT_FOUND\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/update").content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(404, status);
    }

    @Test
    void participant_update_un_authorize_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"UN AUTHORIZED\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/update").content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(401, status);
    }

    @Test
    void participant_update_internal_server_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"params\": { \"msgid\": \"bb355e26-cc12-4aeb-8295-03347c428c62\",\"errmsg\": \"INTERNAL SERVER ERROR\" } } }")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post("/participant/update").content(getParticipantUpdateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }
}
