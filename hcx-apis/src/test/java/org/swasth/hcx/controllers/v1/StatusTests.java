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
    public void status_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("26b1060c", "12345", "5e934f90", "b016", "2022-01-06T20:35:52.636+0530", "93f908ba", "59cefda2-a4cc-4795-95f3-fb9e82e21cef", "/v1/coverageeligibility/check", new Object(), new Object(), "request.dispatched")));
        String requestBody = getStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    @Test
    public void on_status_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("26b1060c-1e83-4600-9612-ea31e0ca5091", "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "5e934f90-111d-4f0b-b016-c22d820674e1", "1e83-460a-4f0b-b016-c22d820674e1", "2022-01-06T20:35:52.636+0530", "93f908ba", "59cefda2-a4cc-4795-95f3-fb9e82e21cef", "/v1/coverageeligibility/check", new Object(), new Object(), "request.dispatched")));
        String requestBody = getOnStatusRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/hcx/on_status").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    private String getStatusRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiOTNmOTA4YmEiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1yZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDkxIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoiNWU5MzRmOTAtMTExZC00ZjBiLWIwMTYtYzIyZDgyMDY3NGUxIiwKIngtaGN4LXRpbWVzdGFtcCI6IjIwMjItMDEtMDZUMDk6NTA6MjMrMDAiLAoieC1oY3gtc3RhdHVzIjoicmVxdWVzdC5pbml0aWF0ZSIsCiJ4LWhjeC13b3JrZmxvd19pZCI6IjFlODMtNDYwYS00ZjBiLWIwMTYtYzIyZDgyMDY3NGUxIiwKIngtaGN4LXN0YXR1c19maWx0ZXJzIjp7InJlcXVlc3RfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUxMDEifSwKImp3c19oZWFkZXIiOnsidHlwIjoiSldUIiwgImFsZyI6IlJTMjU2In0sCiJqd2VfaGVhZGVyIjp7ImFsZyI6IlJTQS1PQUVQIiwiZW5jIjoiQTI1NkdDTSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    private String getOnStatusRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfcmVzcG9uc2UiOnsiZW50aXR5X3R5cGUiOiJjb3ZlcmFnZWVsaWdpYmlsaXR5In0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }


}
