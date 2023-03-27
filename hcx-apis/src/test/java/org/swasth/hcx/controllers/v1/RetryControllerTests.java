package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.dto.Request;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.utils.MockResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

 class RetryControllerTests extends BaseSpec {


    @Test
     void retry_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        ResultSet mockResultSet = getMockResultSet();
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + "/request/retry/e49e067d-60ff-40ee-b3df-08abb6c2fda1").param("mid",Constants.MID).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Map<String,Object> output = JSONUtils.deserialize(response.getContentAsString(),Map.class);
        int status = response.getStatus();
        assertEquals(202, status);
        assertEquals(Constants.SUCCESSFUL,output.get("status"));
    }

    @Test
     void retry_failure_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(),anyString(),any());
        ResultSet mockResultSet = getEmptyResultSet();
        doReturn(mockResultSet).when(postgreSQLClient).executeQuery(anyString());
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + "/request/retry/e49e067d-60ff-40ee-b3df-08abb6c2fda1").param("mid",Constants.MID).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Map<String,Object> output = JSONUtils.deserialize(response.getContentAsString(),Map.class);
        Map<String,Object> errorMap = (Map<String, Object>) output.get("error");
        int status = response.getStatus();
        assertEquals(400, status);
        assertEquals("Invalid mid, request does not exist",errorMap.get("message"));
    }


    public ResultSet getMockResultSet() throws SQLException {
        return MockResultSet.createStringMock(
                new String[]{"mid","data","action","status","retrycount","lastupdatedon"}, //columns
                new Object[][]{ // data
                        {"e49e067d-60ff-40ee-b3df-08abb6c2fda1","{\"payload\":\"eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6InRlc3Rwcm92aWRlcjEuYXBvbGxvQHN3YXN0aC1oY3gtZGV2IiwieC1oY3gtcmVjaXBpZW50X2NvZGUiOiJ0ZXN0cGF5b3IxLmljaWNpQHN3YXN0aC1oY3gtZGV2IiwieC1oY3gtY29ycmVsYXRpb25faWQiOiI4YjIwYThhYS0zNDZiLTQwZjMtOWUxNi1mMWVjZDNkMzQ5ZDAiLCJ4LWhjeC10aW1lc3RhbXAiOiIyMDIzLTAyLTEyVDIxOjQxOjUxLjM3NCswNTMwIiwieC1oY3gtYXBpX2NhbGxfaWQiOiJkMDViZjY0OS00YzMzLTQ5MTAtODIxNS0wNzBmOWYyN2YzZWQifQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw\"}","/coverageeligibility/check","request.queued",0,"1676218371439"}
                });
    }
    public ResultSet getEmptyResultSet() throws SQLException {
        return MockResultSet.createEmptyMock(
                new String[]{},
                new Object[][]{});
    }
}
