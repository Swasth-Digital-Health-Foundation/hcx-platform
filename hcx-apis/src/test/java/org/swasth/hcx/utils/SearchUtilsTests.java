package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.helpers.EventGenerator;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;

@ContextConfiguration(classes=SearchUtil.class)
public class SearchUtilsTests extends BaseSpec {

    @Autowired
    SearchUtil searchUtil;

    @Test
    void buildSearchRequestTest() throws Exception{
        SearchRequest result = searchUtil.buildSearchRequest("hcx_audit",new SearchRequestDTO());
    }
}
