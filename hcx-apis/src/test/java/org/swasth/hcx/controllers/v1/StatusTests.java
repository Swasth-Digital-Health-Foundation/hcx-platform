package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;

import java.util.Arrays;
import java.util.List;
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
        when(auditService.search(any(), any())).thenReturn(List.of(getAuditData(Constants.COVERAGE_ELIGIBILITY_CHECK, Constants.DISPATCHED_STATUS)));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void status_success_for_request_queued_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(auditService.search(any(), any())).thenReturn(List.of(getAuditData(Constants.COVERAGE_ELIGIBILITY_CHECK, Constants.QUEUED_STATUS)));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void status_empty_audit_response_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(auditService.search(any(), any())).thenReturn(Arrays.asList());
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        assertEquals(400, status);
        assertEquals("Invalid correlation id, details do not exist", getResponseErrorMessage(responseBody));
    }

    @Test
    void status_invalid_entity_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(auditService.search(any(), any())).thenReturn(List.of(getAuditData(Constants.PAYMENT_NOTICE_REQUEST, Constants.DISPATCHED_STATUS)));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.HCX_STATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
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
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.HCX_ONSTATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    void on_status_exception_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.HCX_ONSTATUS).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

}
