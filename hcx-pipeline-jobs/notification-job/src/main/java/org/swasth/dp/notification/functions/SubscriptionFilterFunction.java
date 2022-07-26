package org.swasth.dp.notification.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;

import java.util.*;

public class SubscriptionFilterFunction extends ProcessFunction<Map<String, Object>, Map<String, Object>> {

    private NotificationConfig config;
    private AuditService auditService;
    private Map<String, Object> consolidatedEvent;

    public SubscriptionFilterFunction(NotificationConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        auditService = new AuditService(config);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    @Override
    public void processElement(Map<String, Object> eventMap, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        //filter the events based on the action
        String action = (String) eventMap.get(Constants.ACTION());
        if (action.equalsIgnoreCase(Constants.NOTIFICATION_SUBSCRIBE()) || action.equalsIgnoreCase(Constants.NOTIFICATION_UNSUBSCRIBE())) {
            String recipientCode = (String) eventMap.get(Constants.HCX_SENDER_CODE());
            Map<String, Object> inputEvent = new HashMap<>(eventMap);
            Map<String, Object> payloadMap = (Map) eventMap.get(Constants.PAYLOAD());
            String topicCode = (String) payloadMap.get(Constants.TOPIC_CODE());
            List<String> senderList = (List<String>) payloadMap.get(Constants.SENDER_LIST());
            //Remove existing payload with all the sender details
            inputEvent.remove(Constants.PAYLOAD());
            for (String senderCode : senderList) {
                if (!senderCode.equalsIgnoreCase(config.hcxRegistryCode())) {
                    //add senderCode as HCX_RECIPIENT_CODE inorder to dispatch the request to the sender for accepting the subscription
                    inputEvent.put(Constants.HCX_RECIPIENT_CODE(), senderCode);
                    //add payload with only single user details
                    inputEvent.put(Constants.PAYLOAD(), createPayload(topicCode, senderCode));
                    //Create audit event
                    auditService.indexAudit(createSubscriptionAuditEvent(action, topicCode, senderCode, recipientCode, Constants.QUEUED_STATUS()));
                    //process each sender record with the modified payload
                    context.output(config.subscribeOutputTag(), JSONUtil.deserialize(JSONUtil.serialize(inputEvent), Map.class));
                } else {
                    auditService.indexAudit(createSubscriptionAuditEvent(action, topicCode, senderCode, recipientCode, Constants.DISPATCH_STATUS()));
                }
            }
        } else { // for /notification/on_subscribe
            String senderCode = (String) eventMap.get(Constants.HCX_SENDER_CODE());
            String recipientCode = (String) eventMap.get(Constants.HCX_RECIPIENT_CODE());
            Map<String, Object> payloadMap = (Map) eventMap.get(Constants.PAYLOAD());
            String subscriptionId = (String) payloadMap.get(Constants.SUBSCRIPTION_ID());
            Double subscriptionStatus = (Double) payloadMap.get(Constants.SUBSCRIPTION_STATUS());
            //Create audit event
            auditService.indexAudit(createOnSubscriptionAuditEvent(action, subscriptionId, recipientCode, senderCode, subscriptionStatus.intValue()));
            context.output(config.onSubscribeOutputTag(), eventMap);
        }
    }

    private Map<String, Object> createPayload(String topicCode, String senderCode) {
        Map<String, Object> event = new HashMap<>();
        event.put(Constants.TOPIC_CODE(), topicCode);
        event.put(Constants.SENDER_LIST(), new ArrayList() {{
            add(senderCode);
        }});
        return event;
    }

    private Map<String, Object> createSubscriptionAuditEvent(String action, String topicCode, String recipientCode, String senderCode, String status) {
        Map<String, Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.MID(), UUID.randomUUID().toString());
        audit.put(Constants.ACTION(), action);
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_CODE(), senderCode);
        audit.put(Constants.RECIPIENT_CODE(), recipientCode);
        audit.put(Constants.TOPIC_CODE(), topicCode);
        audit.put(Constants.NOTIFY_STATUS(), status);
        return audit;
    }

    private Map<String, Object> createOnSubscriptionAuditEvent(String action, String subscriptionId, String recipientCode, String senderCode, int subscriptionStatus) {
        Map<String, Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.MID(), UUID.randomUUID().toString());
        audit.put(Constants.ACTION(), action);
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_CODE(), senderCode);
        audit.put(Constants.RECIPIENT_CODE(), recipientCode);
        audit.put(Constants.SUBSCRIPTION_ID(), subscriptionId);
        audit.put(Constants.SUBSCRIPTION_STATUS(), subscriptionStatus);
        audit.put(Constants.NOTIFY_STATUS(), Constants.QUEUED_STATUS());
        return audit;
    }
}
