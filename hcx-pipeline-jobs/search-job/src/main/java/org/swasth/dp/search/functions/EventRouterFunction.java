package org.swasth.dp.search.functions;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.swasth.dp.core.function.BaseDispatcherFunction;
import org.swasth.dp.core.function.ValidationResult;
import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.search.beans.SearchEvent;
import org.swasth.dp.search.beans.SearchResponse;
import org.swasth.dp.search.db.PostgresClient;
import org.swasth.dp.search.task.CompositeSearchConfig;
import org.swasth.dp.search.utils.BaseUtils;
import org.swasth.dp.search.utils.Constants;
import org.swasth.dp.search.utils.EventUtils;
import org.swasth.dp.search.utils.ResponseStatus;
import scala.collection.JavaConverters.*;
import scala.collection.LinearSeq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRouterFunction extends BaseDispatcherFunction {

    protected PostgresClient postgresClient;
    protected CompositeSearchConfig searchConfig;

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
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception{
        SearchEvent searchEvent = new SearchEvent(event,searchConfig);
        if (StringUtils.endsWith(searchEvent.getAction(), "/on_search")) {
            context.output(searchConfig.searchResponseOutputTag, event);
        } else if (StringUtils.endsWith(searchEvent.getAction(), "/search")) {
            Map<String, Object> payLoad = getPayload(event);
            //Map<String, Object> searchRecipientMap = new HashMap<>();
            event.put(Constants.PAYLOAD, payLoad);
            event.put(Constants.ACTION, searchEvent.getAction());

            // Iterate through each recipient and send across the event to SearchRequestDispatchFunction
            for (String recipientCode :searchEvent.getRecipientCodes()) {
                Map<String, Object> recipientMap = fetchDetails(recipientCode);
                if (MapUtils.isNotEmpty(recipientMap)) {
                    String recipientEndPoint = (String) recipientMap.getOrDefault(Constants.END_POINT, null);
                    event.put(Constants.END_POINT, recipientEndPoint);
                    event.put(Constants.RECIPIENT_CODE, recipientCode);
                    context.output(searchConfig.searchRequestOutputTag, event);
                } else {
                    //TODO Check in API call, if there are any invalid receivers and reject the request with proper message
                    System.out.println("Recipient not present in registry");
                }
            }
            //TODO Insert base record into composite_search table
            postgresClient.insertSearchRecord(searchEvent.getWorkFlowId(),searchEvent.getRequestId(),searchEvent.getSenderCode(),"", ResponseStatus.OPEN.toString(),new SearchResponse());
            audit(event,true,context,metrics);
        } else {
            // TODO this is an invalid event. This doesn't occur. For now ignore this block and just log it.
           System.out.println("Wrong action in the incoming request");
        }
    }

    @Override
    public void audit(Map<String, Object> event, boolean status, Context context, Metrics metrics) throws Exception {
            context.output(searchConfig.auditOutputTag(), BaseUtils.serialize(event));
    }

    @Override
    public Map<String, Object> getPayload(Map<String, Object> event) throws Exception {
        String mid = (String) event.get(Constants.MID);
        return EventUtils.parsePayload(postgresClient.getPayload(mid));
    }

    @Override
    public ValidationResult validate(Map<String, Object> event) {
        return null;
    }


}
