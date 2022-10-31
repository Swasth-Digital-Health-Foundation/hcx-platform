package org.swasth.common.dto;

import org.swasth.common.utils.Constants;

import java.util.Map;

public class SearchRequest extends Request {

    public SearchRequest(Map<String, Object> body, String apiAction) throws Exception {
        super(body, apiAction);
    }

    public Map<String,Object> getSearchRequest(){
        return getHeaderMap(Constants.SEARCH_REQ);
    }

    public Map<String,Object> getSearchResponse(){
        return getHeaderMap(Constants.SEARCH_RESP);
    }

    public Map<String,Object> getSearchFilters(){
       return (Map<String, Object>) getSearchRequest().getOrDefault(Constants.SEARCH_FILTERS,null);
    }

}
