package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.config.GenericConfiguration;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.services.FreemarkerService;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(GenericConfiguration.class)
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = false,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        })
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
    protected FreemarkerService freemarkerService;

    @MockBean
    protected RegistryService mockRegistryService;
    protected final MockWebServer registryServer =  new MockWebServer();

    protected final MockWebServer hcxApiServer =  new MockWebServer();

    @MockBean
    protected PostgreSQLClient postgreSQLClient;
//    @Resource(name = "postgresClientMockService")
//    @MockBean
//    protected PostgreSQLClient postgresClientMockService;
    @MockBean
    protected KafkaClient kafkaClient;
    private EmbeddedPostgres embeddedPostgres;

    @BeforeEach
     void setup() throws Exception {
        registryServer.start(InetAddress.getByName("localhost"),8082);
        hcxApiServer.start(InetAddress.getByName("localhost"),8080);
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        postgreSQLClient = new PostgreSQLClient("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
//        postgresClientMockService = new PostgreSQLClient("jdbc:postgresql://localhost:5432/mock_service", "postgres", "postgres");
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @AfterEach
    void teardown() throws IOException, InterruptedException {
        registryServer.shutdown();
        hcxApiServer.shutdown();
        Thread.sleep(2000);
    }
    protected String getAuthorizationHeader() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE2ODE2MjcyNDQsImlhdCI6MTY3OTg5OTI0NCwianRpIjoiZmMyNjBlNjQtZDhkYy00OGY1LWIzMDMtOTZmZmU4MmVlNjNmIiwiaXNzIjoiaHR0cDovL2Rldi1oY3guc3dhc3RoLmFwcC9hdXRoL3JlYWxtcy9zd2FzdGgtaGVhbHRoLWNsYWltLWV4Y2hhbmdlIiwic3ViIjoiMjljZWNlMjAtMThlZi00YmM4LThlYTQtYzMxZDZmOTM4NDljIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiYWYzMDdjM2UtMmU5ZS00YzY3LWI0MDYtNzQyZmJhMTBjYjAzIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJISUUvSElPLkhDWCIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeC1hZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3gtYWRtaW4iLCJlbWFpbCI6ImhjeC1hZG1pbkBnbWFpbC5jb20ifQ.IBrmoA0m6QZEYQyv0ggfxV2VZMbPSMPs8JYB7XKK16HRhqaxCpqvc8GWazO8lpOBhLbPQZahaLM75ua9MxYqu5nrk1np3WbeKHpewjuScRbvXTSus2Z4vdy-XcA-Q3sLH6ABqyTwwrhGMD--1x9AqYk3PsCaSDItaulYc3IwC1NivBn60Wwc3o-QYWry5SP1atzii4LTTwgsqi1XHlg1125hV419aHqVJRmU79kXqVqdGtKyyro5QUMAE9YHotKHoPWO3sXgRjTow7QfLyB7PeYYZs9ganfTKa4sH9DWfh52c1Tf2uBjw2boBqbUK4iBv5uxV_DosdTfityVD32E8w";
    }

    protected String verifyRequestBody() throws JsonProcessingException {
        List<Map<String , Object>> data = new ArrayList<>();
        Map<String,Object> body = new HashMap<>();
        body.put("type", "onboard-through-verifier");
        body.put("verifier_code","wemeanhospital+mock_payor.yopmail@swasth-hcx-dev");
        Map<String , Object> participant = new HashMap<>();
        participant.put(Constants.PRIMARY_EMAIL,"obama02@yopmail.com");
        participant.put(Constants.PRIMARY_MOBILE,"9620499129");
        participant.put(Constants.PARTICIPANT_NAME,"test_user_12");
        participant.put("roles",List.of(Constants.PROVIDER));
        body.put("participant",participant);
        data.add(body);
        return JSONUtils.serialize(data);
    }

    protected String updateRequestBody() throws JsonProcessingException {
        Map<String, Object> participant = new HashMap<>();
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participant_code", "test_user_54.yopmail@swasth-hcx");
        participantData.put("endpoint_url", "http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v0.7");
        participantData.put("encryption_cert", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        participantData.put("signing_cert_path", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        participant.put("participant", participantData);
        return JSONUtils.serialize(participant);
    }
}
