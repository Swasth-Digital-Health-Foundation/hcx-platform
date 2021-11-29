package org.swasth.hcx.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class HealthControllerTests extends BaseSpec {

    @InjectMocks
    HealthController healthController;

    @BeforeEach
    public void setup() {
        /* this must be called for the @Mock annotations above to be processed
          and for the mock service to be injected into the controller under test. */
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

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
        JSONObject resp= new JSONObject(response.getContentAsString());
        JSONObject result = resp.getJSONObject("result");
        Boolean healthy = result.getBoolean("healthy");
        assertEquals(200, status);
        assertEquals(true, healthy);
    }

    @Test
    public void testHealth_failure_scenario() throws Exception {
        when(mockKafkaClient.health()).thenReturn(false);
        MvcResult mvcResult = mockMvc.perform(get("/health")).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        JSONObject resp= new JSONObject(response.getContentAsString());
        JSONObject result = resp.getJSONObject("result");
        Boolean healthy = result.getBoolean("healthy");
        assertEquals(200, status);
        assertEquals(false, healthy);
    }

}