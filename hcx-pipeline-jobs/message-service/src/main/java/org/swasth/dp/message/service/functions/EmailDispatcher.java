package org.swasth.dp.message.service.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.message.service.task.MessageServiceConfig;

import java.util.Map;

public class EmailDispatcher extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(EmailDispatcher.class);

    protected MessageServiceConfig config;

    public EmailDispatcher(MessageServiceConfig config) {
        this.config = config;
    }

    @Override
    public void processElement(Map<String, Object> stringObjectMap, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {

    }
}
