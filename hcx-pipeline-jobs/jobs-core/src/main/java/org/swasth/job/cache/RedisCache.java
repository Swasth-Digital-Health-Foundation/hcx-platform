package org.swasth.job.cache;

import org.swasth.job.connector.RedisConnector;
import redis.clients.jedis.Jedis;

public class RedisCache extends RedisConnector {

    public static void set(String key, String value) throws Exception {
       Jedis jedis = getConnection();
       try{
           jedis.set(key,value);
       } catch (Exception e) {
           throw new Exception("Exception Occurred While Saving Data to Redis Cache for Key : " + key + "| Exception is:", e);
       } finally {
           returnConnection(jedis);
       }
    }

    public static String get(String key) throws Exception {
        Jedis jedis = getConnection();
        try{
            return jedis.get(key);
        } catch (Exception e) {
            throw new Exception("Exception Occurred While Fetching Data from Redis Cache for Key : " + key + "| Exception is:", e);
        } finally {
            returnConnection(jedis);
        }
    }
}
