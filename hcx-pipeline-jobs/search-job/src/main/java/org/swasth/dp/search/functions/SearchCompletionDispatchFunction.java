package org.swasth.dp.search.functions;

import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.search.task.CompositeSearchConfig;

import java.util.Map;

public class SearchCompletionDispatchFunction extends EventRouterFunction{
    public SearchCompletionDispatchFunction(CompositeSearchConfig searchConfig) {
        super(searchConfig);
    }
    @Override
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception {
        System.out.println("Event being processed in SearchCompletionDispatchFunction:" + event);
        /**
         *  TODO below logic.
         *  1. Modify the sender(HCX) and recipient(original sender from base record) in the payload
         *  2. Modify the x-hcx-request_id with the requestId from the base record
         *  3. Modify the x-hcx-status as response.complete
         *  3. Fetch all the child record details based on the workflowId
         *  4. Update the consolidated search response from all the recipients
         *  5. Dispatch request to the recipient(original sender) by encoding the updated payload
         *          a. Successful Dispatch, update base record with status as CLOSE
         *          b. In case of dispatch failure to the recipient, update base record status as RETRY
         */
        audit(event, true, context, metrics);

    }
}
