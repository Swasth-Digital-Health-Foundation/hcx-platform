package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.common.exception.ClientException;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.helpers.EventGenerator;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


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
