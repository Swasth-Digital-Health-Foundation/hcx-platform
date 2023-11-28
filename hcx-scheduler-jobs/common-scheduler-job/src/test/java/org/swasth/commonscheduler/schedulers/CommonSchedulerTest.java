package org.swasth.commonscheduler.schedulers;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.commonscheduler.config.GenericConfiguration;
import org.swasth.commonscheduler.job.CommonSchedulerJob;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {ParticipantValidationScheduler.class, CommonSchedulerJob.class, BaseScheduler.class, UserSecretScheduler.class})
@Import(GenericConfiguration.class)
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = false,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        })
public class CommonSchedulerTest {
    @Autowired
    private CommonSchedulerJob commonSchedulerJob;
    @Mock
    private ParticipantValidationScheduler participantValidationScheduler;
    @MockBean
    private EventGenerator eventGenerator;
    @Mock
    private RetryScheduler retryScheduler;
    @Mock
    private UserSecretScheduler userSecretScheduler;
    @MockBean
    private RegistryService registryService;
    @MockBean
    protected KafkaClient kafkaClient;
    @MockBean
    private PostgreSQLClient postgreSQLClient;
    private EmbeddedPostgres embeddedPostgres;
    private MockWebServer registryServer = new MockWebServer();

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        registryServer.start(InetAddress.getByName("localhost"), 8082);
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        postgreSQLClient = new PostgreSQLClient("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
        retryScheduler.postgreSQLClient = postgreSQLClient;
        retryScheduler.eventGenerator = eventGenerator;
        retryScheduler.kafkaClient = kafkaClient;
        userSecretScheduler.postgreSQLClient = postgreSQLClient;
        userSecretScheduler.eventGenerator = eventGenerator;
        userSecretScheduler.kafkaClient = kafkaClient;
    }

    @AfterEach
    void teardown() throws IOException, InterruptedException {
        registryServer.shutdown();
        Thread.sleep(2000);
        embeddedPostgres.close();
    }

    @Test
    void testParticipantValidationScheduler() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        Mockito.when(registryService.getDetails(anyString())).thenReturn(getProviderDetailsLessThanCurrentDay());
        Mockito.when(eventGenerator.createNotifyEvent(anyString(), anyString(), anyString(), anyList(), anyLong(), anyString(), anyString()))
                .thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(), anyString(), anyString());
        String[] args = {"ParticipantValidation"};
        commonSchedulerJob.run(args);
        verify(kafkaClient, times(0)).send(anyString(), anyString(), anyString());
        assertEquals("mockedEvent", eventGenerator.createNotifyEvent(anyString(), anyString(), anyString(), anyList(), anyLong(), anyString(), anyString()));
    }

    private List<Map<String, Object>> getProviderDetailsLessThanCurrentDay() throws Exception {
        return JSONUtils.deserialize("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"encryption_cert_expiry\": 1693301576009,\n" +
                "\"sigining_cert_expiry\": 1693301576009,\"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/test-user-52.gmail%40swasth-hcx/signing_cert_path.pem\",\"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]", ArrayList.class);
    }

    @Test
    void testParticipantValidationSchedulers() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        Mockito.when(registryService.getDetails(anyString())).thenReturn(getProviderDetailsMoreThanFiveDays());
        Mockito.when(eventGenerator.createNotifyEvent(anyString(), anyString(), anyString(), anyList(), anyLong(), anyString(), anyString()))
                .thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(), anyString(), anyString());
        String[] args = {"ParticipantValidation"};
        commonSchedulerJob.run(args);
        verify(kafkaClient, times(0)).send(anyString(), anyString(), anyString());
        assertEquals("mockedEvent", eventGenerator.createNotifyEvent(anyString(), anyString(), anyString(), anyList(), anyLong(), anyString(), anyString()));
    }

    private List<Map<String, Object>> getProviderDetailsMoreThanFiveDays() throws Exception {
        return JSONUtils.deserialize("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"encryption_cert_expiry\": 1695569991000,\n" +
                "\"sigining_cert_expiry\": 1695569991000,\"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/test-user-52.gmail%40swasth-hcx/signing_cert_path.pem\",\"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]", ArrayList.class);
    }

    @Test
    public void testRetryRequestsScheduler() throws Exception {
        postgreSQLClient.execute("CREATE TABLE payload(mid character varying PRIMARY KEY, data character varying NOT NULL, action character varying, status character varying, retrycount integer, lastupdatedon bigint)");
        postgreSQLClient.execute("INSERT INTO payload(mid, data, action, status, retrycount, lastupdatedon) VALUES('e49e067d-60ff-40ee-b3df-08abb6c2fda1', '{}', '/coverageeligibility/check', 'request.retry',1,'1676218371439');");
        when(eventGenerator.generateMetadataEvent(any())).thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(), anyString(), anyString());
        String[] args = {"Retry"};
        commonSchedulerJob.run(args);
        verify(kafkaClient, times(0)).send(anyString(), anyString(), anyString());
        ResultSet result = postgreSQLClient.executeQuery("SELECT * FROM payload where mid='e49e067d-60ff-40ee-b3df-08abb6c2fda1';");
        if (result.next()) {
            int retryCount = result.getInt(Constants.RETRY_COUNT);
            assertEquals(2, retryCount);
        } else throw new Exception("The test case failed.");
    }

    @Test
    void testUserSecretExpired() throws Exception {
        postgreSQLClient.execute("CREATE TABLE api_access_secrets_expiry(user_id character varying, participant_code character varying, secret_generation_date bigint, secret_expiry_date bigint, username character varying NOT NULL)");
        postgreSQLClient.execute("INSERT INTO api_access_secrets_expiry(user_id, participant_code, secret_generation_date, secret_expiry_date, username) VALUES('mock15@gmail.com', 'hcxtest1051.yopmail@swasth-hcx-dev', '1696839989628', '1696833349003', 'hcxtest1051.yopmail@swasth-hcx-dev:mock15@gmail.com');");
        when(eventGenerator.generateMetadataEvent(any())).thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(), anyString(), anyString());
        String[] args = {"UserSecret"};
        commonSchedulerJob.run(args);
        String query = String.format("SELECT * FROM api_access_secrets_expiry WHERE secret_expiry_date <= %d ;", System.currentTimeMillis());
        ResultSet result = postgreSQLClient.executeQuery(query);
        while (result.next()) {
            String participant = result.getString("username");
            Assert.assertEquals("hcxtest1051.yopmail@swasth-hcx-dev:mock15@gmail.com", participant);
        }
    }

    @Test
    void testUserSecretAboutToExpire() throws Exception {
        long secret_generation_date = System.currentTimeMillis();
        long secret_expiry_date = System.currentTimeMillis() + (6) * 24L * 60 * 60 * 1000;
        postgreSQLClient.execute("CREATE TABLE api_access_secrets_expiry(user_id character varying, participant_code character varying, secret_generation_date bigint, secret_expiry_date bigint, username character varying NOT NULL)");
        String query = String.format("INSERT INTO api_access_secrets_expiry(user_id, participant_code, secret_generation_date, secret_expiry_date, username) VALUES('mock18@gmail.com', 'hcxtest6.yopmail@swasth-hcx',%s, %s, 'hcxtest6.yopmail@swasth-hcx:mock18@gmail.com');",secret_generation_date,secret_expiry_date);
        postgreSQLClient.execute(query);
        when(eventGenerator.generateMetadataEvent(any())).thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(), anyString(), anyString());
        String[] args = {"UserSecret"};
        commonSchedulerJob.run(args);
        verify(kafkaClient, times(0)).send(anyString(), anyString(), anyString());
    }
    @Test
    void testUserSecretAExpiredException() {
        try {
            String[] args = {"UserSecret"};
            commonSchedulerJob.run(args);
        } catch (Exception e) {
            String message = e.getMessage();
            assertEquals(true , message.contains("Error while performing database operation: ERROR: relation \"api_access_secrets_expiry\" does not exist\n"));
        }
    }
}

