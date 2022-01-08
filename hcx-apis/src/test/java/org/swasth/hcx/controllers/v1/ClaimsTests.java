package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class ClaimsTests extends BaseSpec {
  
  @Test
  public void check_claim_submit_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

  @Test
  public void check_claim_submit_client_exception_scenario() throws Exception {
      String requestBody = getBadRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(400, status);
  }

  @Test
  public void check_claim_submit_exception_scenario() throws Exception {
      String requestBody = "{}";
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(500, status);
  }

  @Test
  public void check_claim_on_submit_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/on_submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

  @Test
  public void check_claim_on_submit_client_exception_scenario() throws Exception {
      String requestBody = getBadRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/on_submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(400, status);
  }

  @Test
  public void check_claim_on_submit_exception_scenario() throws Exception {
      String requestBody = "{}";
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/on_submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(500, status);
  }

  @Test
  public void check_claim_search_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/search").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

  @Test
  public void check_claim_search_client_exception_scenario() throws Exception {
      String requestBody = getBadRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/search").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(400, status);
  }

  @Test
  public void check_claim_search_exception_scenario() throws Exception {
      String requestBody = "{}";
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/search").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(500, status);
  }

  @Test
  public void check_claim_on_search_success_scenario() throws Exception {
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/on_search").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

  @Test
  public void check_claim_on_search_client_exception_scenario() throws Exception {
      String requestBody = getBadRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/on_search").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(400, status);
  }

  @Test
  public void check_claim_on_search_exception_scenario() throws Exception {
      String requestBody = "{}";
      MvcResult mvcResult = mockMvc.perform(post("/v1/claim/on_search").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(500, status);
  }
}