package org.swasth.hcx.utils;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import org.springframework.util.CollectionUtils;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.Constants;
import co.elastic.clients.json.JsonData;

import java.util.Map;
import java.util.Map.Entry;

public final class SearchUtil {

    public static SearchRequest buildSearchRequest(final String indexName,
            final AuditSearchRequest request) {
        try {
            final int page = request.getOffset();
            final int size = request.getLimit();
            final int from = page <= 0 ? 0 : page * size;

            SearchRequest searchRequest = SearchRequest.of(s -> s.index(indexName)
                .from(from)
                .size(size)
                .query(getQueryBuilder(request)));

            return searchRequest;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Query getQueryBuilder(final AuditSearchRequest request) {
        Map<String, String> fields = request.getFilters();
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        BoolQuery.Builder queryBuilder = QueryBuilders.bool();
        for (Entry<String, String> entry : fields.entrySet()) {
            if (request.getAction().equalsIgnoreCase(Constants.AUDIT_NOTIFICATION_SEARCH) && entry.getKey().equalsIgnoreCase(Constants.HCX_SENDER_CODE)) {
                queryBuilder.should(QueryBuilders.term(t -> t.field(Constants.HCX_SENDER_CODE).value(entry.getValue())));
                queryBuilder.should(QueryBuilders.term(t -> t.field(Constants.HCX_RECIPIENT_CODE).value(entry.getValue())));
            } else if (!entry.getKey().equalsIgnoreCase(Constants.START_DATETIME) && !entry.getKey().equalsIgnoreCase(Constants.STOP_DATETIME)) {
                queryBuilder.must(QueryBuilders.matchPhrase(t -> t.field(entry.getKey()).query(entry.getValue())));
            } else if (entry.getKey().equalsIgnoreCase(Constants.START_DATETIME)) {
                queryBuilder.must(QueryBuilders.range(r -> 
                    r.untyped(nf -> nf
                        .field(Constants.TIMESTAMP)
                        .gte(JsonData.of(entry.getValue()))))); // Use gte instead of from
            } else if (entry.getKey().equalsIgnoreCase(Constants.STOP_DATETIME)) {
                queryBuilder.must(QueryBuilders.range(r -> 
                    r.untyped(nf -> nf
                        .field(Constants.TIMESTAMP)
                        .lte(JsonData.of(entry.getValue()))))); // Use lte instead of to
            }
        }
        return Query.of(q -> q.bool(queryBuilder.build()));
    }
}
