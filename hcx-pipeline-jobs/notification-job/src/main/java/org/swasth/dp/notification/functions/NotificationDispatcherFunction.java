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
        Map<String,Object> notification = (Map<String, Object>) inputEvent.get(Constants.MASTER_DATA());
        String resolvedTemplate = (String) inputEvent.get(Constants.RESOLVED_TEMPLATE());
        List<Map<String, Object>> participantDetails = (List<Map<String, Object>>) inputEvent.get(Constants.PARTICIPANT_DETAILS());
        notificationDispatcher(notification,resolvedTemplate,participantDetails,actualEvent);
    }

    private void notificationDispatcher(Map<String, Object> notificationMasterData, String resolvedTemplate, List<Map<String, Object>> participantDetails,Map<String,Object> event) throws Exception {
        int successfulDispatches = 0;
        int failedDispatches = 0;
        for(Map<String,Object> participant: participantDetails) {
            String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE());
            String endpointUrl = (String) participant.get(Constants.END_POINT());
            if (!(participantCode).contains("null") && endpointUrl != null) {
                participant.put(Constants.END_POINT(), endpointUrl + event.get(Constants.ACTION()));
                String payload = getPayload(resolvedTemplate, participantCode, notificationMasterData,event);
                DispatcherResult result = dispatcherUtil.dispatch(participant, payload);
                System.out.println("Recipient code: " + participantCode + " :: Dispatch status: " + result.success());
                logger.info("Recipient code: " + participantCode + " :: Dispatch status: " + result.success());
                auditService.indexAudit(createNotificationAuditEvent(event, participantCode, createErrorMap(result)));
                if(result.success()) successfulDispatches++; else failedDispatches++;
            }
        }
        int totalDispatches = successfulDispatches+failedDispatches;
        System.out.println("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        logger.info("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
    }

    private String getPayload(String notificationMessage, String recipientCode, Map<String,Object> notificationMasterData,Map<String,Object> event) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.SENDER_CODE(), getProtocolStringValue(Constants.SENDER_CODE(),event));
        request.put(Constants.RECIPIENT_CODE(), recipientCode);
        request.put(Constants.NOTIFICATION_ID(), event.get(Constants.NOTIFICATION_ID()));
        request.put(Constants.TIMESTAMP(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
        request.put(Constants.NOTIFICATION_DATA(), Collections.singletonMap(Constants.MESSAGE(), notificationMessage));
        request.put(Constants.TITLE(), notificationMasterData.get(Constants.NAME()));
        request.put(Constants.DESCRIPTION(), notificationMasterData.get(Constants.DESCRIPTION()));
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

    private Map<String,Object> createNotificationAuditEvent(Map<String,Object> event, String recipientCode, Map<String,Object> errorDetails){
        Map<String,Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.MID(), UUID.randomUUID().toString());
        audit.put(Constants.ACTION(), event.get(Constants.ACTION()));
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_CODE(), getProtocolStringValue(Constants.SENDER_CODE(),event));
        audit.put(Constants.RECIPIENT_CODE(), recipientCode);
        audit.put(Constants.NOTIFICATION_ID(), event.get(Constants.NOTIFICATION_ID()));
        audit.put(Constants.TOPIC_CODE(), getProtocolStringValue(Constants.TOPIC_CODE(),event));
        if(!errorDetails.isEmpty()) {
            audit.put(Constants.ERROR_DETAILS(), errorDetails);
            audit.put(Constants.STATUS(), Constants.ERROR_STATUS());
        } else {
            audit.put(Constants.STATUS(), Constants.DISPATCH_STATUS());
        }
        return audit;
    }

}
