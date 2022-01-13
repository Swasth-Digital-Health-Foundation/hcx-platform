package org.swasth.dp.search.functions;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.search.beans.SearchEvent;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.BaseUtils;
import org.swasth.dp.search.utils.Constants;

import java.util.Map;

public class EventRouterFunction extends BaseDispatcherFunction {

    private PostgresClient postgresClient;
    private CompositeSearchConfig searchConfig;

    public EventRouterFunction(CompositeSearchConfig searchConfig){
        super(searchConfig);
        this.searchConfig = searchConfig;
    }

    @Override
    public void open(Configuration parameters) {
        super.open(parameters);
        this.postgresClient = new PostgresClient(searchConfig);
    }

    @Override
    public void close() throws Exception {
        this.postgresClient.getPostgresConnect().closeConnection();
        super.close();
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction.Context context, Metrics metrics) {
        SearchEvent searchEvent = new SearchEvent(event,searchConfig);

        if (StringUtils.endsWith(searchEvent.getAction(), "/on_search")) {
            // TODO this is a response of the search request.
            context.output(searchConfig.searchResponseOutputTag, event);
        } else if (StringUtils.endsWith(searchEvent.getAction(), "/search")) {
            // TODO this is the request of search request.
            String mid = searchEvent.getMid();
            /**
             *  TODO below logic.
             *  1. Get the payload using mid.
             *  2. Identify the recipient list.
             *      a. fetch the details of recipient.
             *      b. recipient endpointUrl, payload, request as a map send it to the next process function (SearchRequestDispatchFn).
             *  3. SearchRequestDispatchFn - Implementation
             */
        } else {
            // TODO this is an invalid event. This doesn't occur. For now ignore this block and just log it.

        }
    }

    @Override
    public void audit(Map<String, Object> event, boolean status, ProcessFunction.Context context, Metrics metrics) {

    }

    @Override
    public Map<String, Object> getPayload(Map<String, Object> event) throws Exception {
        String mid = (String) event.get(Constants.MID);
        return BaseUtils.decodeBase64String(postgresClient.getPayload(mid),Map.class);
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }
}
