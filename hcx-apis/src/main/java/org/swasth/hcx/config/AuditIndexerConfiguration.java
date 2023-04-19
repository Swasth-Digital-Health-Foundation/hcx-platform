package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.auditindexer.function.AuditIndexer;

@Configuration
public class AuditIndexerConfiguration {

    @Value("${es.host:localhost}")
    public String esHost;

    @Value("${es.port:9200}")
    public int esPort;

    @Value("${audit.hcxIndex}")
    public String hcxIndex;

    @Value("${audit.hcxAlias}")
    public String hcxAlias;

    @Bean
    public AuditIndexer auditIndexer() throws Exception {
        return new AuditIndexer(esHost, esPort, hcxIndex, hcxAlias);
    }
}
