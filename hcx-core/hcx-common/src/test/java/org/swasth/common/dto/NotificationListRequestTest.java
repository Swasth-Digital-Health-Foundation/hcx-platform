package org.swasth.common.dto;

import org.junit.Test;
import org.mockito.Mockito;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.swasth.common.utils.Constants.*;


public class NotificationListRequestTest {

    @Test
    public void testNotificationListRequest() {
        NotificationListRequest request = new NotificationListRequest(getRequestBody());
        request.setFilters(Collections.singletonMap(Constants.PRIORITY, 0));
        assertFalse(request.getFilters().isEmpty());
    }

    private Map<String,Object> getRequestBody() {
        Map<String,Object> filters = new HashMap<>();
        filters.put(Constants.CATEGORY, Constants.WORKFLOW);
        return Collections.singletonMap(Constants.FILTERS, filters);
    }

    private Map<String,Object> getFullRequestBody() {
        Map<String,Object> obj = new HashMap<>();
        obj.put(FILTERS,new HashMap<>(){{put(Constants.CATEGORY, Constants.WORKFLOW);}});
        obj.put(LIMIT, 1);
        obj.put(OFFSET,0);
        return obj;
    }

    @Test
    public void testNotificationListRequestAllFields() {
        NotificationListRequest request = new NotificationListRequest(getFullRequestBody());
        request.setOffset(10);
        request.setLimit(20);
        request.setRecipientCode("test-recipient-code");
        request.setFilters(Collections.singletonMap(Constants.PRIORITY, 0));
        assertFalse(request.getFilters().isEmpty());
        assertEquals(20,request.getLimit());
        assertEquals(10,request.getOffset());
        assertEquals("test-recipient-code",request.getRecipientCode());
    }
}
