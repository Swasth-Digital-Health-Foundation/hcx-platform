package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.Constants;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public final class SearchUtil {

    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final AuditSearchRequest request) {
    	try {
            final int page = request.getOffset();
            final int size = request.getLimit();
            final int from = page <= 0 ? 0 : page * size;

            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .from(from)
                    .size(size)
                    .postFilter(getQueryBuilder(request));

            final SearchRequest searchRequest = new SearchRequest(indexName);
            searchRequest.source(builder);

            return searchRequest;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static QueryBuilder getQueryBuilder(final AuditSearchRequest request) {
        Map<String, String> fields = request.getFilters();
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        Optional<String> firstKey = fields.keySet().stream().findFirst();
        if (firstKey.isPresent()) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            for (Entry<String, String> entry : fields.entrySet()) {
                if (request.getAction().equalsIgnoreCase(Constants.AUDIT_NOTIFICATION_SEARCH) && entry.getKey().equalsIgnoreCase(Constants.HCX_SENDER_CODE)){
                    queryBuilder = queryBuilder.should(QueryBuilders.termQuery(Constants.HCX_SENDER_CODE, entry.getValue()));
                    queryBuilder = queryBuilder.should(QueryBuilders.termQuery(Constants.HCX_RECIPIENT_CODE, entry.getValue()));
                } else if(!entry.getKey().equalsIgnoreCase(Constants.START_DATETIME) & !entry.getKey().equalsIgnoreCase(Constants.STOP_DATETIME)) {
                	queryBuilder = queryBuilder.must(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(Constants.START_DATETIME)) {
                	queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery(Constants.TIMESTAMP).gte(entry.getValue()));
                } else if (entry.getKey().equalsIgnoreCase(Constants.STOP_DATETIME)) {
                	queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery(Constants.TIMESTAMP).lte(entry.getValue()));
                }
            }
            return queryBuilder;
        }
        return null;
    }
}
