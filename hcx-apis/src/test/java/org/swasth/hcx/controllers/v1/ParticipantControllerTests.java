package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.swasth.common.dto.Response;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
public class ParticipantControllerTests extends BaseSpec {

    @Test
    public void participant_create() throws Exception {
        try {
            doNothing().when(mockKafkaClient).send(anyString(), anyString(), any());
            String requestBody = getParticipantCreateBody();
            MvcResult mvcResult = mockMvc.perform(post("/participant/create").content(requestBody).header("Authorization", getAuthorizationHeader()).contentType(MediaType.APPLICATION_JSON)).andReturn();
            MockHttpServletResponse response = mvcResult.getResponse();
            int status = response.getStatus();
            assertEquals(200, status);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
