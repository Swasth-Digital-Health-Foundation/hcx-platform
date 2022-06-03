package org.swasth.redis.cache;


import com.github.fppt.jedismock.RedisServer;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisCacheTest {

    RedisServer redisServer;

    RedisCache redis;

    @BeforeAll
    void setup() throws IOException {
        redisServer = RedisServer.newRedisServer().start();
        redis = new RedisCache(redisServer.getHost(), redisServer.getBindPort());
    }

    @AfterAll
    void shutdown() throws IOException {
        redisServer.stop();
    }

    @Test
    void testSet() throws Exception {
        redis.set("test", "test", 10000000);
    }

    @Test
    void testGet() throws Exception {
        redis.get("test");
    }

    @Test
    void testIsExist() throws Exception {
        redis.set("123", "test", 10000000);
        assertTrue(redis.isExists("123"));
    }

    @Test
    void testGetException() {
        RedisCache redisCache = new RedisCache("redisPort", 6370);
        Exception exception = assertThrows(Exception.class, () -> {
            redisCache.get("exception");
        });
        assertTrue(exception.getMessage().contains("Exception Occurred While Fetching Data from Redis Cache for Key : exception"));
    }

    @Test
    void testSetException() {
        RedisCache redisCache = new RedisCache("redisPort", 6370);
        Exception exception = assertThrows(Exception.class, () -> {
            redisCache.set("exception","123",10000000);
        });
        assertTrue(exception.getMessage().contains("Exception Occurred While Saving Data to Redis Cache for Key : exception"));
    }

    @Test
    void testIsExistException() {
        RedisCache redisCache = new RedisCache("redisPort", 6370);
        Exception exception = assertThrows(Exception.class,() -> {
            redisCache.isExists("test");
        });
        assertTrue(exception.getMessage().contains("Exception occurred while checking key exist or not in Redis Cache: test"));
    }

}
