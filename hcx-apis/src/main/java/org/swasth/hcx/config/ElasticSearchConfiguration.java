package org.swasth.hcx.config;


import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.swasth.auditindexer.utils.ElasticSearchUtil;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.swasth.hcx.repository")
//@ComponentScan(basePackages = {"org.swasth.hcx"})
public class ElasticSearchConfiguration extends AbstractElasticsearchConfiguration {

    @Value("${es_host:localhost}")
    public String esHost;

    @Value("${es.port:9200}")
    public int esPort;

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration config = ClientConfiguration.builder()
                .connectedTo(esHost + ":" + esPort)
                .build();

        return RestClients.create(config).rest();
    }
    @Bean
    public ElasticSearchUtil elasticSearchUtil() throws Exception {
        return new ElasticSearchUtil(esHost, esPort);
    }

}
