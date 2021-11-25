package org.swasth.job.connector;

import org.swasth.job.Platform;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnector {

    private static String redisHost = Platform.getString("redis.host","localhost");
    private static Integer redisPort = Platform.getInteger("redis.port",6379);
    private static Integer maxConnections = Platform.getInteger("redis.maxConnections", 128);
    private static JedisPool jedisPool = new JedisPool(getConfig(), redisHost, redisPort);

    public static Jedis getConnection() throws Exception {
        try{
            return jedisPool.getResource();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static void returnConnection(Jedis jedis) throws Exception {
        try{
            if (jedis != null) jedisPool.returnResource(jedis);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static JedisPoolConfig getConfig(){
       JedisPoolConfig config = new JedisPoolConfig();
       config.setMaxTotal(maxConnections);
       config.setBlockWhenExhausted(true);
       return config;
    }

}
