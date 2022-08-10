package org.swasth.dp.notification.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ErrorResponse;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;
import scala.Option;

import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationDispatcherFunction extends BaseNotificationFunction {

    private final Logger logger = LoggerFactory.getLogger(NotificationDispatcherFunction.class);

    public NotificationDispatcherFunction(NotificationConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String, Object> inputEvent, ProcessFunction<Map<String, Object>, Map<String,Object>>.Context context, Collector<Map<String,Object>> collector) throws Exception {
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
            if (Constants.INVALID_STATUS().contains(participant.get(Constants.STATUS()))){
                failedDispatches++;
                System.out.println("Recipient code: " + participantCode + " :: Dispatch status: false");
                logger.debug("Recipient code: " + participantCode + " :: Dispatch status: false");
                auditService.indexAudit(createNotificationAuditEvent(event, participantCode, createErrorMap(new ErrorResponse(Option.apply(Constants.ERR_INVALID_RECIPIENT()), Option.apply("Recipient is blocked or inactive as per the registry"), Option.apply("")))));
            } else if (Constants.VALID_STATUS().contains(participant.get(Constants.STATUS())) && !(participantCode).contains("null") && endpointUrl != null) {
                participant.put(Constants.END_POINT(), endpointUrl + event.get(Constants.ACTION()));
                String payload = getPayload(resolvedTemplate, participantCode, notificationMasterData,event);
                DispatcherResult result = dispatcherUtil.dispatch(participant, payload);
                System.out.println("Recipient code: " + participantCode + " :: Dispatch status: " + result.success());
                logger.debug("Recipient code: " + participantCode + " :: Dispatch status: " + result.success());
                auditService.indexAudit(createNotificationAuditEvent(event, participantCode, createErrorMap(result.error() != null ? result.error().get() : null)));
                if(result.success()) successfulDispatches++; else failedDispatches++;
            }
        }
        int totalDispatches = successfulDispatches+failedDispatches;
        System.out.println("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        logger.debug("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
    }

    private String getPayload(String notificationMessage, String recipientCode, Map<String,Object> notificationMasterData,Map<String,Object> event) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.SENDER_CODE(), getProtocolStringValue(Constants.SENDER_CODE(),event));
        request.put(Constants.RECIPIENT_CODE(), recipientCode);
        request.put(Constants.NOTIFICATION_REQ_ID(), event.get(Constants.NOTIFICATION_REQ_ID()));
        request.put(Constants.TIMESTAMP(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
        request.put(Constants.NOTIFICATION_DATA(), Collections.singletonMap(Constants.MESSAGE(), notificationMessage));
        request.put(Constants.TITLE(), notificationMasterData.get(Constants.NAME()));
        request.put(Constants.DESCRIPTION(), notificationMasterData.get(Constants.DESCRIPTION()));
        return JSONUtil.serialize(request);
    }



}
