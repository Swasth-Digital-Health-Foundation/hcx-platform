package org.swasth.hcx.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.utils.SearchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final RestHighLevelClient client;

    @Autowired
    public AuditService(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Used to search for audit events based on data provided in the {@link AuditSearchRequest} DTO. For more info take a look
     * at DTO javadoc.
     *
     * @param request DTO containing info about what to search for.
     * @return Returns a list of found audit events.
     */
    public List<Map<String, Object>> search(final AuditSearchRequest request, String action) {
        request.setAction(action);
        final SearchRequest searchRequest = SearchUtil.buildSearchRequest(
        		Constants.HEADER_AUDIT,
                request
        );

        return searchInternal(searchRequest);
    }


    private List<Map<String,Object>> searchInternal(final SearchRequest request) {
        if (request == null) {
            logger.error("Failed to build search request");
            return Collections.emptyList();
        }

        try {
            final SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            final SearchHit[] searchHits = response.getHits().getHits();
            final List<Map<String,Object>> audit = new ArrayList<>(searchHits.length);
            for (SearchHit hit : searchHits) {
                audit.add(hit.getSourceAsMap());
            }
            return audit;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
