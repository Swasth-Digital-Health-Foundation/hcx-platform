package org.swasth.dp.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.task.ProtocolRequestProcessorConfig;

import java.util.Map;

public class ClaimsProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(ClaimsProcessFunction.class);
    private ProtocolRequestProcessorConfig config;

    public ClaimsProcessFunction(ProtocolRequestProcessorConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

}