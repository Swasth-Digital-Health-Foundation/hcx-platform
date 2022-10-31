package org.swasth.hcx.controllers.v1;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.SearchRequest;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.HCX_ON_SEARCH;
import static org.swasth.common.utils.Constants.HCX_SEARCH;

@RestController
@RequestMapping(Constants.VERSION_PREFIX)
public class SearchController extends BaseController {

    @Value("${kafka.topic.search}")
    private String topic;

    @Value("${kafka.topic.searchresponse}")
    private String responseTopic;

    @PostMapping(HCX_SEARCH)
    public ResponseEntity<Object> search (@RequestBody Map<String, Object> requestBody) throws Exception {
        SearchRequest request = new SearchRequest(requestBody, HCX_SEARCH);
        Response response = new Response(request);
        try {
            // Validations
            validateRegistryCode(request);
            Map<String,Object> requestMap = request.getSearchRequest();
            validateSearch(requestMap, "Search details cannot be null, empty and should be 'JSON Object'", Constants.SEARCH_REQ_KEYS, "Search details should contain only: ");
            if(requestMap.containsKey(Constants.SEARCH_FILTERS)){
                validateSearchFilters(request);
            }
            eventHandler.processAndSendEvent(topic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandlerWithAudit(request, response, e);
        }
    }

    @PostMapping(HCX_ON_SEARCH)
    public ResponseEntity<Object> onSearch (@RequestBody Map<String, Object> requestBody) throws Exception {
        SearchRequest request = new SearchRequest(requestBody, HCX_ON_SEARCH);
        Response response = new Response(request);
        try {
            // Validations
            validateRegistryCode(request);
            Map<String, Object> responseMap = request.getSearchResponse();
            validateSearch(responseMap, "Search response details cannot be null, empty and should be 'JSON Object'", Constants.SEARCH_RES_KEYS, "Search response details should contain only: ");
            eventHandler.processAndSendEvent(responseTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandlerWithAudit(request, response, e);
        }
    }

    private void validateRegistryCode(SearchRequest request) throws ClientException {
        if(Constants.HCX_REGISTRY_CODE.equals(request.getHcxRecipientCode())){
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, "Search recipient code must be hcx registry code");
        }
    }

    private void validateSearch(Map<String, Object> requestMap, String s, List<String> searchReqKeys, String s2) throws ClientException {
        if (MapUtils.isEmpty(requestMap)) {
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, s);
        } else if (requestMap.keySet().stream().noneMatch(key -> searchReqKeys.contains(key))) {
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, s2 + searchReqKeys);
        }
    }

    private void validateSearchFilters(SearchRequest request) throws ClientException {
        Map<String, Object> searchFiltersMap = request.getSearchFilters();
        if (MapUtils.isNotEmpty(searchFiltersMap)) {
            if (!searchFiltersMap.containsKey(Constants.SEARCH_FILTERS_RECEIVER)
                    || searchFiltersMap.keySet().stream().noneMatch(key -> Constants.SEARCH_FILTER_KEYS.contains(key)))
                throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, "Search Filters should contain only: " + Constants.SEARCH_FILTER_KEYS);
        } else
            throw new ClientException(ErrorCodes.ERR_INVALID_SEARCH, "Search filters cannot be null and should be 'JSON Object'");
    }

}