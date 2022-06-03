package org.swasth.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.redis.cache.RedisCache;

@Configuration
public class GenericConfiguration {

    @Value("${es.host:localhost}")
    private String esHost;

    @Value("${es.port:9200}")
    private int esPort;

    @Value("${audit.index:hcx_audit}")
    private String auditIndex;

    @Value("${audit.alias:hcx_audit}")
    private String auditAlias;

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:9200}")
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
