package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.HeaderAudit;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class StatusTests extends BaseSpec {

    @Test
    void status_success_for_request_dispatched_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"), Constants.COVERAGE_ELIGIBILITY_CHECK, "200c6dac-b259-4d35-b176-370fb092d7b0", "request.dispatched", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void status_success_for_request_queued_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"), Constants.COVERAGE_ELIGIBILITY_CHECK, "200c6dac-b259-4d35-b176-370fb092d7b0", "request.queued", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void status_empty_audit_response_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList());
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        assertEquals(400, status);
        assertEquals("Invalid correlation id, details do not exist", getResponseErrorMessage(responseBody));
    }

    @Test
    void status_invalid_entity_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"), Constants.PAYMENT_NOTICE_REQUEST, "200c6dac-b259-4d35-b176-370fb092d7b0", "request.dispatched", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        assertEquals(400, status);
        assertEquals("Invalid entity, status search allowed only for entities: [coverageeligibility, preauth, claim, predetermination]", getResponseErrorMessage(responseBody));
    }

    @Test
    void on_status_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getOnStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.HCX_ONSTATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void on_status_exception_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.HCX_ONSTATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

}
