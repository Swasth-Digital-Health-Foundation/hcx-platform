package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.services.OnboardService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class OnboardControllerTests extends BaseSpec{
    @MockBean
    private OnboardService onboardService;

    @Test
    public void testVerify() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        ArrayList<Map<String, Object>> requestBody = new ArrayList<>();
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("primary_email", "test@example.com");
        Map<String, Object> bodyElement = new HashMap<>();
        bodyElement.put("participant", participantData);
        requestBody.add(bodyElement);
        String requestBodyJson = JSONUtils.serialize(requestBody);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_VERIFY).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        ResponseEntity<Object> expectedResult = ResponseEntity.ok("Verification successful");
        when(onboardService.verify(eq(headers), eq(requestBody))).thenReturn(expectedResult);
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
    }

    @Test
    public void test_onboard_update() throws Exception {
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participant_code", "test_user_54.yopmail@swasth-hcx");
        participantData.put("endpoint_url", "http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v0.7");
        participantData.put("encryption_cert", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        participantData.put("signing_cert_path", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        Map<String, Object> bodyElement = new HashMap<>();
        bodyElement.put("participant", participantData);
        String requestBodyJson = JSONUtils.serialize(participantData);
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_ONBOARD_UPDATE).content(requestBodyJson).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Assertions.assertEquals(200, status);
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
