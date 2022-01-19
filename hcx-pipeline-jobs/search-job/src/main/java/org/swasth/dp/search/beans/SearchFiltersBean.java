package org.swasth.dp.search.beans;

import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFiltersBean implements Serializable {
    private List<String> senderCodes;
    private List<String> recipientCodes;
    private List<String> entityTypes;
    private List<String> workFlowIds;
    private List<String> caseIds;
    private List<String> entityStatus;

    public SearchFiltersBean(Map<String,Object> eventMap){
        setSenderCodes((List<String>) eventMap.getOrDefault(Constants.SENDERS, new ArrayList<String>()));
        setRecipientCodes((List<String>) eventMap.getOrDefault(Constants.RECIPIENTS, new ArrayList<String>()));
        setEntityTypes((List<String>) eventMap.get(Constants.ENTITY_TYPES));
        setWorkFlowIds((List<String>) eventMap.getOrDefault(Constants.WORKFLOW_IDS, new ArrayList<String>()));
        setCaseIds((List<String>) eventMap.getOrDefault(Constants.CASE_IDS, new ArrayList<String>()));
        setEntityStatus((List<String>) eventMap.get(Constants.ENTITY_STATUS));
    }

    public List<String> getSenderCodes() {
        return senderCodes;
    }

    public void setSenderCodes(List<String> senderCodes) {
        this.senderCodes = senderCodes;
    }

    public List<String> getRecipientCodes() {
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
}
