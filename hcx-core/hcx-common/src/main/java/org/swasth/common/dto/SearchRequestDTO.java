package org.swasth.common.dto;

import java.util.HashMap;

public class SearchRequestDTO extends PagedRequestDTO {
    private HashMap<String, String> filters;

    public HashMap<String, String> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }

}