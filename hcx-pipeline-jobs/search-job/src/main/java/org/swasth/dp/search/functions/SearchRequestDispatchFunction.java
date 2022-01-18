package org.swasth.dp.search.functions;

import org.apache.flink.configuration.Configuration;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.search.beans.SearchEvent;
import org.swasth.dp.search.beans.SearchFiltersBean;
import org.swasth.dp.search.beans.SearchResponse;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.BaseUtils;
import org.swasth.dp.search.utils.Constants;
import org.swasth.dp.search.utils.EventUtils;
import org.swasth.dp.search.utils.ResponseStatus;
import scala.collection.immutable.Stream;
import sourcecode.Impls;

import java.util.*;

public class SearchRequestDispatchFunction extends BaseDispatcherFunction {

    private PostgresClient postgresClient;
    private CompositeSearchConfig searchConfig;

    public SearchRequestDispatchFunction(CompositeSearchConfig searchConfig){
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
        //String mid = (String) event.get(Constants.MID);
        //return BaseUtils.decodeBase64String(postgresClient.getPayload(mid),Map.class);
        return null;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }

    @Override
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception {
        System.out.println("Event being processed in SearchRequestDispatchFunction:" + event);
        /**
         *  TODO below logic.
         *  1. Modify the sender(HCX) and recipient(recipient id from search filters) in the payload
         *  2. Modify the x-hcx-request_id with the new generated request id
         *  3. Dispatch request to the recipient by encoding the updated payload
         *          a. Successful Dispatch, Insert child record with status as Open
         *          b. In case of dispatch failure to the recipient, Insert child record with status as Fail
         */

        String action = (String) event.get(Constants.ACTION);
        Map<String,Object> payload = (Map<String,Object>) event.getOrDefault(Constants.PAYLOAD,null);
        //Replace sender and recipient details, new requestId, search filters recipient
        Map<String,Object> protocolHeaders = (Map<String,Object>) payload.getOrDefault(Constants.PROTECTED,null);
        protocolHeaders.put(Constants.SENDER_CODE,searchConfig.getHcxRegistryCode());
        String recipientCode = (String) event.get(Constants.RECIPIENT_CODE);
        protocolHeaders.put(Constants.RECIPIENT_CODE,recipientCode);
        // Generate new x-hcx-request_id for each request
        String requestId = UUID.randomUUID().toString();
        protocolHeaders.put(Constants.REQUEST_ID, requestId);

        String workflowId = (String) protocolHeaders.get(Constants.WORKFLOW_ID);
        Map<String, Object> searchRequest = (Map<String, Object>) protocolHeaders.getOrDefault(Constants.SEARCH_REQUEST, new HashMap<String, Object>());
        Map<String, Object> searchFilters = (Map<String, Object>) searchRequest.get(Constants.FILTERS);

        SearchFiltersBean searchFiltersBean = new SearchFiltersBean(searchFilters);
        searchFiltersBean.getRecipientCodes().clear();
        List<String> recipientList= new ArrayList<>();
        recipientList.add(recipientCode);
        searchFiltersBean.setRecipientCodes(recipientList);
        //update search filters list with this recipient details
        Map<String, Object> payloadSearchRequest = (Map<String, Object>) protocolHeaders.getOrDefault(Constants.SEARCH_REQUEST, new HashMap<String, Object>());
        payloadSearchRequest.put(Constants.FILTERS,searchFiltersBean);

        protocolHeaders.put(Constants.SEARCH_REQUEST,payloadSearchRequest);
        payload.put(Constants.PROTOCOL,protocolHeaders);

        event.remove(Constants.PAYLOAD);
        event.remove(Constants.SEARCH_EVENT);
        event.remove(Constants.RECIPIENT_CODE);

        Map<String,Object> recipientMap = createRecipientContext(event,action);
        //TODO Need to send across proper headers at all places
        recipientMap.put(Constants.HEADERS,protocolHeaders);

        DispatcherResult dispatcherResult = DispatcherUtil.dispatch(recipientMap,EventUtils.encodePayload(payload));
        if(!dispatcherResult.success()){
            postgresClient.insertSearchRecord(workflowId,requestId,searchConfig.getHcxRegistryCode(),recipientCode, ResponseStatus.RETRY.toString(),new SearchResponse());
        }else{
            postgresClient.insertSearchRecord(workflowId,requestId,searchConfig.getHcxRegistryCode(),recipientCode, ResponseStatus.OPEN.toString(),new SearchResponse());
        }

        audit(protocolHeaders, true, context, metrics);

    }
}
