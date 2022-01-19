package org.swasth.dp.search.functions;

import org.apache.flink.configuration.Configuration;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.search.beans.CompositeSearch;
import org.swasth.dp.search.beans.ConsolidatedResponse;
import org.swasth.dp.search.beans.SearchResponse;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.BaseUtils;
import org.swasth.dp.search.utils.Constants;
import org.swasth.dp.search.utils.EventUtils;
import org.swasth.dp.search.utils.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCompletionDispatchFunction extends BaseDispatcherFunction {

    protected PostgresClient postgresClient;
    protected CompositeSearchConfig searchConfig;

    public SearchCompletionDispatchFunction(CompositeSearchConfig searchConfig){
        super(searchConfig);
        this.searchConfig = searchConfig;
    }

    @Override
    public void open(Configuration parameters) {
        super.open(parameters);
        this.postgresClient = new PostgresClient(searchConfig);
    }

    @Override
    public void close() throws Exception {
        this.postgresClient.getPostgresConnect().closeConnection();
        super.close();
    }

    @Override
    public void audit(Map<String, Object> event, boolean status, Context context, Metrics metrics) throws Exception {
        context.output(searchConfig.auditOutputTag(), BaseUtils.serialize(event));
    }

    @Override
    public Map<String, Object> getPayload(Map<String, Object> event) throws Exception {
        return null;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }

    @Override
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception {
        System.out.println("Event being processed in SearchCompletionDispatchFunction:" + event);
        String action = (String) event.get(Constants.ACTION);
        CompositeSearch baseRecord = (CompositeSearch) event.get(Constants.BASE_RECORD);
        Map<String, Object> payload = (Map<String, Object>)event.get(Constants.PAYLOAD);
        Map<String, Object> protocolHeaders = (Map<String, Object>) payload.get(Constants.PROTOCOL);
        protocolHeaders.put(Constants.HCX_STATUS, Constants.RESPONSE_COMPLETE);

        String workFlowId = (String) protocolHeaders.get(Constants.WORKFLOW_ID);
        List<CompositeSearch> searchList = postgresClient.fetchAllSearchRecords(workFlowId);

        if(searchList !=null && !searchList.isEmpty())
            dispatchConsolidatedResponse(action, baseRecord, payload, protocolHeaders, searchList);
        /**
         *  TODO below logic.
         *  1. Modify the sender(HCX) and recipient(original sender from base record) in the payload
         *  2. Modify the x-hcx-request_id with the requestId from the base record
         *  3. Modify the x-hcx-status as response.complete
         *  3. Fetch all the child record details based on the workflowId
         *  4. Update the consolidated search response from all the recipients
         *  5. Dispatch request to the recipient(original sender) by encoding the updated payload
         *          a. Successful Dispatch, update base record with status as CLOSE
         *          b. In case of dispatch failure to the recipient, update base record status as RETRY
         */
        audit(protocolHeaders, true, context, metrics);

    }

    private void dispatchConsolidatedResponse(String action, CompositeSearch baseRecord, Map<String, Object> payload, Map<String, Object> protocolHeaders, List<CompositeSearch> searchList) throws Exception {
        int entityTotalCounts=0, resp_index = 0;
        Map<String,Object> entityResultMap = new HashMap<>();
        for (CompositeSearch searchRecord: searchList) {
            if(!ResponseStatus.CLOSE.equals(searchRecord.getResponseStatus())){
                resp_index++;
            }
            SearchResponse searchResponse = searchRecord.getSearchResponse();
            updateEntityCounts(entityResultMap, searchResponse);
        }

        //Create final completion object
        ConsolidatedResponse consolidatedResponse = new ConsolidatedResponse();
        consolidatedResponse.setTotal_responses(searchList.size());
        //TODO need to set expiry time based on the request time
        consolidatedResponse.setResponse_index(resp_index);
        consolidatedResponse.setSender_code(baseRecord.getSenderCode());
        SearchResponse responseSummary = getFinalSearchResponse(entityTotalCounts, entityResultMap);
        consolidatedResponse.setSummary(responseSummary);

        protocolHeaders.put(Constants.SEARCH_RESPONSE, consolidatedResponse);

        DispatcherResult dispatcherResult = dispatchRecipient(baseRecord.getSenderCode(), action, payload);
        //TODO Optimize
        //Map<String,Object> responseMap = BaseUtils.deserialize(responseSummary.toString(),Map.class);
        Map<String,Object> responseMap = BaseUtils.deserialize(BaseUtils.serialize(responseSummary),Map.class);
        if(dispatcherResult.success()){
            postgresClient.updateSearchRecord(baseRecord.getWorkFlowId(), baseRecord.getRequestId(),ResponseStatus.CLOSE.toString(),responseMap);
        }else{
            postgresClient.updateSearchRecord(baseRecord.getWorkFlowId(), baseRecord.getRequestId(),ResponseStatus.FAIL.toString(),responseMap);
        }
    }

    private void updateEntityCounts(Map<String, Object> entityResultMap, SearchResponse searchResponse) {
        Map<String,Object> entityMap = searchResponse.getEntity_counts();
        for (String key : entityMap.keySet())
        {
            int entityCount = (int) entityMap.get(key);
            if(entityResultMap.containsKey(key)){
                int oldCounts = (int) entityResultMap.get(key);
                oldCounts += entityCount;
                entityResultMap.put(key,oldCounts);
            }else{
                entityResultMap.put(key,entityCount);
            }
        }
    }

    private SearchResponse getFinalSearchResponse(int entityTotalCounts, Map<String, Object> entityResultMap) {
        SearchResponse responseSummary = new SearchResponse();
        responseSummary.setEntity_counts(entityResultMap);
        for(String key : responseSummary.getEntity_counts().keySet()){
            entityTotalCounts += (int) responseSummary.getEntity_counts().get(key);
        }
        responseSummary.setCount(entityTotalCounts);
        return responseSummary;
    }


}
