package org.swasth.hcx.controllers.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.hcx.controllers.BaseSpec;

class AuditControllerTests extends BaseSpec {

    @Test
    void audit_search_success_scenario() throws Exception {
        String uri = Constants.VERSION_PREFIX + Constants.AUDIT_SEARCH;
        AuditSearchRequest searchrequest = new AuditSearchRequest();
        HashMap<String, String> filters = new HashMap<String, String>();
        filters.put("status", "submitted");
        searchrequest.setFilters(filters);
        searchrequest.setLimit(10);
        searchrequest.setOffset(0);
        MvcResult mvcResult = mockMvc.perform(post(uri).content(JSONUtils.serialize(searchrequest)).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }
}