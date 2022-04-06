package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.auditindexer.function.AuditIndexer;

@Configuration
public class AuditIndexerConfiguration {

    @Value("${es.host}")
    public String esHost;

    @Value("${es.port}")
    public int esPort;

    @Value("${audit.index}")
    public String auditIndex;

    @Value("${audit.alias}")
    public String auditAlias;

    @Bean
    public AuditIndexer auditIndexer() throws Exception {
        return new AuditIndexer(esHost, esPort, auditIndex, auditAlias);
    }
}
