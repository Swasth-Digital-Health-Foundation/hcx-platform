package org.swasth.commonscheduler.schedulers;


import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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
import org.springframework.test.context.ActiveProfiles;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.JSONUtils;
import org.swasth.commonscheduler.config.GenericConfiguration;
import org.swasth.commonscheduler.job.CommonSchedulerJob;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes ={ ParticipantValidationScheduler.class, CommonSchedulerJob.class, BaseScheduler.class})
@Import(GenericConfiguration.class)
@ActiveProfiles("test")
public class CommonSchedulerTest {
    @Autowired
    private CommonSchedulerJob commonSchedulerJob;
    @Mock
    private ParticipantValidationScheduler participantValidationScheduler;
    @MockBean
    private EventGenerator eventGenerator;
    @Mock
    private RetryScheduler retryScheduler;
    @MockBean
    private RegistryService registryService;
    @MockBean
    protected KafkaClient kafkaClient;
    @MockBean
    private PostgreSQLClient postgreSQLClient;
    private EmbeddedPostgres embeddedPostgres;
    private MockWebServer registryServer =  new MockWebServer();

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        registryServer.start(InetAddress.getByName("localhost"),8082);
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        postgreSQLClient = new PostgreSQLClient("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
        retryScheduler.postgreSQLClient = postgreSQLClient;
        retryScheduler.eventGenerator = eventGenerator;
        retryScheduler.kafkaClient = kafkaClient;
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
        Mockito.when(eventGenerator.createNotifyEvent(anyString(),anyString(),anyString(),anyList(),anyLong(),anyString(),anyString()))
                .thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        String[] args = { "ParticipantValidation" };
        commonSchedulerJob.run(args);
    }
    private List<Map<String,Object>> getProviderDetailsLessThanCurrentDay() throws Exception {
        return JSONUtils.deserialize("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"encryption_cert_expiry\": 1693301576009,\n" +
                "\"sigining_cert_expiry\": 1693301576009,\"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/test-user-52.gmail%40swasth-hcx/signing_cert_path.pem\",\"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]", ArrayList.class);
    }

    @Test
    void testCertExpiryMoreThanFiveDays() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        Mockito.when(registryService.getDetails(anyString())).thenReturn(getProviderDetailsMoreThanFiveDays());
        Mockito.when(eventGenerator.createNotifyEvent(anyString(),anyString(),anyString(),anyList(),anyLong(),anyString(),anyString()))
                .thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(),anyString(),anyString());
        String[] args = { "ParticipantValidation" };
        commonSchedulerJob.run(args);
    }
    private List<Map<String,Object>> getProviderDetailsMoreThanFiveDays() throws Exception {
        return JSONUtils.deserialize("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"encryption_cert_expiry\": 1695569991000,\n" +
                "\"sigining_cert_expiry\": 1695569991000,\"signing_cert_path\": \"https://dev-hcx-certificates.s3.ap-south-1.amazonaws.com/test-user-52.gmail%40swasth-hcx/signing_cert_path.pem\",\"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]", ArrayList.class);
    }

    @Test
    public void testRetryRequestsScheduler() throws Exception {
        postgreSQLClient.execute("CREATE TABLE payload(mid character varying PRIMARY KEY, data character varying NOT NULL, action character varying, status character varying, retrycount integer, lastupdatedon bigint)");
        when(eventGenerator.generateMetadataEvent(any())).thenReturn("mockedEvent");
        lenient().doNothing().when(kafkaClient).send(anyString(), anyString(), anyString());
        String[] args = {"Retry"};
        commonSchedulerJob.run(args);
        }
    }