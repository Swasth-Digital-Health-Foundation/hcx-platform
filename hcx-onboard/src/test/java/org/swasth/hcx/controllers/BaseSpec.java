package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import okhttp3.mockwebserver.MockWebServer;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.swasth.common.exception.ClientException;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.config.GenericConfiguration;
import org.swasth.hcx.controllers.v1.OnboardController;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.services.OnboardService;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@WebMvcTest({ OnboardController.class, OnboardService.class, EventGenerator.class})
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
    private final MockWebServer registryServer =  new MockWebServer();

    @MockBean
    private PostgreSQLClient postgreSQLClient;
    @MockBean
    private KafkaClient kafkaClient;
    private EmbeddedPostgres embeddedPostgres;

    @BeforeEach
     void setup() throws Exception {
        Mockito.mock(OnboardService.class);
        registryServer.start(InetAddress.getByName("localhost"),8082);
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        postgreSQLClient = new PostgreSQLClient("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @AfterEach
    void teardown() throws IOException, InterruptedException {
        registryServer.shutdown();
        Thread.sleep(2000);
    }
    public String getAuthorizationHeader() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE2ODE2MjcyNDQsImlhdCI6MTY3OTg5OTI0NCwianRpIjoiZmMyNjBlNjQtZDhkYy00OGY1LWIzMDMtOTZmZmU4MmVlNjNmIiwiaXNzIjoiaHR0cDovL2Rldi1oY3guc3dhc3RoLmFwcC9hdXRoL3JlYWxtcy9zd2FzdGgtaGVhbHRoLWNsYWltLWV4Y2hhbmdlIiwic3ViIjoiMjljZWNlMjAtMThlZi00YmM4LThlYTQtYzMxZDZmOTM4NDljIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiYWYzMDdjM2UtMmU5ZS00YzY3LWI0MDYtNzQyZmJhMTBjYjAzIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJISUUvSElPLkhDWCIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeC1hZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3gtYWRtaW4iLCJlbWFpbCI6ImhjeC1hZG1pbkBnbWFpbC5jb20ifQ.IBrmoA0m6QZEYQyv0ggfxV2VZMbPSMPs8JYB7XKK16HRhqaxCpqvc8GWazO8lpOBhLbPQZahaLM75ua9MxYqu5nrk1np3WbeKHpewjuScRbvXTSus2Z4vdy-XcA-Q3sLH6ABqyTwwrhGMD--1x9AqYk3PsCaSDItaulYc3IwC1NivBn60Wwc3o-QYWry5SP1atzii4LTTwgsqi1XHlg1125hV419aHqVJRmU79kXqVqdGtKyyro5QUMAE9YHotKHoPWO3sXgRjTow7QfLyB7PeYYZs9ganfTKa4sH9DWfh52c1Tf2uBjw2boBqbUK4iBv5uxV_DosdTfityVD32E8w";
    }
}
