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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class OnboardControllerTests extends BaseSpec{
    @MockBean
    private OnboardService onboardService;

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
