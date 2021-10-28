package org.swasth.hcx.controllers;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.swasth.hcx.Helpers.KafkaEventGenerator;
import org.swasth.hcx.middleware.KafkaClient;

@WebMvcTest(BaseController.class)
public class BaseControllerTests {
    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected KafkaEventGenerator mockKafkaEventGenerator;

    @MockBean
    protected KafkaClient mockKafkaClient;

    @Mock
    protected Environment mockEnv;
}
