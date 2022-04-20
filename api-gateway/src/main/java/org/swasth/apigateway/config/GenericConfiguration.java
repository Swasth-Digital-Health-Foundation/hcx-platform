package org.swasth.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.auditindexer.function.AuditIndexer;

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

    @Bean
    public AuditIndexer auditIndexer() throws Exception {
        return new AuditIndexer(esHost, esPort, auditIndex, auditAlias);
    }

}
