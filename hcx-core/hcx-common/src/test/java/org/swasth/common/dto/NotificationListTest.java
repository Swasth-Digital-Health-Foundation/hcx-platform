package org.swasth.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class NotificationListTest {

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
}
