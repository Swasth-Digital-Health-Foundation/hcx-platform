package org.swasth.redis.cache;


import com.github.fppt.jedismock.RedisServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisCacheTest {

    RedisServer redisServer;

    RedisCache redis;

    @BeforeEach
    public void setup() throws IOException {
        redisServer = RedisServer.newRedisServer().start();
        redis = new RedisCache(redisServer.getHost(), redisServer.getBindPort());
    }

    @AfterEach
    public void shutdown() throws IOException {
        redisServer.stop();
    }

    @Test
    public void testSet() throws Exception {
        redis.set("test", "test", 10000000);
    }

    @Test
    public void testGet() throws Exception {
        redis.get("test");
    }

    @Test
    public void testGetException() {
        RedisCache redisCache = new RedisCache("redisPort", 6370);
        Exception exception = assertThrows(Exception.class, () -> {
            redisCache.get("exception");
        });
        assertTrue(exception.getMessage().contains("Exception Occurred While Fetching Data from Redis Cache for Key : exception"));
    }

    @Test
    public void testSetException() {
        RedisCache redisCache = new RedisCache("redisPort", 6370);
        Exception exception = assertThrows(Exception.class, () -> {
            redisCache.set("exception","123",10000000);
        });
        assertTrue(exception.getMessage().contains("Exception Occurred While Saving Data to Redis Cache for Key : exception"));
    }

    @Test
    public void testIsExistException() {
        RedisCache redisCache = new RedisCache("redisPort", 6370);
        Exception exception = assertThrows(Exception.class,() -> {
            redisCache.isExists("test");
        });
        assertTrue(exception.getMessage().contains("Exception occurred while checking key exist or not in Redis Cache: test"));
    }

}
