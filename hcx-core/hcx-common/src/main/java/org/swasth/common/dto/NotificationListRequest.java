package org.swasth.common.dto;

import org.swasth.common.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class NotificationListRequest {

    private Map<String,Object> filters;
    private String recipientCode;
    private int limit;
    private int offset;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getRecipientCode() {
        return recipientCode;
    }

    public void setRecipientCode(String recipientCode) {
        this.recipientCode = recipientCode;
    }

    public NotificationListRequest(Map<String,Object> requestBody) {
        this.filters = (Map<String, Object>) requestBody.getOrDefault(Constants.FILTERS, new HashMap<>());
        this.recipientCode = (String) requestBody.get(Constants.RECIPIENT_CODE);
        this.limit = (int) requestBody.getOrDefault(Constants.LIMIT,0);
        this.offset = (int) requestBody.getOrDefault(Constants.OFFSET,0);
    }

    public Map<String,Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String,Object> filters) {
        this.filters = filters;
    }
}
