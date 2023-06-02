package org.swasth.dp.fetch.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.fetch.task.FetchConfig;

import java.util.Map;

public class FetchProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(FetchProcessFunction.class);
    private FetchConfig config;

    public FetchProcessFunction(FetchConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

}
