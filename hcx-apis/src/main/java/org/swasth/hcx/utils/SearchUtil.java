package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.common.utils.Constants;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public final class SearchUtil {

    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final SearchRequestDTO dto) {
    	try {
            final int page = dto.getOffset();
            final int size = dto.getLimit();
            final int from = page <= 0 ? 0 : page * size;

            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .from(from)
                    .size(size)
                    .postFilter(getQueryBuilder(dto));

            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static QueryBuilder getQueryBuilder(final SearchRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        final Map<String, String> fields = dto.getFilters();
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        Optional<String> firstKey = fields.keySet().stream().findFirst();
        if (firstKey.isPresent()) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            for (Entry<String, String> entry : fields.entrySet()) {
                if (dto.getAction().equalsIgnoreCase(Constants.AUDIT_NOTIFICATION_SEARCH) && entry.getKey().equalsIgnoreCase(Constants.HCX_SENDER_CODE)){
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
