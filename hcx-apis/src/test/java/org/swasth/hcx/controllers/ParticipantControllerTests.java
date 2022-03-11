package org.swasth.hcx.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ParticipantControllerTests extends BaseSpec{



//    @Test
//    public void participant_create_success_scenario() throws Exception {
//        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
//        MvcResult mvcOrganisation = mockMvc.perform(post("http://localhost:8081/api/v1/Organisation/invite").content(getParticipantCreateBody()).header(HttpHeaders.AUTHORIZATION,getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andExpect();
//
//        MockHttpServletResponse response = mvcOrganisation.getResponse();
//
//        int status = response.getStatus();
//        assertEquals(200, status);
//    }

//    @Test
//    public void participant_search_success_scenario() throws Exception {
//        String requestBody = getRequestBody();
//        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantCreateBody()).contentType(MediaType.APPLICATION_JSON)).andReturn();
//        MockHttpServletResponse response = mvcResult.getResponse();
//        int status = response.getStatus();
//        assertEquals(200, status);
//    }


//    @Test
//    public void participant_update_success_scenario() throws Exception {
//        String requestBody = getRequestBody();
//        MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(getParticipantCreateBody()).contentType(MediaType.APPLICATION_JSON)).andReturn();
//        MockHttpServletResponse response = mvcResult.getResponse();
//        int status = response.getStatus();
//        assertEquals(200, status);
//    }
}
