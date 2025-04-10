package org.swasth.hcx.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.swasth.auditindexer.utils.ElasticSearchUtil;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.swasth.hcx.repository")
public class ElasticSearchConfiguration {

    @Value("${es.host:localhost}")
    public String esHost;

    @Value("${es.port:9200}")
    public int esPort;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(new HttpHost(esHost, esPort)).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticSearchUtil elasticSearchUtil() throws Exception {
        return new ElasticSearchUtil(esHost, esPort);
    }
}

