package org.swasth.dp.search.functions;

import org.apache.flink.configuration.Configuration;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.search.beans.CompositeSearch;
import org.swasth.dp.search.beans.SearchEvent;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.*;

import java.util.HashMap;
import java.util.Map;

public class SearchResponseDispatchFunction extends BaseDispatcherFunction {
    private PostgresClient postgresClient;
    private CompositeSearchConfig searchConfig;

    public SearchResponseDispatchFunction(CompositeSearchConfig searchConfig){
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
        String mid = (String) event.get(Constants.MID);
        return EventUtils.parsePayload(postgresClient.getPayload(mid));
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }

    @Override
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception {
        System.out.println("Event being processed in SearchResponseDispatchFunction:" + event);
        SearchEvent searchEvent = new SearchEvent(event,searchConfig);
        /**
         *  TODO below logic.
         *  1. Fetch the baseRecord based on workflowId,
         *          if status is complete, do nothing
         *       else proceed with next step
         *  2. Modify the sender(HCX) and recipient(original sender from base record) in the payload
         *  3. Modify the x-hcx-request_id with the requestId from the base record
         *  4. Modify the x-hcx-status as response.partial
         *  4. Dispatch request to the recipient
         *  5.      Success: Update child record, with status as Close and response_data with the search response from the event
         *                   Update base record with status as PARTIAL
         *          FAIL: Update the child record, with status as RETRY and response_data with the search response from the event
         *  6. Check whether all the child record status were in CLOSE/FAIL, then mark it as last recipient response received
         *  7. If it is the last recipient response, dispatch request to SearchCompletionDispatchFunction
         */
        // In the coming event: RequestId - recipient requestId, sender is recipientCode, recipient is HCX
        CompositeSearch baseRecord = postgresClient.getBaseRecord(searchEvent.getWorkFlowId());
        if(!ResponseStatus.COMPLETE.equals(baseRecord.getResponseStatus())){
            Map<String, Object> payload = getPayload(event);
            Map<String, Object> protocolHeaders = (Map<String, Object>) payload.get(Constants.PROTOCOL);
            protocolHeaders.put(Constants.SENDER_CODE,searchConfig.getHcxRegistryCode());
            protocolHeaders.put(Constants.RECIPIENT_CODE,baseRecord.getSenderCode());
            protocolHeaders.put(Constants.REQUEST_ID, baseRecord.getRequestId());
            protocolHeaders.put(Constants.HCX_STATUS, Constants.RESPONSE_PARTIAL);

            DispatcherResult dispatcherResult = dispatchRecipient(baseRecord.getSenderCode(),searchEvent.getAction(),payload);
            System.out.println("Dispatching request to recipient "+baseRecord.getSenderCode() + " with status code:" +dispatcherResult.statusCode());
            if(dispatcherResult.success()){
                postgresClient.updateSearchRecord(searchEvent.getWorkFlowId(),searchEvent.getRequestId(),ResponseStatus.CLOSE.toString(),searchEvent.getSearchResponse());
            }else{
                postgresClient.updateSearchRecord(searchEvent.getWorkFlowId(),searchEvent.getRequestId(),ResponseStatus.RETRY.toString(),searchEvent.getSearchResponse());
            }

            //TODO check for the last response from recipients and send across the event to completionProcessFunction
            if (!postgresClient.hasPendingResponses(searchEvent.getWorkFlowId())) {
                //Send across payload instead of fetching it from database
                event.put(Constants.PAYLOAD,payload);
                event.put(Constants.BASE_RECORD,baseRecord);
                event.put(Constants.ACTION,searchEvent.getAction());
                context.output(searchConfig.searchCompleteResponseOutputTag, event);
            }

        }else{
            //TODO Got response from recipient post expiry time, handle it during 2 Phase
        }

        audit(event, true, context, metrics);

    }

}
