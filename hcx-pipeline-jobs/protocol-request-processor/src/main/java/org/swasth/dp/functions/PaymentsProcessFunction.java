package org.swasth.dp.functions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.task.ProtocolRequestProcessorConfig;

import java.util.Map;

public class PaymentsProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(PaymentsProcessFunction.class);
    private ProtocolRequestProcessorConfig config;

    public PaymentsProcessFunction(ProtocolRequestProcessorConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

}