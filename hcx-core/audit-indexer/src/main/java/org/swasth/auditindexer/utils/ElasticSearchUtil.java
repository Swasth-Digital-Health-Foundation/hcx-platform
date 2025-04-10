package org.swasth.auditindexer.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchUtil {

    private final String esHost;
    private final int esPort;
    private final ElasticsearchClient esClient;
    private final RestClient restClient;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ElasticSearchUtil(String esHost, int esPort) throws Exception {
        this.esHost = esHost;
        this.esPort = esPort;
        this.restClient = RestClient.builder(new HttpHost(esHost, esPort)).build();
        this.esClient = createClient();
    }

    public ElasticsearchClient createClient() throws Exception {
        try {
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while creating elastic search connection : " + e.getMessage());
        }
    }

    public void addDocumentWithIndex(String document, String indexName, String identifier) throws Exception {
        try {
            Map<String, Object> doc = mapper.readValue(document, Map.class);
            IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                .index(indexName)
                .id(identifier)
                .document(doc)
            );
            esClient.index(indexRequest);
        } catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while adding document to index : " + indexName + " : " + e.getMessage());
        }
    }

    public void addIndex(String settings, String mappings, String indexName, String alias) throws Exception {
        try {
            if (!isIndexExists(indexName)) {
                CreateIndexRequest.Builder createRequestBuilder = new CreateIndexRequest.Builder()
                    .index(indexName);

                if (settings != null && !settings.isEmpty()) {
                    createRequestBuilder.settings(s -> s.withJson(new java.io.ByteArrayInputStream(settings.getBytes())));
                }
                if (mappings != null && !mappings.isEmpty()) {
                    createRequestBuilder.mappings(TypeMapping.of(m -> m.withJson(new java.io.ByteArrayInputStream(mappings.getBytes()))));
                }
                esClient.indices().create(createRequestBuilder.build());
            }
        } catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while creating index : " + indexName + " : " + e.getMessage(), e);
        }
    }

    public boolean isIndexExists(String indexName) {
        try {
            ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(indexName));
            return esClient.indices().exists(existsRequest).value();
        } catch (IOException e) {
            return false;
        }
    }

    public void close() throws Exception {
        if (esClient != null) {
            try {
                esClient._transport().close();
            } catch (IOException e) {
                throw new Exception("ElasticSearchUtil :: Error while closing elastic search connection : " + e.getMessage());
            }
        }
        if (restClient != null) {
            try {
                restClient.close();
            } catch (IOException e) {
                throw new Exception("ElasticSearchUtil :: Error while closing RestClient connection : " + e.getMessage());
            }
        }
    }

    public boolean isHealthy() {
        try {
            HealthResponse response = esClient.cluster().health();
            String status = response.status().jsonValue();
            return "green".equals(status) || "yellow".equals(status);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
