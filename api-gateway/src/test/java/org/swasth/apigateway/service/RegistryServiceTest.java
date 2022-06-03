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
public class RegistryServiceTest {

    private final MockWebServer registryServer =  new MockWebServer();
    private RedisServer redisServer;

    @Autowired
    private RegistryService registryService;

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
    void check_registry_service_success_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"test\":\"123\"}]")
                .addHeader("Content-Type", "application/json"));

        Map<String,Object> result = registryService.fetchDetails("osid", "test_123");
        assertEquals("123", result.get("test"));
    }

    @Test
    void check_registry_service_empty_response_scenario() throws Exception {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        Map<String,Object> result = registryService.fetchDetails("osid", "test_123");
        assertTrue(result.isEmpty());
    }

    @Test
    void check_registry_service_internal_server_exception_scenario() {
        registryServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        Exception exception = assertThrows(Exception.class, () -> registryService.fetchDetails("osid", "test_123"));
        assertEquals("Error in fetching the participant details400", exception.getMessage());
    }

}
