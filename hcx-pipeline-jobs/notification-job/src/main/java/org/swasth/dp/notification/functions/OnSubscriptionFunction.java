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
            context.output(config.onSubscribeOutputTag(), eventMap);
        } else {
            System.out.println("Wrong event data for this process function OnSubscriptionFunction");
        }
    }
}
