package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.config.GenericConfiguration;
import org.swasth.hcx.service.AuditService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest({AuditController.class, AuditService.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Import(GenericConfiguration.class)
class AuditControllerTests {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private AuditService auditService;

    @MockBean
    private RestHighLevelClient restHighLevelClient;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void audit_search_success_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.AUDIT_SEARCH)
                .content(JSONUtils.serialize(getRequest(Collections.singletonMap("status", "submitted")))).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void audit_notification_search_success_scenario() throws Exception {
        Map<String, String> filters = new HashMap<>();
        filters.put(Constants.HCX_SENDER_CODE, "provider@01");
        filters.put(Constants.START_DATETIME, "2022-09-10T16:21:20.249+0530");
        filters.put(Constants.STOP_DATETIME, "2022-09-15T16:21:20.249+0530");
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.AUDIT_NOTIFICATION_SEARCH)
                .content(JSONUtils.serialize(getRequest(filters))).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    void audit_onboard_search_success_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.AUDIT_ONBOARD_SEARCH)
                .content(JSONUtils.serialize(getRequest(new HashMap<>()))).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }

    private AuditSearchRequest getRequest(Map<String,String> filters){
        AuditSearchRequest request = new AuditSearchRequest();
        request.setFilters(filters);
        request.setLimit(10);
        request.setOffset(5);
        return request;
    }

}