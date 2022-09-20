package org.swasth.common.dto;

import org.junit.Test;
import org.swasth.common.utils.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuditSearchRequestTest {

    @Test
    public void testCreateAuditSearchRequestWithEmptyFilters(){
        AuditSearchRequest request = new AuditSearchRequest(Collections.EMPTY_MAP);
        assertTrue(request.getFilters().isEmpty());
    }

    @Test
    public void testCreateAuditSearchRequestWithValidFilters(){
        Map<String,String> filters = new HashMap<>();
        filters.put(Constants.HCX_SENDER_CODE, "provider@01");
        AuditSearchRequest request = new AuditSearchRequest();
        request.setFilters(filters);
        request.setAction(Constants.AUDIT_SEARCH);
        request.setLimit(0);
        assertEquals(Constants.AUDIT_SEARCH, request.getAction());
        assertEquals("provider@01", request.getFilters().get(Constants.HCX_SENDER_CODE));
    }

}
