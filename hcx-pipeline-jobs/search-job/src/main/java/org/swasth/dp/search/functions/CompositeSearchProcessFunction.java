package org.swasth.dp.search.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.core.util.PostgresConnect;
import org.swasth.dp.core.util.PostgresConnectionConfig;
import org.swasth.dp.search.beans.SearchRequest;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeSearchProcessFunction extends BaseDispatcherFunction {

    private PostgresClient postgresClient;
    private CompositeSearchConfig searchConfig;

    public CompositeSearchProcessFunction(CompositeSearchConfig searchConfig){
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
    public Map<String, Object> getPayload(Map<String, Object> event){
        String mid = (String) event.get(Constants.MID);
        Map<String, Object> payLoad = null;
        try {
            payLoad = BaseUtils.decodeBase64String(postgresClient.getPayload(mid),Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payLoad;
    }

    @Override
    public void audit(Map<String, Object> event, boolean status, ProcessFunction.Context context, Metrics metrics) {
        String auditData = null;
        try {
            Map<String, Object>  audit = createAuditRecord(event,"AUDIT");
            auditData = JSONUtil.serialize(audit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.output(searchConfig.auditOutputTag(), auditData);
    }

    @Override
    public void processElement(Map<String, Object> eventMap, ProcessFunction.Context context, Metrics metrics) {
        Map<String,Object> searchMap = eventMap;
        //Fetch the details from the kafka topic
        SearchRequest searchRequest = new SearchRequest(eventMap,searchConfig);

        String action = searchRequest.getAction();
        if(action.endsWith("on_search")){
            //process the response here and use separate processFunction to process the responses
            context.output(searchConfig.searchResponseOutputTag, searchMap);
        }else{
            String senderCode = searchRequest.getSenderCode();
            try{
                // Insert the parent record into the composite_search table in postgres
                String workFlowId = searchRequest.getWorkFlowId();
                String requestId = searchRequest.getRequestId();
                postgresClient.insertBaseRecord(workFlowId,requestId,senderCode);

                // Iterate through each recipient and replace sender and recipient codes in the protocol headers
                searchRequest.getRecipientCodes().stream().forEach(recipientCode -> {
                    EventUtils.replaceSenderAndRecipientCodes(searchRequest,recipientCode,true);
                    //TODO update the eventMap with the new search filters and new protocol headers
                    context.output(searchConfig.searchOutputTag,searchMap);
                });
                //Create audit log entry to the audit topic
                audit(eventMap,true,context,metrics);
            }catch (Exception e){
                // Send across error message to the sender with on_search api call
                try {
                    dispatchSender(searchRequest.getMid(), action, senderCode,EventUtils.prepareErrorDetails(ErrorCode.INTERNAL_SERVER_ERROR));
                } catch (PipelineException ex) {
                    //TODO
                    ex.printStackTrace();
                }
            }
        }
    }

    private void dispatchSender(String mid,String action, String senderCode,Map<String,Object> errorMap) throws PipelineException {
        try {
            Map<String,Object> senderDetails = fetchDetails(senderCode);
            Map<String,Object> senderContext = createSenderContext(senderDetails, action);
            Map<String, Object> payLoad = EventUtils.parsePayload(postgresClient.getPayload(mid));
            // Update the payload with the error info in the protected header
            Map<String, Object> protectedMap = (Map<String, Object>)payLoad.get(Constants.PROTECTED);
            protectedMap.put(Constants.ERROR_KEY,errorMap);
            //Update the payload with the new protected header
            payLoad.put(Constants.PROTECTED,protectedMap);
            //Encode the payload values with .separated values and send it back to the sender
            String encodedPayload = EventUtils.encodePayload(payLoad);
            DispatcherResult result = DispatcherUtil.dispatch(senderContext, encodedPayload);
            System.out.println("Dispatching back to sender with status code" +result.statusCode());

        } catch (Exception ex) {
           throw new PipelineException("Exception occurred while sending request to sender",ex);
        }
    }

}
