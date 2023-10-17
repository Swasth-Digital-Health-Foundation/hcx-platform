package org.swasth.hcx.controllers.v1;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.services.OnboardService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.swasth.common.utils.Constants.SUCCESSFUL;

class OnboardControllerTests extends BaseSpec{
    @Autowired
    private OnboardController onboardController;
    @Mock
    private OnboardService onboardService;

    @Test
    public void testVerify() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                                "    \"timestamp\": 1697191734176,\n" +
                                "    \"participants\": [\n" +
                                "        {\n" +
                                "            \"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/signing_cert_path.pem\",\n" +
                                "            \"participant_name\": \"wemeanhospital Mock Payor\",\n" +
                                "            \"endpoint_url\": \"http://a6a5d9138995a45b2bf9fd3f72b84367-915129339.ap-south-1.elb.amazonaws.com:8080/v0.7\",\n" +
                                "            \"roles\": [\n" +
                                "                \"payor\"\n" +
                                "            ],\n" +
                                "            \"scheme_code\": \"default\",\n" +
                                "            \"primary_email\": \"wemeanhospital+mock_payor@yopmail.com\",\n" +
                                "            \"encryption_cert\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/wemeanhospital%2Bmock_payor.yopmail%40swasth-hcx-dev/encryption_cert.pem\",\n" +
                                "            \"status\": \"Active\",\n" +
                                "            \"participant_code\": \"wemeanhospital+mock_payor.yopmail@swasth-hcx-dev\",\n" +
                                "            \"sigining_cert_expiry\": 1779007885000,\n" +
                                "            \"encryption_cert_expiry\": 1779007885000,\n" +
                                "            \"osOwner\": [\n" +
                                "                \"62c12021-eb1a-49ff-9496-d7a65a616930\"\n" +
                                "            ],\n" +
                                "            \"osCreatedAt\": \"2023-05-18T08:51:30.271Z\",\n" +
                                "            \"osUpdatedAt\": \"2023-09-14T05:34:07.932Z\",\n" +
                                "            \"osid\": \"93dac853-9089-4df6-9cbe-6b4b9acdc27e\",\n" +
                                "            \"@type\": \"Organisation\",\n" +
                                "            \"primary_mobile\": \"\"\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"participant_code\": \"provider.clinic_test_u_312076@swasth-hcx-dev\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                        .setBody("{\n" +
                                "    \"timestamp\": 1697200288894,\n" +
                                "    \"users\": []\n" +
                                "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697200288894,\n" +
                        "    \"users\": []\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"user_id\": \"obama02@yopmail.com\"}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"result\": [\n" +
                        "        {\n" +
                        "            \"user_id\": \"obama02@yopmail.com\",\n" +
                        "            \"status\": \"successful\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"overallStatus\": \"successful\",\n" +
                        "    \"timestamp\": 1697199663810\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying,    createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        String requestBody = verifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_onboard_update() throws Exception {
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697524745246,\n" +
                        "    \"result\": {\n" +
                        "        \"identity_verification\": \"rejected\",\n" +
                        "        \"email_verified\": true,\n" +
                        "        \"phone_verified\": true,\n" +
                        "        \"participant_code\": \"test_user_54.yopmail@swasth-hcx\",\n" +
                        "        \"communication_verification\": \"successful\"\n" +
                        "    },\n" +
                        "    \"status\": \"SUCCESSFUL\"\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        hcxApiServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\n" +
                        "    \"timestamp\": 1697523436655,\n" +
                        "    \"participants\": [\n" +
                        "        {\n" +
                        "            \"primary_email\": \"test_user-hcx-5@yopmail.com\",\n" +
                        "            \"primary_mobile\": \"9620499129\",\n" +
                        "            \"roles\": [\n" +
                        "                \"payor\"\n" +
                        "            ],\n" +
                        "            \"participant_name\": \"test_user-hcx-5\",\n" +
                        "            \"endpoint_url\": \"http://testurl/v0.7\",\n" +
                        "            \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-27/hcx-apis/src/test/resources/examples/x509-self-signed-certificate.pem\",\n" +
                        "            \"status\": \"Created\",\n" +
                        "            \"scheme_code\": \"default\",\n" +
                        "            \"encryption_cert_expiry\": 1666612517000,\n" +
                        "            \"participant_code\": \"test_user-hcx-5.yopmail@swasth-hcx\",\n" +
                        "            \"osOwner\": [\n" +
                        "                \"9306930e-ac74-4d52-9f75-bfd5ca4db867\"\n" +
                        "            ],\n" +
                        "            \"osid\": \"3cb8233d-01d9-4e29-af0c-d00c2f28d353\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
                .addHeader("Content-Type", "application/json"));
        Mockito.doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        Mockito.when(mockEventGenerator.getEmailMessageEvent(anyString(),anyString(),anyList(),anyList(),anyList())).thenReturn("mocked-event");
        Mockito.when(freemarkerService.renderTemplate(any(),anyMap())).thenReturn("freemarker");
        Response resp = new Response();
        resp.setStatus(SUCCESSFUL);
        Mockito.when(onboardService.generateAndSetPassword(any(),anyString())).thenReturn(resp);
        Mockito.doNothing().when(onboardService).setKeycloakPassword(anyString(),anyString(),anyMap());
        postgreSQLClient.execute("CREATE TABLE onboard_verification(participant_code character varying NOT NULL PRIMARY KEY,   primary_email character varying,   primary_mobile character varying, createdon bigInt,updatedon bigInt,  expiry bigInt,  phone_verified boolean NOT NULL,email_verified boolean NOT NULL,status character varying,  regenerate_count int,last_regenerate_date date, attempt_count bigInt, comments character varying, phone_short_url character varying, phone_long_url character varying, onboard_validation_properties json, participant_validation_properties json)");
        postgreSQLClient.execute("INSERT INTO onboard_verification(participant_code,primary_email,primary_mobile,createdon,updatedon,expiry,phone_verified,email_verified,status,regenerate_count,last_regenerate_date,attempt_count, comments,phone_short_url,phone_long_url,onboard_validation_properties,participant_validation_properties) " +
                " VALUES('test_user_54.yopmail@swasth-hcx','test_user_54@yopmail.com','9620499129','169719173417','169719173417','1666612517000',true,true,'successful',0,'2023-10-12T13:37:12.533Z','1666612517000','','','','{\"email\": \"activation\",\"phone\": \"verification\"}',' {\"email\": \"activation\",\"phone\": \"verification\"}')");
        postgreSQLClient.execute("CREATE TABLE onboard_verifier( applicant_email character varying NOT NULL,applicant_code character varying NOT NULL,  verifier_code character varying, status character varying, createdon bigInt,   updatedon bigInt,participant_code character varying)");
        postgreSQLClient.execute("INSERT INTO onboard_verifier(applicant_email,applicant_code,verifier_code,status,createdon,updatedon,participant_code)"+"VALUES ('test_user_54@yopmail.com','123445','987655','accepted','1666612517000','1666612517000','test_user_54.yopmail@swasth-hcx')");
//        postgresClientMockService.execute("CREATE TABLE mock_participant(parent_participant_code character varying,child_participant_code character varying NOT NULL PRIMARY KEY, primary_email character varying,password character varying,private_key character varying)");
//        postgresClientMockService.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES('test_user_54.yopmail@swasth-hcx','test_user_54+mock_provider@yopmail.com','test_user_54@yopmail.com','12345','MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDX8KLtg3R9jiqy0GHNdPgo7Kb+eOQlBeWFHAKXY/1TG3m6CaVwtQzof3akE457WFtGR1bP41/0AzDIvmKTVXQCwer9wB2GYuQfrUjvPbFgECcBOC3Dv0Ak/3+T05ntnVrwxYLowEQl53IQt7IWOUCLIBNYjn3B6Wx99PPY4UcHSL6HoWN4/KnAKJiHcUtMSuIUfAG4BMtWDt1yKTRBPNwZpwhka0NTTnPnfVP5z')");
        String requestBodyJson = updateRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_ONBOARD_UPDATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
    }

    @Test
    public void test_verify_identity() throws Exception {
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participant_code", "test_user_54.yopmail@swasth-hcx");
        participantData.put("endpoint_url", "http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v0.7");
        participantData.put("encryption_cert", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        participantData.put("signing_cert_path", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        Map<String, Object> bodyElement = new HashMap<>();
        bodyElement.put("participant", participantData);
        String requestBodyJson = JSONUtils.serialize(participantData);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY_IDENTITY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_applicant_verify() throws Exception {
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participant_code", "test_user_54.yopmail@swasth-hcx");
        participantData.put("endpoint_url", "http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v0.7");
        participantData.put("encryption_cert", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        participantData.put("signing_cert_path", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        Map<String, Object> bodyElement = new HashMap<>();
        bodyElement.put("participant", participantData);
        String requestBodyJson = JSONUtils.serialize(participantData);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_applicant_get_info() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("participant_code", "payr_testpa_313025@swasth-hcx");
        String requestBodyJson = JSONUtils.serialize(requestBody);
        ResponseEntity<Object> expectedResponseEntity = new ResponseEntity<>(requestBodyJson, HttpStatus.OK);;
        when(onboardService.sendVerificationLink(requestBody)).thenReturn(expectedResponseEntity);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_GET_INFO).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_applicant_search() throws Exception {
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participant_code", "test_user_54.yopmail@swasth-hcx");
        Map<String, Object> bodyElement = new HashMap<>();
        bodyElement.put("participant", participantData);
        String requestBodyJson = JSONUtils.serialize(participantData);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.APPLICANT_SEARCH).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }
    
    @Test
    public void test_sendVerification_link_success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("participant_code", "payr_testpa_313025@swasth-hcx");
        requestBody.put("channel", List.of("EMAIL","MOBILE"));
        String requestBodyJson = JSONUtils.serialize(requestBody);
        ResponseEntity<Object> expectedResponseEntity = new ResponseEntity<>(requestBodyJson, HttpStatus.OK);;
        when(onboardService.sendVerificationLink(requestBody)).thenReturn(expectedResponseEntity);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_sendVerification_link_exception() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("channel", List.of("EMAIL","MOBILE"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFICATION_LINK_SEND).content(requestBody.toString()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(400, status);
    }
}
