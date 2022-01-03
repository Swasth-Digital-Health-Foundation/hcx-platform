package org.swasth.common.dto;

import java.util.Map;

public class SearchRequestDTO extends PagedRequestDTO {
    private Map<String, String> filters;

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

}