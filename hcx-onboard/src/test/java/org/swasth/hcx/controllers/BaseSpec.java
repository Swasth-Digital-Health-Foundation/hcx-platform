package org.swasth.hcx.controllers;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.config.GenericConfiguration;
import org.swasth.hcx.controllers.v1.ParticipantController;
import org.swasth.hcx.helpers.EventGenerator;


@WebMvcTest({ ParticipantController.class, EventGenerator.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Import(GenericConfiguration.class)
public class BaseSpec {

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Mock
    protected Environment mockEnv;

    @MockBean
    protected AuditIndexer auditIndexer;

    @MockBean
    protected EventGenerator mockEventGenerator;

    @MockBean
    protected RegistryService mockRegistryService;

    @MockBean
    protected RestHighLevelClient restHighLevelClient;

    @MockBean
    protected JWTUtils jwtUtils;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

}
