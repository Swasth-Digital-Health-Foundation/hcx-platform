package org.swasth.hcx.utils;

import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.service.NotificationService;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


@ContextConfiguration(classes={SearchUtil.class,NotificationService.class})
class SearchUtilsTests extends BaseSpec {

    @Test
    void buildSearchRequestTest() {
        SearchRequest result = SearchUtil.buildSearchRequest("hcx_audit",new SearchRequestDTO());
        assertNotNull(result);
    }

    @Test
    void buildSearchRequestSuccessTest() {
        SearchRequestDTO searchRequest = new SearchRequestDTO();
        searchRequest.setFilters(new HashMap<String, String>() {{
            put("status","submitted");
        }});
        SearchRequest result = SearchUtil.buildSearchRequest("hcx_audit",searchRequest);
        assertEquals("hcx_audit", result.indices()[0]);
    }

    @Test
    void buildSearchRequestErrorTest() {
        SearchRequest result = SearchUtil.buildSearchRequest("hcx_audit",null);
        assertNull(result);
    }

}
