package org.swasth.dp.search.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ContextEnrichmentFunction;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.search.beans.CompositeSearch;
import org.swasth.dp.search.beans.SearchResponse;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SearchDispatchProcessFunction extends BaseDispatcherFunction {

    private PostgresClient postgresClient;
    private CompositeSearchConfig searchConfig;

    public SearchDispatchProcessFunction(CompositeSearchConfig searchConfig) {
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

            String mid = getPayloadRefId(searchEvent);
            String action = getAction(searchEvent);
            String workFlowId = getWorkflowId(searchEvent);
            // Recipient from the searchFilter to whom the request has to be dispatched
            String recipientCode = getRecipientCode(searchEvent);
            String senderCode = getSenderCode(searchEvent);
            // Generate new x-hcx-request_id for each request
            String requestId = UUID.randomUUID().toString();
        try {
            //Send request to the recipient with
            DispatcherResult dispatcherResult = dispatchRecipient(mid,action,recipientCode,requestId);
            // If status code is other than success, then insert the record, status=FAIL and  with 0 counts in response data
            if(dispatcherResult != null && dispatcherResult.success()) {
                // Insert recipient record into composite_search table in postgres
                postgresClient.insertSearchRecord(workFlowId, requestId, senderCode, recipientCode, ResponseStatus.OPEN.toString(), new SearchResponse());
                //TODO Update the request id with the new request id for this event, add it to audit log
                audit(searchEvent,true,context,metrics);
            } else {
                respondToSender(mid, action, workFlowId, recipientCode, senderCode, requestId);
                //TODO update errorKey, requestId(original id) and status to the audit log
                audit(searchEvent,true,context,metrics);
            }

        } catch (Exception e) {
            try {
                respondToSender(mid, action, workFlowId, recipientCode, senderCode, requestId);
                //TODO update errorKey, requestId(original id) and status to the audit log
                audit(searchEvent,true,context,metrics);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void respondToSender(String mid, String action, String workFlowId, String recipientCode, String senderCode, String requestId) throws Exception {
        //TODO Use upsert query instead of failing while executing the insert query
        // Update the child record status as fail with count,entityCount as 0
        postgresClient.insertSearchRecord(workFlowId, requestId, senderCode, recipientCode, ResponseStatus.FAIL.toString(), EventUtils.generateFailResponse());
        // fetch the actual sender details from the database and use the sender code of the base record
         CompositeSearch compositeSearch = postgresClient.getBaseRecord(workFlowId);
        //Dispatch the on_search request to the actual senderCode from BaseRecord in postgres table with the old request id
        dispatchSender(mid, action, compositeSearch.getSenderCode(),compositeSearch.getRequestId(),EventUtils.prepareErrorDetails(ErrorCode.INTERNAL_SERVER_ERROR));
        //TODO update the base record with status as partial

    }

    private DispatcherResult dispatchRecipient(String mid,String action, String recipientCode,String requestId) throws PipelineException {
        DispatcherResult result = null;
        try {
            Map<String,Object> recipientDetails = fetchDetails(recipientCode);
            Map<String,Object> recipientContext = createRecipientContext(recipientDetails, action);
            Map<String, Object> payLoad = EventUtils.parsePayload(postgresClient.getPayload(mid));
            // Update the payload with the new requestId generated for each recipient in the search filters
            Map<String, Object> protectedMap = (Map<String, Object>)payLoad.get(Constants.PROTECTED);
            protectedMap.put(Constants.REQUEST_ID,requestId);
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

    private void dispatchSender(String mid,String action, String senderCode,String requestId,Map<String,Object> errorMap) throws PipelineException {
        try {
            Map<String,Object> senderDetails = fetchDetails(senderCode);
            Map<String,Object> senderContext = createSenderContext(senderDetails, action);
            Map<String, Object> payLoad = EventUtils.parsePayload(postgresClient.getPayload(mid));
            // Update the payload with the error info in the protected header
            Map<String, Object> protectedMap = (Map<String, Object>)payLoad.get(Constants.PROTECTED);
            protectedMap.put(Constants.ERROR_KEY,errorMap);
            protectedMap.put(Constants.REQUEST_ID, requestId);
            protectedMap.put(Constants.HCX_STATUS,Constants.RESPONSE_PARTIAL);
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
