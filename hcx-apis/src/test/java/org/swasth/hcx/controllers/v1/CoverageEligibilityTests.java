package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.HeaderAudit;
import org.swasth.hcx.controllers.BaseSpec;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CoverageEligibilityTests extends BaseSpec {

    @Test
    public void check_coverage_eligibility_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
    }



    @Test
    public void check_coverage_eligibility_exception_scenario() throws Exception {
        String requestBody = getExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    public void on_check_coverage_eligibility_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    public void on_check_coverage_eligibility_exception_scenario() throws Exception {
        String requestBody = getExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    public void on_check_coverage_eligibility_redirect() throws Exception {
        String requestBody = "{\"x-hcx-status\":\"response.redirect\"}";
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    public void on_check_coverage_eligibility_redirect_status() throws Exception {
        String requestBody = "{\"x-hcx-sender_code\":\"1234567\"}";
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

}
