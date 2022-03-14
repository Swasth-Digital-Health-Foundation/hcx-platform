package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@ContextConfiguration(classes=SearchUtil.class)
class SearchUtilsTests extends BaseSpec {

    @Autowired
    SearchUtil searchUtil;

    @Test
    void buildSearchRequestTest() {
        SearchRequest result = searchUtil.buildSearchRequest("hcx_audit",new SearchRequestDTO());
        assertNotNull(result);
    }

    @Test
    void getQueryBuilderTest() throws Exception{
        QueryBuilder result = searchUtil.getQueryBuilder(new SearchRequestDTO());
        assertNull(result);
    }

}
