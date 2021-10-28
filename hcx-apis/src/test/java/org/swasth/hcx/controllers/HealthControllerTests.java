package org.swasth.hcx.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(HealthController.class)
public class HealthControllerTests extends BaseControllerTests {

    @Test
    public void testServiceHealth() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/service/health")).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
    }

    @Test
    public void testHealth() throws Exception {
        when(mockKafkaClient.health()).thenReturn(true);
        MvcResult mvcResult = mockMvc.perform(get("/health")).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        JSONObject jsonObject= new JSONObject(response.getContentAsString());
        JSONObject result = jsonObject.getJSONObject("result");
        assertEquals(200, status);
        assertEquals(true, result.get("healthy"));
    }

}
