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
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.utils.SearchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class HeaderAuditService {

    private static final Logger LOG = LoggerFactory.getLogger(HeaderAuditService.class);

    private final RestHighLevelClient client;
    @Autowired
    public HeaderAuditService(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Used to search for audit events based on data provided in the {@link SearchRequestDTO} DTO. For more info take a look
     * at DTO javadoc.
     *
     * @param dto DTO containing info about what to search for.
     * @return Returns a list of found audit events.
     */
    public List<Map<String, Object>> search(final SearchRequestDTO dto) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
        		Constants.HEADER_AUDIT,
                dto
        );

        return searchInternal(request);
    }


    private List<Map<String,Object>> searchInternal(final SearchRequest request) {
        if (request == null) {
            LOG.error("Failed to build search request");
            return Collections.emptyList();
        }

        try {
            final SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            final SearchHit[] searchHits = response.getHits().getHits();
            final List<Map<String,Object>> searchResult = new ArrayList<>(searchHits.length);
            for (SearchHit hit : searchHits) {
                searchResult.add(hit.getSourceAsMap());
            }
            return searchResult;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
