package org.swasth.hcx.config;



import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.swasth.hcx.repository")
//@ComponentScan(basePackages = {"org.swasth.hcx"})
public class ElasticSearchConfiguration extends AbstractElasticsearchConfiguration {

    @Value("${es.host}")
    public String esHost;

    @Value("${es.port}")
    public String esPort;

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration config = ClientConfiguration.builder()
                .connectedTo(esHost + ":" + esPort)
                .build();

        return RestClients.create(config).rest();
    }
}
