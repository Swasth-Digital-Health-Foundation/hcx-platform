package org.swasth.redis.cache;

import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;
import redis.clients.jedis.Jedis;


public class RedisCache {

    private String redisHost;
    private int redisPort;

    public RedisCache(String redisHost, int redisPort) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    public Jedis getConnection() throws Exception {
        try{
            return new Jedis(redisHost,redisPort);
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Error connecting to redis server " + e);
        }
    }

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

    public Long delete(String key) throws Exception {
        Jedis jedis = getConnection();
        try{
            return jedis.del(key);
        } catch (Exception e) {
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, "Exception occurred while deleting the record in redis cache for Key : " + key + "| Exception is:" + e);
        }
        finally {
            jedis.close();
        }
    }
}
