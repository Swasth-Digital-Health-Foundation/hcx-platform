package org.swasth.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.swasth.common.utils.Constants.COVERAGE_ELIGIBILITY_CHECK;


public class NotificationListTest {

    @Test
    public void testNotificationListRequest() throws ClientException {
        NotificationListRequest request = new NotificationListRequest(getRequestBody());
        request.setFilters(Collections.singletonMap(Constants.PRIORITY, 0));
        assertFalse(request.getFilters().isEmpty());
    }

    @Test(expected = ClientException.class)
    public void testMissingNotificationFilters() throws Exception {
        new NotificationListRequest(new HashMap<>());
    }

    private Map<String,Object> getRequestBody() {
        Map<String,Object> filters = new HashMap<>();
        filters.put(Constants.CATEGORY, Constants.WORKFLOW);
        return Collections.singletonMap(Constants.FILTERS, filters);
    }
}
