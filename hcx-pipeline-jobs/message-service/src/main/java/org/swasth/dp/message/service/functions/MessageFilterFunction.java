package org.swasth.dp.message.service.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.message.service.task.MessageServiceConfig;

import java.util.Map;

public class MessageFilterFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(MessageFilterFunction.class);

    protected MessageServiceConfig config;

    public MessageFilterFunction(MessageServiceConfig config) {
        this.config = config;
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        String channel = event.getOrDefault(Constants.CHANNEL(), "").toString();
        switch(channel){
            case "email":
                context.output(config.emailOutputTag, event);
                break;
            case "sms":
                context.output(config.smsOutputTag, event);
                break;
            default:
                System.out.println("Invalid event: " + event);
        }
    }

}
