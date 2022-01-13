package org.swasth.dp.search.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.search.beans.CompositeSearch;
import org.swasth.dp.search.beans.SearchEvent;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.*;

import java.util.Map;

public class ResponseDispatchProcessFunction extends BaseDispatcherFunction {

    private PostgresClient postgresClient;
    private CompositeSearchConfig searchConfig;

    public ResponseDispatchProcessFunction(CompositeSearchConfig searchConfig){
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
        super.close();
        this.postgresClient.getPostgresConnect().closeConnection();
    }


    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }

    //TODO unused as we have to change the framework signature
    @Override
    public Map<String, Object> getPayload(Map<String, Object> event) {
        String mid = (String) event.get(Constants.MID);
        Map<String, Object> payLoad = null;
        try {
            payLoad = BaseUtils.decodeBase64String(postgresClient.getPayload(mid), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payLoad;
    }

    @Override
    public void audit(Map<String, Object> event, boolean status, ProcessFunction.Context context, Metrics metrics) {
        String auditData = null;
        try {
            Map<String, Object> audit = createAuditRecord(event, "AUDIT");
            auditData = JSONUtil.serialize(audit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.output(searchConfig.auditOutputTag(), auditData);
    }

    @Override
    public void processElement(Map<String, Object> searchEvent, ProcessFunction.Context context, Metrics metrics) {
        // fetch the payload from postgres based on mid and replace the protocol headers with the new sender and recipient details
        CompositeSearch baseRecord = null;
        //Populate the bean with the data from the incoming event
        SearchEvent searchRequest = new SearchEvent(searchEvent,searchConfig);
        try {
            //Response from the recipient/payor
            Map<String,Object> searchResponse = searchRequest.getSearchResponse();
            // Fetch the baseRecord record from the Postgres based on workflowId where recipient_code is null and status is not close
            baseRecord = postgresClient.getBaseRecord(searchRequest.getWorkFlowId());
            // if status is not complete then proceed further
            if(!baseRecord.getResponseStatus().equals(ResponseStatus.COMPLETE)) {
                EventUtils.replaceSenderAndRecipientCodes(searchRequest, baseRecord.getSenderCode(), false);
                //Set original requestId from the sender, instead of requestId from this response event
                searchRequest.setRequestId(baseRecord.getRequestId());
                DispatcherResult dispatcherResult = dispatchRecipient(searchRequest,baseRecord.getSenderCode(), baseRecord.getRequestId(), false);
                if(dispatcherResult != null && dispatcherResult.success()){
                    // Update the child search record status as close and update the search response from the recipient
                    postgresClient.updateChildRecord(searchRequest.getWorkFlowId(),searchRequest.getRequestId(),ResponseStatus.CLOSE.toString(),searchRequest.getSearchResponse());
                    // Update the base record with status as partial
                    postgresClient.updateBaseRecord(searchRequest.getWorkFlowId(),baseRecord.getRequestId(),ResponseStatus.PARTIAL.toString());
                }else{
                    failureProcess(searchEvent, context, searchRequest);
                }
            }else{
                // if status is complete then we got response from the recipient after the expiry time,
                //TODO check what needs to be done, for now do nothing; for phase 2.
            }

        } catch (Exception e) {
            //Add the event to the retry topic and update the counts and status as RETRY for child record
            failureProcess(searchEvent, context, searchRequest);
        }

        //TODO If it is the last recipient response, send across the consolidated response data from all the recipients back to the original sender
        try {

            DispatcherResult dispatcherResult = dispatchRecipient(searchRequest,baseRecord.getSenderCode(), baseRecord.getRequestId(), true);
        } catch (PipelineException e) {
            e.printStackTrace();
        }

        //TODO update the event with these values baseReqId,hcx-status,sender, recipient Codes
        audit(searchEvent,true,context,metrics);
    }

    private void failureProcess(Map<String, Object> searchEvent, Context context, SearchEvent searchRequest) {
        //Add the event to the retry topic and update the counts and status as RETRY for child record
        try {
            postgresClient.updateChildRecord(searchRequest.getWorkFlowId(), searchRequest.getRequestId(), ResponseStatus.RETRY.toString(), searchRequest.getSearchResponse());
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.output(searchConfig.retryOutputTag(), searchEvent);
    }


    private DispatcherResult dispatchRecipient(SearchEvent searchRequest, String baseSenderCode, String baseRequestId, boolean isFinalDispatch) throws PipelineException {
        DispatcherResult result = null;
        try {
            //Recipient details from cache or registry
            Map<String,Object> recipientDetails = fetchDetails(baseSenderCode);
            Map<String,Object> recipientContext = createRecipientContext(recipientDetails, searchRequest.getAction());
            Map<String, Object> payLoad = EventUtils.parsePayload(postgresClient.getPayload(searchRequest.getMid()));
            // Update the payload with the new requestId generated for each recipient in the search filters
            Map<String, Object> protectedMap = (Map<String, Object>)payLoad.get(Constants.PROTECTED);
            protectedMap.put(Constants.REQUEST_ID,baseRequestId);
            protectedMap.put(Constants.SENDER_CODE,searchRequest.getRecipientCode());
            protectedMap.put(Constants.RECIPIENT_CODE,baseSenderCode);
            if(isFinalDispatch){
                protectedMap.put(Constants.HCX_STATUS, Constants.RESPONSE_COMPLETE);
                //Update the consolidated counts in the protected map for key SEARCH_RESPONSE
            }else
            protectedMap.put(Constants.HCX_STATUS, Constants.RESPONSE_PARTIAL);
            //Update the payload with the new protected header values
            payLoad.put(Constants.PROTECTED,protectedMap);
            //Encode the payload values with .separated values and send the payload to recipient
            String encodedPayload = EventUtils.encodePayload(payLoad);
            result = DispatcherUtil.dispatch(recipientContext, encodedPayload);
            System.out.println("Dispatching request to recipient with status code" +result.statusCode());
        } catch (Exception ex) {
            throw new PipelineException("Exception occurred while dispatching request to recipient",ex);
        }
        return result;
    }
}
