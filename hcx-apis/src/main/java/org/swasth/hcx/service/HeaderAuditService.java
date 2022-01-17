package org.swasth.hcx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.HeaderAudit;
import org.swasth.hcx.utils.*;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.hcx.utils.SearchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class HeaderAuditService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(HeaderAuditService.class);

    private final RestHighLevelClient client;
    @Autowired
    public HeaderAuditService(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * Used to search for vehicles based on data provided in the {@link SearchRequestDTO} DTO. For more info take a look
     * at DTO javadoc.
     *
     * @param dto DTO containing info about what to search for.
     * @return Returns a list of found vehicles.
     */
    public List<HeaderAudit> search(final SearchRequestDTO dto) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
        		Constants.HEADER_AUDIT,
                dto
        );

        return searchInternal(request);
    }


    private List<HeaderAudit> searchInternal(final SearchRequest request) {
        if (request == null) {
            LOG.error("Failed to build search request");
            return Collections.emptyList();
        }

        try {
            final SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            final SearchHit[] searchHits = response.getHits().getHits();
            final List<HeaderAudit> headeraudit = new ArrayList<>(searchHits.length);
            for (SearchHit hit : searchHits) {
                headeraudit.add(
                        MAPPER.readValue(hit.getSourceAsString(), HeaderAudit.class)
                );
            }

            return headeraudit;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
