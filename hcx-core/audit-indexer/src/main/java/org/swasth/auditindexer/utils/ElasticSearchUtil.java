package org.swasth.auditindexer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

public class ElasticSearchUtil {

    private final String esHost;
    private final int esPort;
    private final RestClient esClient;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ElasticSearchUtil(String esHost, int esPort) {
        this.esHost = esHost;
        this.esPort = esPort;
        this.esClient = createClient();
    }

//    public RestHighLevelClient createClient() throws Exception {
//        try {
//            return new RestHighLevelClient(RestClient.builder(new HttpHost(esHost, esPort)));
//        }  catch (Exception e) {
//            throw new Exception("ElasticSearchUtil :: Error while creating elastic search connection : " + e.getMessage());
//        }
//    }

    public RestClient createClient() {
        return RestClient
                .builder(new HttpHost(esHost, esPort)).build();
    }

//    public void addDocumentWithIndex(String document, String indexName, String identifier) throws Exception {
//        try {
//            Map<String,Object> doc = mapper.readValue(document, Map.class);
//            IndexRequest indexRequest = new IndexRequest(indexName);
//            indexRequest.id(identifier);
//            esClient.index(indexRequest.source(doc), RequestOptions.DEFAULT);
//        }  catch (Exception e) {
//            throw new Exception("ElasticSearchUtil :: Error while adding document to index : " + indexName + " : " + e.getMessage());
//        }
//    }

    public void addDocumentWithIndex(String document, String indexName, String identifier) throws Exception {
        try {
            String endpoint = "/" + indexName + "/_doc/" + identifier;
            Request request = new Request("PUT", endpoint);
            request.setJsonEntity(document);
            Response response = esClient.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 201) {
                throw new Exception("ElasticSearchUtil :: Error while adding document to index : " + indexName + " : Unexpected status code: " + statusCode);
            }
        } catch (Exception e) {
            throw new Exception("ElasticSearchUtil :: Error while adding document to index : " + indexName + " : " + e.getMessage());
        }
    }

//    public void addIndex(String settings, String mappings, String indexName, String alias) throws Exception {
//        try {
//            if(!isIndexExists(indexName)) {
//                CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
//                if (StringUtils.isNotBlank(alias)) createRequest.alias(new Alias(alias));
//                if (StringUtils.isNotBlank(settings)) createRequest.settings(Settings.builder().loadFromSource(settings, XContentType.JSON));
//                if (StringUtils.isNotBlank(mappings)) createRequest.mapping(mappings, XContentType.JSON);
//                esClient.indices().create(createRequest, RequestOptions.DEFAULT);
//            }
//        } catch (Exception e) {
//            throw new Exception("ElasticSearchUtil :: Error while creating index : " + indexName + " : " + e.getMessage());
//        }
//    }

    public void addIndex(String settings, String mappings, String indexName, String alias) throws Exception {
        try {
            if (!isIndexExists(indexName)) {
                RestClient restClient = RestClient.builder(
                        new HttpHost(esHost, esPort)).build();
                Request createRequest = new Request("PUT", "/" + indexName);
                if (StringUtils.isNotBlank(alias)) {
                    String aliasJson = "{\"actions\":[{\"add\":{\"index\":\"" + indexName + "\",\"alias\":\"" + alias + "\"}}]}";
                    Request aliasRequest = new Request("POST", "/_aliases");
                    aliasRequest.setEntity(new NStringEntity(aliasJson, ContentType.APPLICATION_JSON));
                    restClient.performRequest(aliasRequest);
                }
                if (StringUtils.isNotBlank(settings)) {
                    createRequest.setJsonEntity(settings);
                }
                if (StringUtils.isNotBlank(mappings)) {
                    createRequest.setJsonEntity(mappings);
                }
                Response createResponse = restClient.performRequest(createRequest);
                System.out.println(createResponse.getHost() + "----- Host");
                restClient.close();
            }
        } catch (IOException e) {
            throw new Exception("ElasticSearchUtil :: Error while creating index : " + indexName + " : " + e.getMessage());
        }
    }

//    public boolean isIndexExists(String indexName){
//        try {
//            return esClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            return false;
//        }
//    }

    private boolean isIndexExists(String indexName) throws IOException {
        RestClient restClient = RestClient.builder(
                new HttpHost(esHost, esPort)).build();
        Request request = new Request("HEAD", "/" + indexName);
        Response response = restClient.performRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        restClient.close();
        return statusCode == 200;
    }

    public void close() throws Exception {
        if (null != esClient)
            try {
                esClient.close();
            } catch (IOException e) {
                throw new Exception("ElasticSearchUtil :: Error while closing elastic search connection : " + e.getMessage());
            }
    }

//     public boolean isHealthy(){
//        try {
//            esClient.indices().exists(new GetIndexRequest("test"), RequestOptions.DEFAULT);
//            return true;
//        } catch (Exception e){
//            return false;
//        }
//    }

    public boolean isHealthy() {
        try (RestClient restClient = RestClient.builder(
                new HttpHost(esHost, esPort)).build()) {
            Request request = new Request("HEAD", "/test");
            Response response = restClient.performRequest(request);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
