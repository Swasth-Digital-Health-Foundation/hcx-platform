package org.swasth.dp.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.task.ProtocolRequestProcessorConfig;

import java.util.Map;

public class ProcessorFilterFunction extends ProcessFunction<Map<String, Object>, Map<String,Object>> {

    private ProtocolRequestProcessorConfig config;

    public ProcessorFilterFunction(ProtocolRequestProcessorConfig config) {
        this.config = config;
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) {
        String action = (String) event.get(Constants.ACTION());
        if (action.equalsIgnoreCase(Constants.COVERAGE_ELIGIBILITY_CHECK()) || action.equalsIgnoreCase(Constants.COVERAGE_ELIGIBILITY_ONCHECK())) {
            new CoverageEligibilityProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.CLAIM_SUBMIT()) || action.equalsIgnoreCase(Constants.CLAIM_ONSUBMIT())) {
            new ClaimsProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.PREDETERMINATION_SUBMIT()) || action.equalsIgnoreCase(Constants.PREDETERMINATION_ONSUBMIT())) {
            new PredeterminationProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.PRE_AUTH_SUBMIT()) || action.equalsIgnoreCase(Constants.PRE_AUTH_ONSUBMIT())) {
            new PreauthProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.EOB_FETCH()) || action.equalsIgnoreCase(Constants.PREDETERMINATION_ONSUBMIT())) {
            new FetchProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.PAYMENT_NOTICE_REQUEST()) || action.equalsIgnoreCase(Constants.PAYMENT_NOTICE_ONREQUEST())) {
            new PaymentsProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.COMMUNICATION_REQUEST()) || action.equalsIgnoreCase(Constants.COMMUNICATION_ONREQUEST())) {
            new CommunicationProcessFunction(config);
        } else if (action.equalsIgnoreCase(Constants.HCX_STATUS_CONTROLLER()) || action.equalsIgnoreCase(Constants.HCX_ONSTATUS_CONTROLLER())) {
            new StatusSearchProcessFunction(config);
        } else if (action.startsWith(Constants.REQUEST_RETRY())) {
            new RetryProcessFunction(config);
        }
    }
}
