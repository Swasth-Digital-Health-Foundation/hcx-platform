package com.oss.apigateway.cache;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisCache extends RedisConnector {

    public void set(String key, String value) throws Exception {
        Jedis jedis = getConnection();
        try{
            jedis.set(key,value);
        } catch (Exception e) {
            throw new Exception("Exception Occurred While Saving Data to Redis Cache for Key : " + key + "| Exception is:", e);
        }
        finally {
            jedis.close();
        }
    }

    public String get(String key) throws Exception {
        Jedis jedis = getConnection();
        try{
            return jedis.get(key);
        } catch (Exception e) {
            throw new Exception("Exception Occurred While Fetching Data from Redis Cache for Key : " + key + "| Exception is:", e);
        }
        finally {
            jedis.close();
        }
    }
}
