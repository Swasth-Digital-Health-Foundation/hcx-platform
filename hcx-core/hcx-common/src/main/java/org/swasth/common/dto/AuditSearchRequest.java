package org.swasth.common.dto;

import java.util.Map;

public class AuditSearchRequest extends PagedRequest {

    private String action;

    private Map<String, String> filters;

    public AuditSearchRequest() {}

    public AuditSearchRequest(Map<String, String> filters){
        this.filters = filters;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public void setAction(String action) { this.action = action; }

    public String getAction() { return action; }

}