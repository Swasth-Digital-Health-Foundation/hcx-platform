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
import static org.swasth.common.utils.Constants.HCX_ON_SEARCH;
import static org.swasth.common.utils.Constants.HCX_SEARCH;

public class SearchControllerTest extends BaseSpec {

    @Test
    public void searchBadRequest() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getSearchBadRequest();
        MvcResult mvcResult = mockMvc.perform(post(HCX_SEARCH).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    private String getSearchBadRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtODA1MDBjZGQtMmRlYy00ZDYwLWJkMWItOGY5ZDgzZjQ5N2ZmIiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJoY3gtZ2F0ZXdheS1jb2RlIiwieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsIngtaGN4LXJlcXVlc3RfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLCJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LmluaXRpYXRlIiwieC1oY3gtZGVidWdfZmxhZyI6IkluZm8iLCJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiJiYWQuaW5wdXQiLCJlcnJvci5tZXNzYWdlIjoiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LCJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiJiYWQuaW5wdXQiLCJlcnJvci5tZXNzYWdlIjoiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LCJ4LWhjeC1zZWFyY2giOnsiZmlsdGVycyI6eyJzZW5kZXJzIjpbIjEtODA1MDBjZGQtMmRlYy00ZDYwLWJkMWItOGY5ZDgzZjQ5N2ZmIl0sInJlY2VpdmVycyI6WyIxLTkzZjkwOGJhLWI1NzktNDUzZS04YjJhLTU2MDIyYWZhZDI3NSJdLCJlbnRpdHlfdHlwZXMiOlsicHJlYXV0aCIsImNsYWltIl0sIndvcmtmbG93X2lkcyI6W10sImNhc2VfaWRzIjpbXSwiZW50aXR5X3N0YXR1cyI6WyJjbGFpbXMuY29tcGxldGVkIiwiY2xhaW1zLnJlamVjdGVkIl19LCJ0aW1lX3BlcmlvZCI6MjR9fQ==.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        return JSONUtils.serialize(obj);
    }

    @Test
    public void searchSuccess() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getSearchRequest();
        MvcResult mvcResult = mockMvc.perform(post(HCX_SEARCH).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    private String getSearchRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtODA1MDBjZGQtMmRlYy00ZDYwLWJkMWItOGY5ZDgzZjQ5N2ZmIiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJoY3gtZ2F0ZXdheS1jb2RlIiwieC1oY3gtY29ycmVsYXRpb25faWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsIngtaGN4LWFwaV9jYWxsX2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDkxIiwieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0xNlQwOTo1MDoyMyswMCIsIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLCJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsIngtaGN4LWVycm9yX2RldGFpbHMiOnsiZXJyb3IuY29kZSI6ImJhZC5pbnB1dCIsImVycm9yLm1lc3NhZ2UiOiJQcm92aWRlciBjb2RlIG5vdCBmb3VuZCIsInRyYWNlIjoiIn0sIngtaGN4LWRlYnVnX2RldGFpbHMiOnsiZXJyb3IuY29kZSI6ImJhZC5pbnB1dCIsImVycm9yLm1lc3NhZ2UiOiJQcm92aWRlciBjb2RlIG5vdCBmb3VuZCIsInRyYWNlIjoiIn0sIngtaGN4LXNlYXJjaCI6eyJmaWx0ZXJzIjp7InNlbmRlcnMiOlsiMS04MDUwMGNkZC0yZGVjLTRkNjAtYmQxYi04ZjlkODNmNDk3ZmYiXSwicmVjZWl2ZXJzIjpbIjEtOTNmOTA4YmEtYjU3OS00NTNlLThiMmEtNTYwMjJhZmFkMjc1Il0sImVudGl0eV90eXBlcyI6WyJwcmVhdXRoIiwiY2xhaW0iXSwid29ya2Zsb3dfaWRzIjpbXSwiY2FzZV9pZHMiOltdLCJlbnRpdHlfc3RhdHVzIjpbImNsYWltcy5jb21wbGV0ZWQiLCJjbGFpbXMucmVqZWN0ZWQiXX0sInRpbWVfcGVyaW9kIjoyNH0sImp3c19oZWFkZXIiOnsidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifSwiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifX0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    @Test
    public void onSearchBadRequest() throws Exception {
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("ff84928c-a077-4565-8fb1-731b1b6466a0", "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "1e83-460a-4f0b-b016-c22d820674e1", "", "2022-01-06T20:35:52.636+0530", "93f908ba", "59cefda2-a4cc-4795-95f3-fb9e82e21cef", "/v1/hcx/search", new Object(), new Object(), "request.dispatched")));
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getOnSearchBadRequest();
        MvcResult mvcResult = mockMvc.perform(post(HCX_ON_SEARCH).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    private String getOnSearchBadRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtOTNmOTA4YmEtYjU3OS00NTNlLThiMmEtNTYwMjJhZmFkMjc1IiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJoY3gtZ2F0ZXdheS1jb2RlIiwieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsIngtaGN4LXJlcXVlc3RfaWQiOiJmZjg0OTI4Yy1hMDc3LTQ1NjUtOGZiMS03MzFiMWI2NDY2YTAiLCJ4LWhjeC1zdGF0dXMiOiJyZXNwb25zZS5pbml0aWF0ZSIsIngtaGN4LWRlYnVnX2ZsYWciOiJJbmZvIiwieC1oY3gtZXJyb3JfZGV0YWlscyI6eyJlcnJvci5jb2RlIjoiYmFkLmlucHV0IiwiZXJyb3IubWVzc2FnZSI6IlByb3ZpZGVyIGNvZGUgbm90IGZvdW5kIiwidHJhY2UiOiIifSwieC1oY3gtZGVidWdfZGV0YWlscyI6eyJlcnJvci5jb2RlIjoiYmFkLmlucHV0IiwiZXJyb3IubWVzc2FnZSI6IlByb3ZpZGVyIGNvZGUgbm90IGZvdW5kIiwidHJhY2UiOiIifSwieC1oY3gtc2VhcmNoX3Jlc3BvbnNlIjp7ImNvdW50Ijo2LCJlbnRpdHlfY291bnRzIjp7ImNsYWltIjoxLCJwcmVhdXRoIjoyLCJwcmVkZXRlcm1pbmF0aW9uIjozfX19.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        return JSONUtils.serialize(obj);
    }

    @Test
    public void onSearchSuccess() throws Exception {
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("ff84928c-a077-4565-8fb1-731b1b6466a0", "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d", "1e83-460a-4f0b-b016-c22d820674e1", "", "2022-01-06T20:35:52.636+0530", "93f908ba", "59cefda2-a4cc-4795-95f3-fb9e82e21cef", "/v1/hcx/search", new Object(), new Object(), "request.dispatched")));
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getOnSearchRequest();
        MvcResult mvcResult = mockMvc.perform(post(HCX_ON_SEARCH).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }

    private String getOnSearchRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtOTNmOTA4YmEtYjU3OS00NTNlLThiMmEtNTYwMjJhZmFkMjc1IiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJoY3gtZ2F0ZXdheS1jb2RlIiwieC1oY3gtY29ycmVsYXRpb25faWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsIngtaGN4LWFwaV9jYWxsX2lkIjoiZmY4NDkyOGMtYTA3Ny00NTY1LThmYjEtNzMxYjFiNjQ2NmEwIiwieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0xNlQwOTo1MDoyMyswMCIsIngtaGN4LXN0YXR1cyI6InJlc3BvbnNlLnN1Y2Nlc3MiLCJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsIngtaGN4LWVycm9yX2RldGFpbHMiOnsiZXJyb3IuY29kZSI6ImJhZC5pbnB1dCIsImVycm9yLm1lc3NhZ2UiOiJQcm92aWRlciBjb2RlIG5vdCBmb3VuZCIsInRyYWNlIjoiIn0sIngtaGN4LWRlYnVnX2RldGFpbHMiOnsiZXJyb3IuY29kZSI6ImJhZC5pbnB1dCIsImVycm9yLm1lc3NhZ2UiOiJQcm92aWRlciBjb2RlIG5vdCBmb3VuZCIsInRyYWNlIjoiIn0sIngtaGN4LXNlYXJjaF9yZXNwb25zZSI6eyJjb3VudCI6NiwiZW50aXR5X2NvdW50cyI6eyJjbGFpbSI6MSwicHJlYXV0aCI6MiwicHJlZGV0ZXJtaW5hdGlvbiI6M319LCJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0sImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn19.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        return JSONUtils.serialize(obj);
    }


}
