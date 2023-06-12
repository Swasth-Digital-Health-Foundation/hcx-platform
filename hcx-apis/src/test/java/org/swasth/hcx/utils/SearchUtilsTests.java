package org.swasth.hcx.utils;

import org.elasticsearch.action.search.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.hcx.service.BaseRegistryService;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.service.ParticipantService;
import org.swasth.hcx.service.UserService;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes={SearchUtil.class,NotificationService.class, EventHandler.class, ParticipantService.class , UserService.class, BaseRegistryService.class})
class SearchUtilsTests extends BaseSpec {

    @Test
    void buildSearchRequestTest() {
        SearchRequest result = SearchUtil.buildSearchRequest("hcx_audit",new AuditSearchRequest());
        assertNotNull(result);
    }

    @Test
    void buildSearchRequestSuccessTest() {
        AuditSearchRequest searchRequest = new AuditSearchRequest();
        searchRequest.setFilters(new HashMap<String, String>() {{
            put("status","submitted");
        }});
        searchRequest.setAction(Constants.AUDIT_SEARCH);
        SearchRequest result = SearchUtil.buildSearchRequest("hcx_audit",searchRequest);
        assertEquals("hcx_audit", result.indices()[0]);
    }

    @Test
    void buildSearchRequestErrorTest() {
        SearchRequest result = SearchUtil.buildSearchRequest("hcx_audit",null);
        assertNull(result);
    }

}
