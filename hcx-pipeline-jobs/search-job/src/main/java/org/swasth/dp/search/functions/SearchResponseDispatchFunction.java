package org.swasth.dp.search.functions;

import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.search.task.CompositeSearchConfig;

import java.util.Map;

public class SearchResponseDispatchFunction extends EventRouterFunction{
    public SearchResponseDispatchFunction(CompositeSearchConfig searchConfig) {
        super(searchConfig);
    }

    @Override
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception {
        System.out.println("Event being processed in SearchResponseDispatchFunction:" + event);
        /**
         *  TODO below logic.
         *  1. Fetch the baseRecord based on workflowId,
         *          if status is complete, do nothing
         *       else proceed with next step
         *  2. Modify the sender(HCX) and recipient(original sender from base record) in the payload
         *  3. Modify the x-hcx-request_id with the requestId from the base record
         *  4. Modify the x-hcx-status as response.partial
         *  4. Dispatch request to the recipient
         *  5.      Success: Update child record, with status as Close and response_data with the search response from the event
         *                   Update base record with status as PARTIAL
         *          FAIL: Update the child record, with status as RETRY and response_data with the search response from the event
         *  6. Check whether all the child record status were in CLOSE/FAIL, then mark it as last recipient response received
         *  7. If it is the last recipient response, dispatch request to SearchCompletionDispatchFunction
         */

        audit(event, true, context, metrics);

    }
}
