package org.swasth.apigateway.cache;

import org.swasth.apigateway.constants.Constants;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.ServerException;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisCache extends RedisConnector {

    public void set(String key, String value, int ttl) throws Exception {
        Jedis jedis = getConnection();
        try{
            jedis.setex(key, ttl, value);
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Exception Occurred While Saving Data to Redis Cache for Key : " + key + "| Exception is:" + e);
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
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Exception Occurred While Fetching Data from Redis Cache for Key : " + key + "| Exception is:" + e);
        }
        finally {
            jedis.close();
        }
    }

    public boolean isExists(String key) throws Exception {
        Jedis jedis = getConnection();
        try{
            return jedis.exists(key);
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Exception occurred while checking key exist or not in Redis Cache: " + key + "| Exception is:" + e);
        }
        finally {
            jedis.close();
        }
    }
}
