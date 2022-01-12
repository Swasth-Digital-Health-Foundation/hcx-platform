package org.swasth.dp.search.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.Metrics;

import java.util.Map;

//TODO Use this as base class for search and move all reusable common methods here
public class BaseSearchProcessFunction extends BaseDispatcherFunction {

    public BaseSearchProcessFunction(BaseJobConfig config) {
        super(config);
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }

    @Override
    public Map<String, Object> getPayload(Map<String, Object> event) {
        return null;
    }

    @Override
    public void audit(Map<String, Object> event, boolean status, ProcessFunction.Context context, Metrics metrics) {

    }

    @Override
    public void processElement(Map<String, Object> stringObjectMap, ProcessFunction.Context context, Metrics metrics) {

    }
}
