package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class RegistryControllerTests extends BaseSpec {


    @Test
    void participant_remove_user_interrupt_exception() throws Exception {
        Thread.currentThread().interrupt();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_REMOVE)
                        .content(getParticipantAddBody())
                        .header(HttpHeaders.AUTHORIZATION, getAddUserToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }

    @Test
    void participant_add_user_interrupt_exception() throws Exception {
        Thread.currentThread().interrupt();
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_USER_ADD)
                        .content(getParticipantAddBody())
                        .header(HttpHeaders.AUTHORIZATION, getAddUserToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);
    }
}
