package org.swasth.common.dto;

import org.swasth.common.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class NotificationListRequest {

    private Map<String,Object> filters;

    public NotificationListRequest(Map<String,Object> requestBody) {
        this.filters = (Map<String, Object>) requestBody.getOrDefault(Constants.FILTERS, new HashMap<>());
    }

    public Map<String,Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String,Object> filters) {
        this.filters = filters;
    }
}
