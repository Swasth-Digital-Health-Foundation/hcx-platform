package org.swasth.dp.notification.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ErrorResponse;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.dto.ErrorDetails;
import org.swasth.dp.notification.task.NotificationConfig;

import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationDispatcherFunction extends ProcessFunction<Map<String, Object>, Object> {

    private final Logger logger = LoggerFactory.getLogger(NotificationDispatcherFunction.class);
    private NotificationConfig config;
    private DispatcherUtil dispatcherUtil;
    private AuditService auditService;

    public NotificationDispatcherFunction(NotificationConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        dispatcherUtil = new DispatcherUtil(config);
        auditService = new AuditService(config);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }



    @Override
    public void processElement(Map<String, Object> inputEvent, ProcessFunction<Map<String, Object>, Object>.Context context, Collector<Object> collector) throws Exception {
        Map<String,Object> actualEvent = (Map<String, Object>) inputEvent.get(Constants.INPUT_EVENT());
        Map<String,Object> notificationMasterData = (Map<String, Object>) inputEvent.get(Constants.MASTER_DATA());
        String resolvedTemplate = (String) inputEvent.get(Constants.RESOLVED_TEMPLATE());
        List<Map<String, Object>> participantDetails = (List<Map<String, Object>>) inputEvent.get(Constants.PARTICIPANT_DETAILS());
        notificationDispatcher(notificationMasterData,resolvedTemplate,participantDetails,actualEvent);
    }

    private void notificationDispatcher(Map<String, Object> notificationMasterData, String resolvedTemplate, List<Map<String, Object>> participantDetails,Map<String,Object> event) throws Exception {
        List<Object> dispatchResult = new ArrayList<>();
        int successfulDispatches = 0;
        int failedDispatches = 0;
        for(Map<String,Object> participant: participantDetails) {
            String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE());
            String endpointUrl = (String) participant.get(Constants.END_POINT());
            if (!(participantCode).contains("null") && endpointUrl != null) {
                participant.put(Constants.END_POINT(), endpointUrl + event.get(Constants.ACTION()));
                String payload = getPayload(resolvedTemplate, participantCode, notificationMasterData,event);
                System.out.println("Recipient Id: " + participantCode + " :: Notification payload: " + payload);
                logger.info("Recipient Id: " + participantCode + " :: Notification payload: " + payload);
                DispatcherResult result = dispatcherUtil.dispatch(participant, payload);
                dispatchResult.add(JSONUtil.serialize(new ErrorDetails(participantCode, result.success(), createErrorMap(result))));
                if(result.success()) successfulDispatches++; else failedDispatches++;
            }
        }
        int totalDispatches = successfulDispatches+failedDispatches;
        System.out.println("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        logger.info("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        Map<String,Object> dispatchResultDetails = new HashMap<>();
        dispatchResultDetails.put(Constants.TOTAL_DISPATCHES(), totalDispatches);
        dispatchResultDetails.put(Constants.SUCCESSFUL_DISPATCHES(), successfulDispatches);
        dispatchResultDetails.put(Constants.FAILED_DISPATCHES(), failedDispatches);
        dispatchResultDetails.put(Constants.RESULT_DETAILS(), dispatchResult);
        event.put(Constants.NOTIFICATION_DISPATCH_RESULT(), dispatchResultDetails);
        auditService.indexAudit(createNotificationAuditEvent(event));
    }

    private String getPayload(String notificationMessage, String recipientCode, Map<String,Object> notificationMasterData,Map<String,Object> event) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.SENDER_CODE(), config.hcxRegistryCode());
        request.put(Constants.RECIPIENT_CODE(), recipientCode);
        request.put(Constants.API_CALL_ID(), UUID.randomUUID());
        request.put(Constants.CORRELATION_ID(), getProtocolStringValue(Constants.CORRELATION_ID(),event));
        request.put(Constants.TIMESTAMP(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
        request.put(Constants.NOTIFICATION_DATA(), Collections.singletonMap(Constants.MESSAGE(), notificationMessage));
        request.put(Constants.NOTIFICATION_TITLE(), notificationMasterData.get(Constants.NAME()));
        request.put(Constants.NOTIFICATION_DESC(), notificationMasterData.get(Constants.DESCRIPTION()));
        if(!getProtocolStringValue(Constants.WORKFLOW_ID(),event).isEmpty())
            request.put(Constants.WORKFLOW_ID(), getProtocolStringValue(Constants.WORKFLOW_ID(),event));
        return JSONUtil.serialize(request);
    }

    private String getProtocolStringValue(String key,Map<String,Object> event) {
        return (String) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, "");
    }

    private Map<String,Object> getProtocolMapValue(String key,Map<String,Object> event) {
        return (Map<String,Object>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new HashMap<>());
    }

    private Map<String,Object> createErrorMap(DispatcherResult result){
        Map<String,Object> errorMap = new HashMap<>();
        if (result.error() != null) {
            ErrorResponse error = result.error().get();
            errorMap.put(Constants.CODE(), error.code().get());
            errorMap.put(Constants.MESSAGE(), error.message().get());
            errorMap.put(Constants.TRACE(), error.trace().get());
        }
        return errorMap;
    }

    private Map<String,Object> createNotificationAuditEvent(Map<String,Object> event){
        Map<String,Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.SENDER_CODE(), getProtocolStringValue(Constants.SENDER_CODE(),event));
        audit.put(Constants.RECIPIENT_CODE(), getProtocolStringValue(Constants.RECIPIENT_CODE(),event));
        audit.put(Constants.API_CALL_ID(), getProtocolStringValue(Constants.API_CALL_ID(),event));
        audit.put(Constants.CORRELATION_ID(), getProtocolStringValue(Constants.CORRELATION_ID(),event));
        audit.put(Constants.WORKFLOW_ID(), getProtocolStringValue(Constants.WORKFLOW_ID(),event));
        audit.put(Constants.TIMESTAMP(), getProtocolStringValue(Constants.TIMESTAMP(),event));
        audit.put(Constants.MID(), event.get(Constants.MID()));
        audit.put(Constants.ACTION(), event.get(Constants.ACTION()));
        audit.put(Constants.STATUS(), getProtocolStringValue(Constants.STATUS(),event));
        audit.put(Constants.REQUESTED_TIME(), event.get(Constants.ETS()));
        audit.put(Constants.UPDATED_TIME(), event.getOrDefault(Constants.UPDATED_TIME(), Calendar.getInstance().getTime()));
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_ROLE(), getProtocolStringValue(Constants.SENDER_ROLE(),event).equals(config.hcxRegistryCode())? Collections.singletonList("HIE/HIO.HCX") : Collections.emptyList());
        audit.put(Constants.RECIPIENT_ROLE(), event.getOrDefault(Constants.RECIPIENT_ROLE(), Collections.emptyList()));
        audit.put(Constants.NOTIFICATION_ID(), getProtocolStringValue(Constants.NOTIFICATION_ID(),event));
        audit.put(Constants.NOTIFICATION_DATA(), getProtocolMapValue(Constants.NOTIFICATION_DATA(),event));
        audit.put(Constants.NOTIFICATION_DISPATCH_RESULT(), event.get(Constants.NOTIFICATION_DISPATCH_RESULT()));
        audit.put(Constants.PAYLOAD(), "");
        return audit;
    }

}
