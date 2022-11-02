package org.swasth.auditindexer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchUtil {

    private final String esHost;
    private final int esPort;
    private final RestHighLevelClient esClient;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ElasticSearchUtil(String esHost, int esPort) throws Exception {
        this.esHost = esHost;
        this.esPort = esPort;
        this.esClient = createClient();
    }

    public RestHighLevelClient createClient() throws Exception {
        try {
            return new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort)));
        }  catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while creating elastic search connection : " + e.getMessage());
        }
    }

    public void addDocumentWithIndex(String document, String indexName, String identifier) throws Exception {
        try {
            Map<String,Object> doc = mapper.readValue(document, Map.class);
            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.id(identifier);
            esClient.index(indexRequest.source(doc), RequestOptions.DEFAULT);
        }  catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while adding document to index : " + indexName + " : " + e.getMessage());
        }
    }

    public void addIndex(String settings, String mappings, String indexName, String alias) throws Exception {
        try {
            if(!isIndexExists(indexName)) {
                CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
                if (StringUtils.isNotBlank(alias)) createRequest.alias(new Alias(alias));
                if (StringUtils.isNotBlank(settings)) createRequest.settings(Settings.builder().loadFromSource(settings, XContentType.JSON));
                if (StringUtils.isNotBlank(mappings)) createRequest.mapping(mappings, XContentType.JSON);
                esClient.indices().create(createRequest, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while creating index : " + indexName + " : " + e.getMessage());
        }
    }

    public boolean isIndexExists(String indexName){
        try {
            return esClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
    }

    public void close() throws Exception {
        if (null != esClient)
            try {
                esClient.close();
            } catch (IOException e) {
                throw new Exception("ElasticSearchUtil :: Error while closing elastic search connection : " + e.getMessage());
            }
    }

    public boolean isHealthy() {
        try {
            esClient.indices().exists(new GetIndexRequest("test"), RequestOptions.DEFAULT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

