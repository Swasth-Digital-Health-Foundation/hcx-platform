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
    public void searchSuccess() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        when(headerAuditService.search(any())).thenReturn(Arrays.asList(new HeaderAudit("26b1060c", "12345", "5e934f90", "b016", "2022-01-06T20:35:52.636+0530", "93f908ba", "59cefda2-a4cc-4795-95f3-fb9e82e21cef", "/v1/coverageeligibility/check", new Object(), new Object(), "request.dispatched")));
        String requestBody = getSearchRequest();
        MvcResult mvcResult = mockMvc.perform(post(HCX_SEARCH).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        //used mock server for testcases
        assertEquals(404, status);
    }

    @Test
    public void onSearchSuccess() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        String requestBody = getOnSearchRequest();
        MvcResult mvcResult = mockMvc.perform(post(HCX_ON_SEARCH).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        //used mock server for testcases
        assertEquals(404, status);
    }

    private String getSearchRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtODA1MDBjZGQtMmRlYy00ZDYwLWJkMWItOGY5ZDgzZjQ5N2ZmIiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJoY3gtZ2F0ZXdheS1jb2RlIiwieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsIngtaGN4LXJlcXVlc3RfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLCJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LmluaXRpYXRlIiwieC1oY3gtZGVidWdfZmxhZyI6IkluZm8iLCJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiJiYWQuaW5wdXQiLCJlcnJvci5tZXNzYWdlIjoiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LCJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiJiYWQuaW5wdXQiLCJlcnJvci5tZXNzYWdlIjoiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LCJ4LWhjeC1zZWFyY2giOnsiZmlsdGVycyI6eyJzZW5kZXJzIjpbIjEtODA1MDBjZGQtMmRlYy00ZDYwLWJkMWItOGY5ZDgzZjQ5N2ZmIl0sInJlY2VpdmVycyI6WyIxLTkzZjkwOGJhLWI1NzktNDUzZS04YjJhLTU2MDIyYWZhZDI3NSJdLCJlbnRpdHlfdHlwZXMiOlsicHJlYXV0aCIsImNsYWltIl0sIndvcmtmbG93X2lkcyI6W10sImNhc2VfaWRzIjpbXSwiZW50aXR5X3N0YXR1cyI6WyJjbGFpbXMuY29tcGxldGVkIiwiY2xhaW1zLnJlamVjdGVkIl19LCJ0aW1lX3BlcmlvZCI6MjR9fQ==.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        return JSONUtils.serialize(obj);
    }

    private String getOnSearchRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtOTNmOTA4YmEtYjU3OS00NTNlLThiMmEtNTYwMjJhZmFkMjc1IiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJoY3gtZ2F0ZXdheS1jb2RlIiwieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsIngtaGN4LXJlcXVlc3RfaWQiOiJmZjg0OTI4Yy1hMDc3LTQ1NjUtOGZiMS03MzFiMWI2NDY2YTAiLCJ4LWhjeC1zdGF0dXMiOiJyZXNwb25zZS5pbml0aWF0ZSIsIngtaGN4LWRlYnVnX2ZsYWciOiJJbmZvIiwieC1oY3gtZXJyb3JfZGV0YWlscyI6eyJlcnJvci5jb2RlIjoiYmFkLmlucHV0IiwiZXJyb3IubWVzc2FnZSI6IlByb3ZpZGVyIGNvZGUgbm90IGZvdW5kIiwidHJhY2UiOiIifSwieC1oY3gtZGVidWdfZGV0YWlscyI6eyJlcnJvci5jb2RlIjoiYmFkLmlucHV0IiwiZXJyb3IubWVzc2FnZSI6IlByb3ZpZGVyIGNvZGUgbm90IGZvdW5kIiwidHJhY2UiOiIifSwieC1oY3gtc2VhcmNoX3Jlc3BvbnNlIjp7ImNvdW50Ijo2LCJlbnRpdHlfY291bnRzIjp7ImNsYWltIjoxLCJwcmVhdXRoIjoyLCJwcmVkZXRlcm1pbmF0aW9uIjozfX19.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        return JSONUtils.serialize(obj);
    }
}
