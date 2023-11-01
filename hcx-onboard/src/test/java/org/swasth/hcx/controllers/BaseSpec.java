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

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static org.swasth.common.utils.Constants.EMAIL;
import static org.swasth.common.utils.Constants.MOBILE;


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

    @MockBean
    protected KafkaClient kafkaClient;
    private EmbeddedPostgres embeddedPostgres;

    @BeforeEach
     void setup() throws Exception {
        registryServer.start(InetAddress.getByName("localhost"),8082);
        hcxApiServer.start(InetAddress.getByName("localhost"),8080);
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        String jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres");
        postgreSQLClient = new PostgreSQLClient(jdbcUrl, "postgres", "postgres");
        jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres");
        postgreSQLClient = new PostgreSQLClient(jdbcUrl, "postgres", "postgres");

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

    protected String authorizationHeaderForGeneratePassword(){
        return "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzYjU1MGM5Mi00MzY0LTRkOGUtOGFmMS0yY2EyN2Q0MDlhM2IiLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwOlwvXC9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwXC9hdXRoXC9yZWFsbXNcL3N3YXN0aC1oY3gtdXNlcnMiLCJ0eXAiOiJCZWFyZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJoY3h0ZXN0cHJvdmlkZXI5MDAxQHlvcG1haWwuY29tIiwiYXVkIjoiYWNjb3VudCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1zd2FzdGgtaGVhbHRoLWNsYWltLWV4Y2hhbmdlLXVzZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl0sInRlbmFudF9yb2xlcyI6W3sicm9sZSI6ImFkbWluIiwicGFydGljaXBhbnRfY29kZSI6ImhjeHRlc3Rwcm92aWRlcjkwMDAueW9wbWFpbEBzd2FzdGgtaGN4LWRldiJ9XX0sInVzZXJfaWQiOiJoY3h0ZXN0cHJvdmlkZXI5MDAxQHlvcG1haWwuY29tIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJleHAiOjE2OTg3NjgzMDYsInNlc3Npb25fc3RhdGUiOiJiODkxODk3YS0xMTI5LTQ3NTEtOTc1Mi03NzA3NjhmOGFhYjkiLCJpYXQiOjE2OTg3MzIzMDYsImp0aSI6ImY2MmFhYzI1LTM2YWItNGQ1My1hOWExLWY1MWY1ODI3ZDBiZSIsImVtYWlsIjoiaGN4dGVzdHByb3ZpZGVyOTAwMUB5b3BtYWlsLmNvbSIsImVudGl0eSI6WyJVc2VyIl19.wMsZUzbopJuru2nSrGaEc_aaQQnF7WV4TLF5vc84ohrFY9GvPX_ngqvdNvYbfcUEksxMn7DR1kDIbA9yQrg26uoBowb4pFsGFT2_Ht9Se7me4PHvOft98HBL3BxoO_2I-h8z4a0gflT8a9tej4KIPTuxoUlMxC1nZVP6HFKdP0x0d2oVgDcBl8yFVj9bwSDNfM1nrkULnkSQY2vfaMpuZQJHrcJvjvL__rvCH1-citkPQtRwNL9i1UOHSe_Ge8uOalbnixcK6zBLXTWOCWpapzQACtbKF8k2doTvGuQayjaUf4rOgwfO975qgzlQDgfdeNLn-B3ai3MJtyRtxfWf0g";
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

    protected String verifyPayorRequestBody() throws JsonProcessingException {
        List<Map<String , Object>> data = new ArrayList<>();
        Map<String,Object> body = new HashMap<>();
        body.put("type", "onboard-through-verifier");
        body.put("verifier_code","wemeanhospital+mock_payor.yopmail@swasth-hcx-dev");
        Map<String , Object> participant = new HashMap<>();
        participant.put(Constants.PRIMARY_EMAIL,"obama02@yopmail.com");
        participant.put(Constants.PRIMARY_MOBILE,"9620499129");
        participant.put(Constants.PARTICIPANT_NAME,"test_user_12");
        participant.put("roles",List.of(Constants.PAYOR));
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

    protected String onboardUpdateRequestBody() throws JsonProcessingException {
        Map<String, Object> participant = new HashMap<>();
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("participant_code", "test_user_54.yopmail@swasth-hcx");
        participantData.put("endpoint_url", "http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v0.7");
        participantData.put("encryption_cert", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        participantData.put("signing_cert_path", "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
        Map<String , Object>  onboard_validation_properties = new HashMap<>();
        onboard_validation_properties.put("email","activation");
        onboard_validation_properties.put("phone","verification");
        participantData.put("onboard_validation_properties",onboard_validation_properties);
        participant.put("participant", participantData);
        return JSONUtils.serialize(participant);
    }

    protected String verifyIdentityRequestBody() throws JsonProcessingException {
        Map<String,Object> participant =  new HashMap<>();
        participant.put("participant_code","test_user_52.yopmail@swasth-hcx");
        participant.put("status","accepted");
        return JSONUtils.serialize(participant);
    }

    protected String verifyIdentityRejectRequestBody() throws JsonProcessingException {
        Map<String,Object> participant =  new HashMap<>();
        participant.put("participant_code","test_user_52.yopmail@swasth-hcx");
        participant.put("status","rejected");
        return JSONUtils.serialize(participant);
    }

    protected String verifyIdentityOtherThanAllowedStatus() throws JsonProcessingException {
        Map<String,Object> participant =  new HashMap<>();
        participant.put("participant_code","test_user_52.yopmail@swasth-hcx");
        participant.put("status","marked");
        return JSONUtils.serialize(participant);
    }

    protected String verificationLinkRequestBody() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("participant_code","test_user_52.yopmail@swasth-hcx");
        participant.put("channel", Arrays.asList(EMAIL,MOBILE)); participant.put("channel",Arrays.asList(EMAIL,MOBILE));
        return JSONUtils.serialize(participant);
    }

    protected String applicantVerifyRequestBody() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("applicant_code","test_user_55.yopmail@swasth-hcx");
        participant.put("verifier_code","testpayor1.icici@swasth-hcx-dev");
        participant.put("email","test_user_555@yopmail.com");
        participant.put("mobile","9899912323");
        participant.put("applicant_name", "olly");
        participant.put("role","payer");
        return JSONUtils.serialize(participant);
    }

    protected String applicantGetInfoRequestBody() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("applicant_code","test_user_95.yopmail@swasth-hcx");
        participant.put("verifier_code","testpayor1.icici@swasth-hcx-dev");
        return JSONUtils.serialize(participant);
    }

    protected String applicantGetInfoJwtToken() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("verification_token", "eyJ0eXAiOiJqd3QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJwYXlvci10ZXN0LXVzZXItMyIsImlzcyI6InRlc3RwYXlvcjEuaWNpY2lAc3dhc3RoLWhjeC1kZXYiLCJleHAiOjE2NzU5MzE4NTYzOTIsImlhdCI6MTY3NTg0NTQ1NjM5MiwianRpIjoiNzY4MjlmYjctMjgzOS00YTFkLTgxOTYtMmY4MTU4MjQyYjdiIn0.HJ9Sd_u209oSoSH-CIBAkr3L6wlTCFWyzLslnQaxRYi-8uTuT9naurfKUMmljYkmsznUwuKsMvOKRJI7Q3caSEn6Y0kXwmvrAcvv8oX7TULncXZFXV3DiBD0KCvpYsm_VadfwPKEl55Giyt_sAi0lSfKtWcTPG7t8HRuRYs3PI86blZVWeiYZaQoFnmcGZbwc5H7U70fWL5Fr_Gwu4DjNlD-Wx63PIBmlMq6UjvfoF0ss-LU0ELimTFFzqw-4tCbJ1pP8YhPmDPaEykiDMeLMuzyC_Vp0gLYBa18YjJPqiQdK4jEBLjhOzawRKwGvVFC5--ccOr8AoR6kK2jKPIGmw");
        return JSONUtils.serialize(participant);
    }

    protected String applicantSearchRequestBody() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        Map<String , Object> filters = new HashMap<>();
        Map<String ,Object> participantCode = new HashMap<>();
        participantCode.put("eq","provider-swasth-mock-provider-dev");
        filters.put("participant_code" ,participantCode);
        participant.put("filters",filters);
        return JSONUtils.serialize(participant);
    }

    protected String onboardUserInviteRequestBody() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("participant_code","testprovider1.apollo@swasth-hcx-dev");
        participant.put("email","mock-invite@yopmail.com");
        participant.put("role","admin");
        participant.put("invited_by","mock42@gmail.com");
        return JSONUtils.serialize(participant);
    }

    protected String onboardUserInviteJwtTokenException() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token", "eyJhbGciOiJSUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJwYXJ0aWNpcGFudF9jb2RlIjoidGVzdHByb3ZpZGVyMS5hcG9sbG9Ac3dhc3RoLWhjeC1kZXYiLCJpc3MiOiJoY3hnYXRld2F5LnN3YXN0aEBzd2FzdGgtaGN4LWRldiIsInR5cCI6Imludml0ZSIsImludml0ZWRfYnkiOiJtb2NrNDJAZ21haWwuY29tIiwiZXhwIjoiMTY5MTQyNDMxOTYzMCIsImlhdCI6IjE2OTEzMzc5MTk2MzAiLCJqdGkiOiJkYWUwMzU5YS0yMmFjLTQzZTAtYjZjMy1kOWFiZTRjYzU1NDIiLCJlbWFpbCI6Im1vY2staW52aXRlQHlvcG1haWwuY29tIn0.gq1ZJSXwr-SH_1Ib22R-d8G1X5MmmYpBwiUYBbRJDjbm-yCkpUtGHvWvmlV6GkT-PxxRQmiIlizqZJeyUEuTXs4FMvNDmnkzcrz6zb9xAUQVK7jQXmqLwZ14aDWz0L2470r7M1C4Uo6vd2mg67FmY6wHFiib-vfjwYKRXSv_73PxN5c4xs38JBCi8sz8Z3BKmcOhhkL6oPwvB1rC2QKRceXtDYIWQrU6N_uMqigdQeUqTTXVW_zACdgKaG2aWb1pac3ov-jzTnFS_ls7FpBvuDsfkQ7FqCIHBQyoT5aBBDUSxJxNqlzV8xVX8Yxuk2rjmKVHZmvSlVCohShJFvyhrw");
        Map<String , Object> users = new HashMap<>();
        users.put( "user_name", "test_user");
        users.put("email","mock-invite@yopmail.com");
        users.put("mobile", "9620499129");
        List<Map<String, Object>> tenant_roles = new ArrayList<>();
        Map<String, Object> participants = new HashMap<>();
        participants.put("participant_code","testprovider1.apollo@swasth-hcx-dev");
        participants.put( "role", "admin");
        tenant_roles.add(participants);
        users.put("tenant_roles",tenant_roles);
        users.put( "created_by","testprovider1.apollo@swasth-hcx-dev");
        return JSONUtils.serialize(participant);
    }

    protected String onboardUserInviteJwtToken() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token", "eyJ0eXBlIjoiand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJ0ZXN0aGN0ZXMxM0B5b3BtYWlsLmNvbSIsInBhcnRpY2lwYW50X25hbWUiOiJ0ZXN0LXBheW9yIiwicGFydGljaXBhbnRfY29kZSI6InRlc3RoY3RlczEzLnlvcG1haWxAc3dhc3RoLWhjeCIsImlzcyI6ImhjeGdhdGV3YXkuc3dhc3RoQHN3YXN0aC1oY3gtZGV2IiwidHlwIjoiZW1haWwiLCJpYXQiOjE2OTA1MjU3ODc3NzUsImp0aSI6Ijg5ZThjYWQ5LTNjZDMtNDcxNS1iODkzLTU5NzMzNGMyODBmZiJ9.NNL_BI9f1mMejUZZXzk8ltNo4-m9R5p23Rbj96QdUxQN-78jl1G6P2nMuDeD57RCDZ1GcQAdNdvTr4XrohaZerXE4aX9UBymfga6wGa2U8s2SXXc0UyQWMrM1bNP_Aw28UocHuM10gzW0hWCXW6UnPGYQudUlGJCA1Jkd3FeD6H2FBnPIQdZphmIrL6KlTiF6anEDAhuQGI0A5mfilRg2ewCjU5LQn6iik-Jtc_4aMqZJ-WbT_oy7rqYkbuzxFJrn3LjBrwjtIdi_mwKZowrObyWBLEwVFnC0ve0SjhNncxmOV3kDMoNIaQTMf7GDohXHtFijU7SomDeuqF1zIGfmw");
        Map<String , Object> users = new HashMap<>();
        users.put( "user_name", "Abhishek");
        users.put("email","mock-invite@yopmail.com");
        users.put("mobile", "9620499129");
        List<Map<String, Object>> tenant_roles = new ArrayList<>();
        Map<String, Object> participants = new HashMap<>();
        participants.put("participant_code","testprovider1.apollo@swasth-hcx-dev");
        participants.put( "role", "admin");
        tenant_roles.add(participants);
        users.put("tenant_roles",tenant_roles);
        users.put( "created_by","testprovider1.apollo@swasth-hcx-dev");
        participant.put("user",users);
        return JSONUtils.serialize(participant);
    }

    protected String userInviteRejectException() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token","eyJ0eXBlIjoiand0IiwiYWxnIjoiUlMyNTYifQ.eyJyb2xlIjoidmlld2VyIiwicGFydGljaXBhbnRfY29kZSI6InRlc3Rwcm92aWRlcjEuYXBvbGxvQHN3YXN0aC1oY3gtZGV2IiwiaXNzIjoiaGN4Z2F0ZXdheS5zd2FzdGhAc3dhc3RoLWhjeC1kZXYiLCJ0eXAiOiJpbnZpdGUiLCJpbnZpdGVkX2J5IjoibW9jazQyQGdtYWlsLmNvbSIsImV4cCI6MTY4NzQyMDE0NzY3OCwiaWF0IjoxNjg3MzMzNzQ3Njc4LCJqdGkiOiI4YzM0MWEzNS04MDFhLTQwYjQtYjRjYi1mZGQ1ZjcwZDAxZTciLCJlbWFpbCI6Im1vY2staW52aXRlQHlvcG1haWwuY29tIn0.MqyBWyS0sQSHRlXHaWTlb9hJZyqjICOc0oSwviHKQ0wDQ3xNmpBjLKu2naOzfozPIdRHtfYkxb_5fca_cOPV5zyQeyqIH6prcaDKPnPDJIwY2VxvsR2njJnAPK5xRuSaqahTgYfzoVF7PI4nAPCSRYCJqdMXMrBIrY10uoN7EWY9VjfbrYiIgwvEBFCqAI-V0SHziyKh8ufNGT3ueKocm4ittFI3qUMP7i0AYx29CV84kBNPB2-fz_TJY_WmWDRnrSQR536PROlv3MASOsHR3iVa2HSOj9VwDQFwV1MpF8p9VY-gz2K6JOxyJvhw_1iJnmjWKjERlqOjy0KdBl2B5A");
        Map<String , Object> user = new HashMap<>();
        user.put("email","mock41@gmail.com");
        participant.put("user",user);
        return JSONUtils.serialize(participant);
    }

    protected String applicantPasswordRequestBody() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("participant_code","hcxtestprovider9000.yopmail@swasth-hcx-dev");
        return JSONUtils.serialize(participant);
    }

    protected String applicantVerifyWithJwtToken() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token", "eyJ0eXBlIjoiand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJ0ZXN0aGN0ZXMxM0B5b3BtYWlsLmNvbSIsInBhcnRpY2lwYW50X25hbWUiOiJ0ZXN0LXBheW9yIiwicGFydGljaXBhbnRfY29kZSI6InRlc3RoY3RlczEzLnlvcG1haWxAc3dhc3RoLWhjeCIsImlzcyI6ImhjeGdhdGV3YXkuc3dhc3RoQHN3YXN0aC1oY3gtZGV2IiwidHlwIjoiZW1haWwiLCJpYXQiOjE2OTA1MjU3ODc3NzUsImp0aSI6Ijg5ZThjYWQ5LTNjZDMtNDcxNS1iODkzLTU5NzMzNGMyODBmZiJ9.NNL_BI9f1mMejUZZXzk8ltNo4-m9R5p23Rbj96QdUxQN-78jl1G6P2nMuDeD57RCDZ1GcQAdNdvTr4XrohaZerXE4aX9UBymfga6wGa2U8s2SXXc0UyQWMrM1bNP_Aw28UocHuM10gzW0hWCXW6UnPGYQudUlGJCA1Jkd3FeD6H2FBnPIQdZphmIrL6KlTiF6anEDAhuQGI0A5mfilRg2ewCjU5LQn6iik-Jtc_4aMqZJ-WbT_oy7rqYkbuzxFJrn3LjBrwjtIdi_mwKZowrObyWBLEwVFnC0ve0SjhNncxmOV3kDMoNIaQTMf7GDohXHtFijU7SomDeuqF1zIGfmw");
        participant.put("status", "successful");
        return JSONUtils.serialize(participant);
    }

    protected String applicantVerifyWithJwtTokenFail() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token", "eyJ0eXBlIjoiand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJ0ZXN0aGN0ZXMxM0B5b3BtYWlsLmNvbSIsInBhcnRpY2lwYW50X25hbWUiOiJ0ZXN0LXBheW9yIiwicGFydGljaXBhbnRfY29kZSI6InRlc3RoY3RlczEzLnlvcG1haWxAc3dhc3RoLWhjeCIsImlzcyI6ImhjeGdhdGV3YXkuc3dhc3RoQHN3YXN0aC1oY3gtZGV2IiwidHlwIjoiZW1haWwiLCJpYXQiOjE2OTA1MjU3ODc3NzUsImp0aSI6Ijg5ZThjYWQ5LTNjZDMtNDcxNS1iODkzLTU5NzMzNGMyODBmZiJ9.NNL_BI9f1mMejUZZXzk8ltNo4-m9R5p23Rbj96QdUxQN-78jl1G6P2nMuDeD57RCDZ1GcQAdNdvTr4XrohaZerXE4aX9UBymfga6wGa2U8s2SXXc0UyQWMrM1bNP_Aw28UocHuM10gzW0hWCXW6UnPGYQudUlGJCA1Jkd3FeD6H2FBnPIQdZphmIrL6KlTiF6anEDAhuQGI0A5mfilRg2ewCjU5LQn6iik-Jtc_4aMqZJ-WbT_oy7rqYkbuzxFJrn3LjBrwjtIdi_mwKZowrObyWBLEwVFnC0ve0SjhNncxmOV3kDMoNIaQTMf7GDohXHtFijU7SomDeuqF1zIGfmw");
        participant.put("status", "failed");
        return JSONUtils.serialize(participant);
    }

    protected  String applicantVerifyJwtTokenMobile() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token","eyJ0eXBlIjoiand0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI5NjIwNDk5MTI5IiwicGFydGljaXBhbnRfbmFtZSI6ImhjeHB5cjgwIiwicGFydGljaXBhbnRfY29kZSI6InBheXJfaGN4cHlyXzU5ODg4MUBzd2FzdGgtaGN4LWRldiIsImlzcyI6ImhjeGdhdGV3YXkuc3dhc3RoQHN3YXN0aC1oY3gtZGV2IiwidHlwIjoicGhvbmUiLCJpYXQiOjE2OTg2NTU5NzEwNjAsImp0aSI6ImIyMGY3MjRjLThkZmQtNDJhMy05ZmMxLWUxZjFjZTlkMTgwMiJ9.hg6EoIL-IbgKh8-Jb1dRcL98DY1ASC1JoW4_Fmgb4EbZvvjbYALm_RZyxMrdhDgaES_jNiyqC_oeE81YUgtu5s-9NIxxthNomi_LZCITheJMK0BoHQ9QfvbQ2eawOpFHFnXDC5d_VVzOEliEG06TRoouahjtgVoTJkdMOj5AfX0pF2aThCo13HgTHUp6BmP06MpAwuANCjlh23eUonpOCLDMMbNf3uehFsiwvFFLgJQioh2mKnniiu77Hus4Cm_4WvbB_03FIlr3zD9dMjYwEc6nD1WXNUSL7cEaFbLS6xB6xwTHzgrv0IIlWbiVHkH2IdHiwAbaivYgVyWYtKrC6w");
        participant.put("status","successful");
        return JSONUtils.serialize(participant);
    }

    protected String applicantVerifyInvalidJwtToken() throws JsonProcessingException {
        Map<String , Object> participant = new HashMap<>();
        participant.put("jwt_token", "eyJhbGciOiJSUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJwYXJ0aWNpcGFudF9jb2RlIjoidGVzdHByb3ZpZGVyMS5hcG9sbG9Ac3dhc3RoLWhjeC1kZXYiLCJpc3MiOiJoY3hnYXRld2F5LnN3YXN0aEBzd2FzdGgtaGN4LWRldiIsInR5cCI6Imludml0ZSIsImludml0ZWRfYnkiOiJtb2NrNDJAZ21haWwuY29tIiwiZXhwIjoiMTY5MTQyNDMxOTYzMCIsImlhdCI6IjE2OTEzMzc5MTk2MzAiLCJqdGkiOiJkYWUwMzU5YS0yMmFjLTQzZTAtYjZjMy1kOWFiZTRjYzU1NDIiLCJlbWFpbCI6Im1vY2staW52aXRlQHlvcG1haWwuY29tIn0.gq1ZJSXwr-SH_1Ib22R-d8G1X5MmmYpBwiUYBbRJDjbm-yCkpUtGHvWvmlV6GkT-PxxRQmiIlizqZJeyUEuTXs4FMvNDmnkzcrz6zb9xAUQVK7jQXmqLwZ14aDWz0L2470r7M1C4Uo6vd2mg67FmY6wHFiib-vfjwYKRXSv_73PxN5c4xs38JBCi8sz8Z3BKmcOhhkL6oPwvB1rC2QKRceXtDYIWQrU6N_uMqigdQeUqTTXVW_zACdgKaG2aWb1pac3ov-jzTnFS_ls7FpBvuDsfkQ7FqCIHBQyoT5aBBDUSxJxNqlzV8xVX8Yxuk2rjmKVHZmvSlVCohShJFvyhrw");
        participant.put("status", "successful");
        return JSONUtils.serialize(participant);
    }

    protected String userInviteReject() throws JsonProcessingException {
        Map<String ,Object> participant = new HashMap<>();
        participant.put("jwt_token","eyJ0eXBlIjoiand0IiwiYWxnIjoiUlMyNTYifQ.eyJyb2xlIjoidmlld2VyIiwicGFydGljaXBhbnRfY29kZSI6InRlc3Rwcm92aWRlcjEuYXBvbGxvQHN3YXN0aC1oY3gtZGV2IiwiaXNzIjoiaGN4Z2F0ZXdheS5zd2FzdGhAc3dhc3RoLWhjeC1kZXYiLCJ0eXAiOiJpbnZpdGUiLCJpbnZpdGVkX2J5IjoibW9jazQyQGdtYWlsLmNvbSIsImV4cCI6MTY4NzQyMDE0NzY3OCwiaWF0IjoxNjg3MzMzNzQ3Njc4LCJqdGkiOiI4YzM0MWEzNS04MDFhLTQwYjQtYjRjYi1mZGQ1ZjcwZDAxZTciLCJlbWFpbCI6Im1vY2staW52aXRlQHlvcG1haWwuY29tIn0.MqyBWyS0sQSHRlXHaWTlb9hJZyqjICOc0oSwviHKQ0wDQ3xNmpBjLKu2naOzfozPIdRHtfYkxb_5fca_cOPV5zyQeyqIH6prcaDKPnPDJIwY2VxvsR2njJnAPK5xRuSaqahTgYfzoVF7PI4nAPCSRYCJqdMXMrBIrY10uoN7EWY9VjfbrYiIgwvEBFCqAI-V0SHziyKh8ufNGT3ueKocm4ittFI3qUMP7i0AYx29CV84kBNPB2-fz_TJY_WmWDRnrSQR536PROlv3MASOsHR3iVa2HSOj9VwDQFwV1MpF8p9VY-gz2K6JOxyJvhw_1iJnmjWKjERlqOjy0KdBl2B5A");
        Map<String , Object> user = new HashMap<>();
        user.put("email","mock41@gmail.com");
        participant.put("user",user);
        return JSONUtils.serialize(participant);
    }
}
