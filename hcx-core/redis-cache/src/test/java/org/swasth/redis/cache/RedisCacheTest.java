package org.swasth.redis.cache;

import com.github.fppt.jedismock.RedisServer;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisCacheTest {

    private RedisServer redisServer;

    private RedisCache redis;

    private RedisCache invalidRedis;

    @BeforeAll
    void setup() throws IOException {
        redisServer = RedisServer.newRedisServer().start();
        redis = new RedisCache(redisServer.getHost(), redisServer.getBindPort());
        invalidRedis = new RedisCache("redisPort", 6370);
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
        Exception exception = assertThrows(Exception.class, () -> invalidRedis.get("exception"));
        assertTrue(exception.getMessage().contains("Exception Occurred While Fetching Data from Redis Cache for Key : exception"));
    }

    @Test
    void testSetException() {
        Exception exception = assertThrows(Exception.class, () -> invalidRedis.set("exception","123",10000000));
        assertTrue(exception.getMessage().contains("Exception Occurred While Saving Data to Redis Cache for Key : exception"));
    }

    @Test
    void testIsExistException() {
        Exception exception = assertThrows(Exception.class,() -> invalidRedis.isExists("test"));
        assertTrue(exception.getMessage().contains("Exception occurred while checking key exist or not in Redis Cache: test"));
    }

    @Test
    void testDelete() throws Exception {
        redis.set("123", "test", 10000000);
        assertEquals(Long.valueOf(1), redis.delete("123"));
    }

    @Test
    void testDeleteException() {
        Exception exception = assertThrows(Exception.class,() -> invalidRedis.delete("test"));
        assertTrue(exception.getMessage().contains("Exception occurred while deleting the record in redis cache for Key : test"));
    }

    @Test
    void testHealthSuccess() throws Exception {
        boolean isValid = redis.isHealthy();
        assertTrue(isValid);
    }

    @Test
    void testHealthFail() throws Exception {
        boolean isInvalid = invalidRedis.isHealthy();
        assertFalse(isInvalid);

    }

}
