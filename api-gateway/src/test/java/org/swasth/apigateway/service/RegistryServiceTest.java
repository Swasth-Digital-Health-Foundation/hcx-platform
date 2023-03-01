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
    void check_registry_service_empty_response_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"timestamp\": 1677646697335, \"participants\": []}")
                .addHeader("Content-Type", "application/json"));

        Map<String,Object> result = registryService.fetchDetails("participant_code", "testprovider2.swasthmock@swasth-hcx");
        assertTrue(result.isEmpty());
    }

}
