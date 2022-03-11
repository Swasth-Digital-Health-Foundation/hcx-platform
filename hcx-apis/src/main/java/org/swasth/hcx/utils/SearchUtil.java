package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.swasth.common.dto.SearchRequestDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public final class SearchUtil {

    private SearchUtil() {}

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



    private static QueryBuilder getQueryBuilder(final SearchRequestDTO dto) throws ParseException {
        if (dto == null) {
            return null;
        }
        final Map<String, String> fields = dto.getFilters();
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        Optional<String> firstkey = fields.keySet().stream().findFirst();
        if (firstkey.isPresent()) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            for (Entry<String, String> entry : fields.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                if(entry.getKey() != "start_datetime" & entry.getKey() != "stop_datetime") {
                	queryBuilder = queryBuilder.must(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));	
                } else if (entry.getKey() == "start_datetime") {
                    System.out.println("here is the start date");
                	queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(entry.getValue()));
                } else if (entry.getKey() == "stop_datetime") {
                	System.out.println("here is the stop date");
                	queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery("timestamp").lte(entry.getValue()));
                }    
            }
            return queryBuilder;
        }
        return null;
    }
}
