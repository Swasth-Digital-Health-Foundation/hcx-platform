package org.swasth.hcx.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.utils.SearchUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private ElasticsearchClient esClient;

    public List<Map<String, Object>> search(final AuditSearchRequest request, String action, String index) {
        try {
            logger.info("Audit search started: {}", JSONUtils.serialize(request));
            request.setAction(action);

            SearchRequest searchRequest = SearchUtil.buildSearchRequest(index, request);
            return searchInternal(searchRequest);

        } catch (Exception e) {
            logger.error("Error while processing audit search :: message: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> searchInternal(final SearchRequest request) throws IOException {
        SearchResponse<?> rawResponse = esClient.search(request, Map.class);
        SearchResponse<Map<String, Object>> response = (SearchResponse<Map<String, Object>>) rawResponse;
        List<Map<String, Object>> audit = new ArrayList<>(response.hits().hits().size());
        for (Hit<Map<String, Object>> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source != null) {
                audit.add(source);
            }
        }
        logger.info("Audit search completed :: count: {}", audit.size());
        return audit;
    }
}
