package org.swasth.hcx.controllers.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.swasth.common.JsonUtils;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.hcx.controllers.BaseSpec;

public class AuditControllerTests extends BaseSpec{

	@InjectMocks
	AuditController auditController;
	
    @BeforeEach
    public void setup() {
        /* this must be called for the @Mock annotations above to be processed
         and for the mock service to be injected into the controller under test. */
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(auditController).build();
        
    }
    
    @Test
    public void audit_search_success_scenario() throws Exception {
    	String uri = "/vi/audit/search";
    	SearchRequestDTO searchrequest = new SearchRequestDTO();
    	searchrequest.setFilters((HashMap<String, String>) Map.of(
    		    "status", "submitted",
    		    "request_id", "26b1060c-1e83-4600-9612-ea31e0ca5070"
    		));
    	searchrequest.setLimit(10);
    	searchrequest.setOffset(0);
    	MvcResult mvcResult =  mockMvc.perform(post(uri).content(JsonUtils.serialize(searchrequest)).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);   	
    }
}
