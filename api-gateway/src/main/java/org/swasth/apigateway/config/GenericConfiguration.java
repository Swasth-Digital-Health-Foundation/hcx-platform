package org.swasth.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.redis.cache.RedisCache;

@Configuration
public class GenericConfiguration {

    @Value("${es.host}")
    private String esHost;

    @Value("${es.port}")
    private int esPort;

    @Value("${audit.index}")
    private String auditIndex;

    @Value("${audit.alias}")
    private String auditAlias;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public AuditIndexer auditIndexer() throws Exception {
        return new AuditIndexer(esHost, esPort, auditIndex, auditAlias);
    }

    @Bean
    public RedisCache redisCache() {
        return new RedisCache(redisHost, redisPort);
    }

}
