package org.swasth.dp.claims.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.claims.task.ClaimsConfig;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;

import java.util.Map;

public class ClaimsProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(ClaimsProcessFunction.class);
    private ClaimsConfig config;

    public ClaimsProcessFunction(ClaimsConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

}
