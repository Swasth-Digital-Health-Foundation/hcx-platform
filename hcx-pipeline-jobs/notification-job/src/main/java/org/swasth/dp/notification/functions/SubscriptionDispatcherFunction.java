package org.swasth.dp.notification.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;

import java.util.Map;

public class SubscriptionDispatcherFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionDispatcherFunction.class);
    private NotificationConfig config;
    private DispatcherUtil dispatcherUtil;
    private AuditService auditService;

    public SubscriptionDispatcherFunction(NotificationConfig config) {
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
    public void processElement(Map<String, Object> inputEvent, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        int successfulDispatches = 0;
        int failedDispatches = 0;
        Map<String, Object> contextData = (Map) inputEvent.get(Constants.CDATA());
        Map<String, Object> recipientContextData = (Map) contextData.get(Constants.RECIPIENT());
        Map<String, Object> payloadMap = (Map) inputEvent.get(Constants.PAYLOAD());
        DispatcherResult result = dispatcherUtil.dispatch(recipientContextData, JSONUtil.serialize(payloadMap));
        if(result.success()) successfulDispatches++; else failedDispatches++;
        int totalDispatches = successfulDispatches+failedDispatches;
        System.out.println("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        logger.info("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
    }
}
