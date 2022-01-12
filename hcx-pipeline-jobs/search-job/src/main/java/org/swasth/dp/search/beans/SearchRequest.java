package org.swasth.dp.search.beans;

import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchRequest implements Serializable {
    private Map<String,Object> eventMap;
    private CompositeSearchConfig searchConfig;
    private List<String> senderCodes;
    private List<String> recipientCodes;
    private List<String> entityTypes;
    private List<String> workFlowIds;
    private List<String> caseIds;
    private List<String> entityStatus;
    private int timePeriod;
    private Map<String,Object> headers;
    private Map<String,Object> protocolHeaders;
    private Map<String,Object> searchRequest;
    private Map<String,Object> searchResponse;
    private String senderCode;
    private String recipientCode;
    private String mid;
    private String action;
    private String workFlowId;
    private String requestId;


    public SearchRequest(Map<String,Object> eventMap, CompositeSearchConfig searchConfig){
        this.eventMap = eventMap;
        this.searchConfig = searchConfig;
        populateSearchParams();
    }

    private void populateSearchParams() {
        setHeaders((Map<String, Object>) eventMap.get(Constants.HEADERS));
        setMid((String) eventMap.get(Constants.MID));
        setAction((String) eventMap.get(Constants.ACTION));
        setProtocolHeaders((Map<String, Object>) headers.get(Constants.PROTOCOL));
        setSenderCode((String) getProtocolHeaders().get(Constants.SENDER_CODE));
        setRecipientCode((String) getProtocolHeaders().get(Constants.RECIPIENT_CODE));
        setWorkFlowId((String) getProtocolHeaders().get(Constants.WORKFLOW_ID));
        setRequestId((String) getProtocolHeaders().get(Constants.REQUEST_ID));
        setSearchRequest((Map<String, Object>) getProtocolHeaders().getOrDefault(Constants.SEARCH_REQUEST, new HashMap<String,Object>()));
        setTimePeriod ((Integer)getSearchRequest().getOrDefault(Constants.TIME_PERIOD, searchConfig.timePeriod));
        Map<String, Object> searchFilters = (Map<String, Object>) getSearchRequest().get(Constants.FILTERS);
        setSenderCodes((List<String>) searchFilters.getOrDefault(Constants.SENDERS, new ArrayList<String>()));
        setRecipientCodes((List<String>) searchFilters.getOrDefault(Constants.RECIPIENTS, new ArrayList<String>()));
        setEntityTypes((List<String>) searchFilters.getOrDefault(Constants.ENTITY_TYPES, searchConfig.entityTypes));
        setWorkFlowIds((List<String>) searchFilters.getOrDefault(Constants.WORKFLOW_IDS, new ArrayList<String>()));
        setCaseIds((List<String>) searchFilters.getOrDefault(Constants.CASE_IDS, new ArrayList<String>()));
        setEntityStatus((List<String>) searchFilters.getOrDefault(Constants.ENTITY_STATUS, searchConfig.entityTypes));
        setSearchResponse((Map<String, Object>) getProtocolHeaders().getOrDefault(Constants.SEARCH_RESPONSE, new HashMap<String,Object>()));
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getWorkFlowId() {
        return workFlowId;
    }

    public void setWorkFlowId(String workFlowId) {
        this.workFlowId = workFlowId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSenderCode() {
        return senderCode;
    }

    public void setSenderCode(String senderCode) {
        this.senderCode = senderCode;
    }

    public String getRecipientCode() {
        return recipientCode;
    }

    public void setRecipientCode(String recipientCode) {
        this.recipientCode = recipientCode;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getProtocolHeaders() {
        return protocolHeaders;
    }

    public void setProtocolHeaders(Map<String, Object> protocolHeaders) {
        this.protocolHeaders = protocolHeaders;
    }

    public Map<String, Object> getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(Map<String, Object> searchRequest) {
        this.searchRequest = searchRequest;
    }

    public Map<String, Object> getSearchResponse() {
        return searchResponse;
    }

    public void setSearchResponse(Map<String, Object> searchResponse) {
        this.searchResponse = searchResponse;
    }

    public List<String> getSenderCodes() {
        return senderCodes;
    }

    public void setSenderCodes(List<String> senderCodes) {
        this.senderCodes = senderCodes;
    }

    public List<String> getRecipientCodes() {
        //TODO Need to fetch recipient details from auditLog if we do not get any recipients in the search filters
        return recipientCodes;
    }

    public void setRecipientCodes(List<String> recipientCodes) {
        this.recipientCodes = recipientCodes;
    }

    public List<String> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public List<String> getWorkFlowIds() {
        return workFlowIds;
    }

    public void setWorkFlowIds(List<String> workFlowIds) {
        this.workFlowIds = workFlowIds;
    }

    public List<String> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(List<String> caseIds) {
        this.caseIds = caseIds;
    }

    public List<String> getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(List<String> entityStatus) {
        this.entityStatus = entityStatus;
    }

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

}
