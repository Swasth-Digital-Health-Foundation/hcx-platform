package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ParticipantControllerTests extends BaseSpec {

    @Test
    void participant_search_success_scenario() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/v1/participant/search").content(getSearchFilter()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }
}
