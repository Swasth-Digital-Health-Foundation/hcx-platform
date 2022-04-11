package org.swasth.apigateway.cache;

import ai.grakn.redismock.RedisServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = RedisCache.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisCacheTest {

    RedisServer redisServer;

    @Autowired
    RedisCache redis;

    @BeforeAll
    public void Setup() throws IOException {
        redisServer = RedisServer.newRedisServer(6379);
        redisServer.start();
    }

    @AfterAll
    public void shutdown() {
        redisServer.stop();
    }


    @Test
    public void testSet() throws Exception {
        redis.set("test","test",10000000);
    }

    @Test
    public void testGet() throws Exception {
        redis.get("test");
    }

    @Test
    public void testGetException() throws Exception {
        redis.set("exception","123",10000000);
        Exception exception = assertThrows(Exception.class, () -> {
            redis.get("exception");
        });
        assertEquals("Exception Occurred While Fetching Data from Redis Cache for Key : exception| Exception is:redis.clients.jedis.exceptions.JedisConnectionException: Unexpected character!", exception.getMessage());
    }

    @Test
    public void testIsExistException() {
        Exception exception = assertThrows(Exception.class, () -> {
            redis.isExists("test");
        });
        assertEquals("Exception occurred while checking key exist or not in Redis Cache: test| Exception is:redis.clients.jedis.exceptions.JedisConnectionException: Unexpected character!", exception.getMessage());
    }

}
