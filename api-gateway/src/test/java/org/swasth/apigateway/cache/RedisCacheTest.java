package org.swasth.apigateway.cache;

import ai.grakn.redismock.RedisServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RedisCache.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisCacheTest {

    RedisServer redisServer;

    @Autowired
    RedisCache redis;

    @BeforeEach
    public void Setup() throws IOException {
        redisServer = RedisServer.newRedisServer(6379);
        redisServer.start();
    }

    @AfterEach
    public void shutdown() {redisServer.stop();
    }


    @Test
    void testSet() throws Exception {
        ReflectionTestUtils.setField(redis, "redisPort", 6379);
        redis.set("test","test",10000000);
    }

    @Test
    void testGet() throws Exception {
        ReflectionTestUtils.setField(redis, "redisPort", 6379);
        redis.get("test");
    }

    @Test
    public void testGetException(){
        ReflectionTestUtils.setField(redis, "redisPort", 6370);
        Exception exception = assertThrows(Exception.class, () -> {
            redis.get("exception");
        });
        assertTrue(exception.getMessage().contains("Exception Occurred While Fetching Data from Redis Cache for Key : exception"));
    }

    @Test
    public void testSetException() {
        ReflectionTestUtils.setField(redis, "redisPort", 6370);
        Exception exception = assertThrows(Exception.class, () -> {
            redis.set("exception","123",10000000);
        });
        assertTrue(exception.getMessage().contains("Exception Occurred While Saving Data to Redis Cache for Key : exception"));
    }

    @Test
    public void testIsExistException() {
        ReflectionTestUtils.setField(redis, "redisPort", 6370);
        Exception exception = assertThrows(Exception.class,() -> {
            redis.isExists("test");
        });
        assertTrue(exception.getMessage().contains("Exception occurred while checking key exist or not in Redis Cache: test"));
    }

}
