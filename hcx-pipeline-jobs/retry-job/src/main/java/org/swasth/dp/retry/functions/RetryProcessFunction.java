package org.swasth.dp.retry.functions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.retry.task.RetryConfig;

import java.util.Map;

public class RetryProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(RetryProcessFunction.class);
    private RetryConfig config;

    public RetryProcessFunction(RetryConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

}
