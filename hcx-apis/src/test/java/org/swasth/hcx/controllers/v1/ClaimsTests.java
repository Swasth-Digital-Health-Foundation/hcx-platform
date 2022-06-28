package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.HeaderAudit;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseSpec;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


class ClaimsTests extends BaseSpec {
  
  @Test
  void check_claim_submit_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.CLAIM_SUBMIT).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

  @Test
  void check_claim_submit_exception_scenario() throws Exception {
      String requestBody = getExceptionRequestBody();
      MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.CLAIM_SUBMIT).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(500, status);
  }

  @Test
  void check_claim_on_submit_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.CLAIM_ONSUBMIT).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

  @Test
  void check_claim_on_submit_exception_scenario() throws Exception {
      String requestBody = getExceptionRequestBody();
      MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.CLAIM_ONSUBMIT).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(500, status);
  }

}