package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class CoverageEligibilityTests extends BaseSpec {
    
    @InjectMocks
    CoverageEligibilityController coverageEligibilityController;

    @BeforeEach
    public void setup() {
        /* this must be called for the @Mock annotations above to be processed
         and for the mock service to be injected into the controller under test. */
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(coverageEligibilityController).build();
    }

    @Test
    public void check_coverage_eligibility_success_scenario() throws Exception {
      when(mockEnv.getProperty("protocol.headers.mandatory", List.class)).thenReturn(new ArrayList<>(Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-request_id", "x-hcx-correlation_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-workflow_id")));
      when(mockEnv.getProperty("headers.jose", List.class)).thenReturn(new ArrayList<>(Arrays.asList("alg", "enc")));
      when(mockEnv.getProperty("payload.mandatory.properties", List.class)).thenReturn(new ArrayList<>(Arrays.asList("protected", "encrypted_key", "aad", "iv", "ciphertext", "tag")));
      when(mockEnv.getProperty("service.mode")).thenReturn("gateway");
      doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
      String requestBody = getRequestBody();
      MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
    }

    @Test
    public void check_coverage_eligibility_headers_missing_scenario() throws Exception {
        when(mockEnv.getProperty(Constants.PAYLOAD_MANDATORY_PROPERTIES, List.class)).thenReturn(new ArrayList<>(Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-request_id", "x-hcx-correlation_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-workflow_id")));
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getHeadersMissingRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }


    @Test
    public void check_coverage_eligibility_client_exception_scenario() throws Exception {
        String requestBody = getBadRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    public void check_coverage_eligibility_exception_scenario() throws Exception {
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    public void on_check_coverage_eligibility_success_scenario() throws Exception {
        when(mockEnv.getProperty("protocol.headers.mandatory", List.class)).thenReturn(new ArrayList<>(Arrays.asList("x-hcx-sender_code", "x-hcx-recipient_code", "x-hcx-request_id", "x-hcx-correlation_id", "x-hcx-timestamp", "x-hcx-status", "x-hcx-workflow_id")));
        when(mockEnv.getProperty("headers.jose", List.class)).thenReturn(new ArrayList<>(Arrays.asList("alg", "enc")));
        when(mockEnv.getProperty("payload.mandatory.properties", List.class)).thenReturn(new ArrayList<>(Arrays.asList("protected", "encrypted_key", "aad", "iv", "ciphertext", "tag")));
        when(mockEnv.getProperty("service.mode")).thenReturn("gateway");
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }
    @Test
    public void on_check_coverage_eligibility_client_exception_scenario() throws Exception {
        String requestBody = getBadRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }
    @Test
    public void on_check_coverage_eligibility_exception_scenario() throws Exception {
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/on_check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }
}
