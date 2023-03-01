package org.swasth.apigateway.service;

import ai.grakn.redismock.RedisServer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.swasth.redis.cache.RedisCache;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = {RegistryService.class, RedisCache.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RegistryServiceTest {

    private final MockWebServer registryServer =  new MockWebServer();
    private RedisServer redisServer;

    @Autowired
    private RegistryService registryService;

    @MockBean
    private RedisCache redisCache;

    @BeforeEach
    public void setup() throws IOException {
        registryServer.start(InetAddress.getByName("localhost"),8080);
        redisServer = RedisServer.newRedisServer(6379);
        redisServer.start();
    }

    @AfterEach
    public void teardown() throws IOException {
        registryServer.shutdown();
        redisServer.stop();
    }

    @Test
    void check_registry_service_internal_server_exception_scenario() {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{ \"timestamp\": \"2023-02-28T10:45:22.744+00:00\", \"error\": { \"code\": \"null\", \"message\": \"Invalid search filters\", \"trace\": null } }")
                .addHeader("Content-Type", "application/json"));

        Exception exception = assertThrows(Exception.class, () -> registryService.fetchDetails("osid", "test_123"));
        assertTrue(exception.getMessage().contains("Error while fetching the participant details from the registry"));
    }

    @Test
    void check_registry_service_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"timestamp\": 1677646697335, \"participants\": [{ \"status\": \"Active\", \"signing_cert_path\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem\", \"participant_name\": \"test provider 2\", \"address\": { \"pincode\": \"500805\", \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"osid\": \"43581cf1-9b0a-4f1b-ba4a-b27010155f13\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\" }, \"payment_details\": { \"ifsc_code\": \"ICICI\", \"account_number\": \"4707890099809809\", \"osid\": \"c5451b6d-54a1-4f5b-b55f-b897a9b57159\" }, \"encryption_cert_expiry\": 1666612517000, \"roles\": [ \"provider\" ], \"primary_mobile\": \"9493347192\", \"osid\": \"5b58a417-3f88-42e1-958a-fe5f3d462929\", \"osOwner\": [ \"fe329aeb-6bc3-47ea-a759-5494ea90994e\" ], \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem\", \"linked_registry_codes\": [ \"22344\" ], \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080/v0.7\", \"phone\": [ \"040-12345678\" ], \"participant_code\": \"testprovider2.swasthmock@swasth-hcx\", \"primary_email\": \"testprovider2@swasthmock.com\" }] }")
                .addHeader("Content-Type", "application/json"));

        Map<String,Object> result = registryService.fetchDetails("osid", "test_123");
        assertEquals("testprovider2.swasthmock@swasth-hcx", result.get("participant_code"));
    }

    @Test
    void check_registry_service_empty_response_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"timestamp\": 1677646697335, \"participants\": []}")
                .addHeader("Content-Type", "application/json"));

        Map<String,Object> result = registryService.fetchDetails("osid", "test_123");
        assertTrue(result.isEmpty());
    }

}
