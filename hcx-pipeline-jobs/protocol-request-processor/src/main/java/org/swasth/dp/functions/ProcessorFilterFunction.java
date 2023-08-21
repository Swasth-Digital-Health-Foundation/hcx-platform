package org.swasth.dp.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.task.ProtocolRequestProcessorConfig;

import java.util.Map;

public class ProcessorFilterFunction extends ProcessFunction<Map<String, Object>, Map<String, Object>> {

    private final Logger logger = LoggerFactory.getLogger(ProcessorFilterFunction.class);

    private ProtocolRequestProcessorConfig config;

    public ProcessorFilterFunction(ProtocolRequestProcessorConfig config) {
        this.config = config;
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        String action = (String) event.get(Constants.ACTION());
        if (action.equalsIgnoreCase(Constants.COVERAGE_ELIGIBILITY_CHECK()) || action.equalsIgnoreCase(Constants.COVERAGE_ELIGIBILITY_ONCHECK())) {
            pushToOutputTag(context, event, config.coverageEligibilityOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.CLAIM_SUBMIT()) || action.equalsIgnoreCase(Constants.CLAIM_ONSUBMIT())) {
            pushToOutputTag(context, event, config.claimOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.PREDETERMINATION_SUBMIT()) || action.equalsIgnoreCase(Constants.PREDETERMINATION_ONSUBMIT())) {
            pushToOutputTag(context, event, config.preDeterminationOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.PRE_AUTH_SUBMIT()) || action.equalsIgnoreCase(Constants.PRE_AUTH_ONSUBMIT())) {
            pushToOutputTag(context, event, config.preAuthOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.EOB_FETCH()) || action.equalsIgnoreCase(Constants.EOB_ON_FETCH())) {
            pushToOutputTag(context, event, config.fetchOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.PAYMENT_NOTICE_REQUEST()) || action.equalsIgnoreCase(Constants.PAYMENT_NOTICE_ONREQUEST())) {
            pushToOutputTag(context, event, config.paymentOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.COMMUNICATION_REQUEST()) || action.equalsIgnoreCase(Constants.COMMUNICATION_ONREQUEST())) {
            pushToOutputTag(context, event, config.communicationOutputTag, action);
        } else if (action.equalsIgnoreCase(Constants.HCX_STATUS_CONTROLLER()) || action.equalsIgnoreCase(Constants.HCX_ONSTATUS_CONTROLLER())) {
            pushToOutputTag(context, event, config.statusSearchOutputTag, action);
        } else if (action.startsWith(Constants.REQUEST_RETRY())) {
            pushToOutputTag(context, event, config.retryOutputTag, action);
        }
    }

    private void pushToOutputTag(ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Map<String, Object> event, OutputTag<Map<String, Object>> outputTag, String action) {
        if (event != null) {
            context.output(outputTag, event);
            logger.info("{} : is processing", action);
            logger.info("event added to output tag :: {} ", event);
        }
    }
}
