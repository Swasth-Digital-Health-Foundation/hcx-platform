package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.HeaderAudit;
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

public class StatusTests extends BaseSpec {

    @Test
    public void status_success_for_request_dispatched_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"), "/v1/coverageeligibility/check", "200c6dac-b259-4d35-b176-370fb092d7b0", "request.dispatched", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    public void status_success_for_request_queued_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"), "/v1/coverageeligibility/check", "200c6dac-b259-4d35-b176-370fb092d7b0", "request.queued", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    public void status_empty_audit_response_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList());
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        assertEquals(400, status);
        assertEquals("Invalid correlation id, details do not exist", getResponseErrorMessage(responseBody));
    }

    @Test
    public void status_invalid_entity_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("AUDIT", new Object(), new Object(), "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "93f908ba", "26b1060c-1e83-4600-9612-ea31e0ca5091", "1e83-460a-4f0b-b016-c22d820674e1", "5e934f90-111d-4f0b-b016-c22d820674e1", "2022-01-06T09:50:23+00", new Long("1642781095099"), new Long("1642781095099"), new Long("1642781095099"), "/v1/paymentnotice/request", "200c6dac-b259-4d35-b176-370fb092d7b0", "request.dispatched", Arrays.asList("provider"), Arrays.asList("payor"), "test_payload")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        Map<String,Object> responseBody = JSONUtils.deserialize(response.getContentAsString(), Map.class);
        assertEquals(400, status);
        assertEquals("Invalid entity, status search allowed only for entities: [coverageeligibility, preauth, claim, predetermination]", getResponseErrorMessage(responseBody));
    }

    @Test
    public void on_status_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getOnStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/on_status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    private String getStatusRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiOTNmOTA4YmEiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJhcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTEwMSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    private String getOnStatusRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtYXBpX2NhbGxfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0wNlQwOTo1MDoyMyswMCIsCiJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LmluaXRpYXRlIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMWU4My00NjBhLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtc3RhdHVzX3Jlc3BvbnNlIjp7ImVudGl0eV90eXBlIjoiY292ZXJhZ2VlbGlnaWJpbGl0eSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }


}
