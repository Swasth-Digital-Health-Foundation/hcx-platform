package org.swasth.dp.notification.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;

import java.util.*;

public class SubscriptionFilterFunction extends BaseNotificationFunction {

    public SubscriptionFilterFunction(NotificationConfig config) {
        super(config);
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
            Map<String, Object> subscriptionMap = (Map) payloadMap.get(Constants.SUBSCRIPTION_MAP());
            //Remove existing payload with all the sender details
            inputEvent.remove(Constants.PAYLOAD());
            for (String senderCode : senderList) {
                if (!senderCode.equalsIgnoreCase(config.hcxRegistryCode())) {
                    //add senderCode as HCX_RECIPIENT_CODE inorder to dispatch the request to the sender for accepting the subscription
                    inputEvent.put(Constants.HCX_RECIPIENT_CODE(), senderCode);
                    //add payload with only single user details
                    inputEvent.put(Constants.PAYLOAD(), createPayload(topicCode, senderCode, subscriptionMap, recipientCode));
                    //Create audit event
                    auditService.indexAudit(createSubscriptionAuditEvent(action, topicCode, senderCode, recipientCode, Constants.QUEUED_STATUS()));
                    //process each sender record with the modified payload
                    context.output(config.subscribeOutputTag(), JSONUtil.deserialize(JSONUtil.serialize(inputEvent), Map.class));
                } else {
                    auditService.indexAudit(createSubscriptionAuditEvent(action, topicCode, senderCode, recipientCode, Constants.DISPATCH_STATUS()));
                }
            }
        } else {
            System.out.println("Wrong event data for this process function SubscriptionFilterFunction");
        }
    }

    private Map<String, Object> createPayload(String topicCode, String senderCode, Map<String, Object> subscriptionMap, String recipientCode) {
        Map<String, Object> event = new HashMap<>();
        event.put(Constants.TOPIC_CODE(), topicCode);
        event.put(Constants.SENDER_LIST(), new ArrayList() {{
            add(senderCode);
        }});
        event.put(Constants.RECIPIENT_CODE(), recipientCode);
        event.put(Constants.SUBSCRIPTION_ID(),subscriptionMap.get(senderCode));
        return event;
    }

}
