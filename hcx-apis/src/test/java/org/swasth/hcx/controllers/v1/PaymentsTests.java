package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


class PaymentsTests extends BaseSpec {

    @Test
    void check_payment_request_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PAYMENT_NOTICE_REQUEST).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void check_payment_request_exception_scenario() throws Exception {
        String requestBody = getExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PAYMENT_NOTICE_REQUEST).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    void check_payment_on_request_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PAYMENT_NOTICE_ONREQUEST).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void check_payment_on_request_exception_scenario() throws Exception {
        String requestBody = getExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PAYMENT_NOTICE_ONREQUEST).content(requestBody).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

}