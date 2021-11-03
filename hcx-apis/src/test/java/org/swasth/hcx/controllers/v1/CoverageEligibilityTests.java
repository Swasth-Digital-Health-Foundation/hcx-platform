package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class CoverageEligibilityTests extends BaseSpec {

    @Test
    public void check_coverage_eligibility_success_scenario() throws Exception {
        doNothing().when(mockKafkaClient).send(anyString(), anyString(), anyString());
        HttpHeaders header = getHeaders();
        String requestBody = getRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/v1/coverageeligibility/check").headers(header).content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }
}
