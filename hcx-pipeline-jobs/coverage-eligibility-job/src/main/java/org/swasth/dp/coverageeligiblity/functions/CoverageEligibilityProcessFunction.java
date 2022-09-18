package org.swasth.dp.coverageeligiblity.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.coverageeligiblity.task.CoverageEligibilityConfig;

import java.util.Map;

public class CoverageEligibilityProcessFunction extends BaseDispatcherFunction {

    private Logger logger = LoggerFactory.getLogger(CoverageEligibilityProcessFunction.class);
    private CoverageEligibilityConfig config;

    public CoverageEligibilityProcessFunction(CoverageEligibilityConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        // TODO: Add domain specific validations
        return new ValidationResult(true, null);
    }

}
