package org.swasth.common.dto;

import org.apache.commons.collections.MapUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;

import java.util.List;
import java.util.Map;

public class SearchRequest extends Request {

    public SearchRequest(Map<String, Object> body) throws Exception {
        super(body);
    }

    private Map<String,Object> getSearchRequest(){
        return getHeaderMap(Constants.SEARCH_REQ);
    }

    private Map<String,Object> getSearchResponse(){
        return getHeaderMap(Constants.SEARCH_RESP);
    }

    private Map<String,Object> getSearchFilters(){
       return (Map<String, Object>) getSearchRequest().getOrDefault(Constants.SEARCH_FILTERS,null);
    }

    @Override
    public void validate(List<HeaderAudit> auditResponse, String apiAction) throws ClientException {
        super.validate(auditResponse, apiAction);

        if(Constants.HCX_REGISTRY_CODE.equals(getRecipientCode())){
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, "Search recipient code must be hcx registry code");
        }

        if (hcxHeaders.containsKey(Constants.SEARCH_REQ)) {
            Map<String,Object> requestMap = getSearchRequest();
            validateSearch(requestMap, "Search details cannot be null, empty and should be 'JSON Object'", Constants.SEARCH_REQ_KEYS, "Search details should contain only: ");
            if(requestMap.containsKey(Constants.SEARCH_FILTERS)){
                validateSearchFilters();
            }
        }

        if (hcxHeaders.containsKey(Constants.SEARCH_RESP)) {
            Map<String,Object> responseMap = getSearchResponse();
            validateSearch(responseMap, "Search response details cannot be null, empty and should be 'JSON Object'", Constants.SEARCH_RES_KEYS, "Search response details should contain only: ");
        }
    }

    private void validateSearch(Map<String, Object> requestMap, String s, List<String> searchReqKeys, String s2) throws ClientException {
        if (MapUtils.isEmpty(requestMap)) {
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, s);
        } else if (requestMap.keySet().stream().noneMatch(key -> searchReqKeys.contains(key))) {
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, s2 + searchReqKeys);
        }
    }

    //TODO We can remove this check for phase 2
    private void validateSearchFilters() throws ClientException {
        Map<String, Object> searchFiltersMap = getSearchFilters();
        if (MapUtils.isNotEmpty(searchFiltersMap)) {
            if (!searchFiltersMap.containsKey(Constants.SEARCH_FILTERS_RECEIVER)
                    || searchFiltersMap.keySet().stream().noneMatch(key -> Constants.SEARCH_FILTER_KEYS.contains(key)))
                throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, "Search Filters should contain only: " + Constants.SEARCH_FILTER_KEYS);
        } else
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, "Search filters cannot be null and should be 'JSON Object'");
    }
}
