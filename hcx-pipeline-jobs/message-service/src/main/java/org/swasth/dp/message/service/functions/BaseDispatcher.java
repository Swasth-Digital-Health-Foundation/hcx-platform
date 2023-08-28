package org.swasth.dp.message.service.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.message.service.helpers.EventGenerator;
import org.swasth.dp.message.service.task.MessageServiceConfig;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseDispatcher  extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    protected MessageServiceConfig config;

    protected AuditService auditService;

    protected EventGenerator eventGenerator;

    public BaseDispatcher(MessageServiceConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        auditService = new AuditService(config);
        eventGenerator = new EventGenerator();
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    protected Map<String,Object> createErrorMap(String code, String message, String trace) {
        Map<String,Object> error = new HashMap<>();
        error.put(Constants.CODE(), code);
        error.put(Constants.MESSAGE(), message);
        error.put(Constants.TRACE(), trace);
        return error;
    }

}
