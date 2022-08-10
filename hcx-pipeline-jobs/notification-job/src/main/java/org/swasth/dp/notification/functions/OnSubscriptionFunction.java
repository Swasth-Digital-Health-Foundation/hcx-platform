package org.swasth.dp.notification.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.notification.task.NotificationConfig;

import java.util.Map;

public class OnSubscriptionFunction extends BaseNotificationFunction {


    public OnSubscriptionFunction(NotificationConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String, Object> eventMap, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        String action = (String) eventMap.get(Constants.ACTION());
        // for /notification/on_subscribe
        if (action.equalsIgnoreCase(Constants.NOTIFICATION_ONSUBSCRIBE())) {
            String senderCode = (String) eventMap.get(Constants.HCX_SENDER_CODE());
            String recipientCode = (String) eventMap.get(Constants.HCX_RECIPIENT_CODE());
            Map<String, Object> payloadMap = (Map) eventMap.get(Constants.PAYLOAD());
            String subscriptionId = (String) payloadMap.get(Constants.SUBSCRIPTION_ID());
            Double subscriptionStatus = (Double) payloadMap.get(Constants.SUBSCRIPTION_STATUS());
            //Check for any subscriptions which are still in Pending state, set the status
            //Create audit event
            //auditService.indexAudit(createOnSubscriptionAuditEvent(action, subscriptionId, recipientCode, senderCode, subscriptionStatus.intValue()));
            context.output(config.onSubscribeOutputTag(), eventMap);
        } else {
            System.out.println("Wrong event data for this process function OnSubscriptionFunction");
        }
    }
}
