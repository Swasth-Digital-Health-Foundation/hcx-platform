package org.swasth.dp.search.functions;

import org.swasth.dp.core.job.Metrics;
import org.swasth.dp.search.task.CompositeSearchConfig;

import java.util.Map;

public class SearchRequestDispatchFunction extends EventRouterFunction {

    public SearchRequestDispatchFunction(CompositeSearchConfig searchConfig) {
        super(searchConfig);
    }

    @Override
    public void processElement(Map<String, Object> event, Context context, Metrics metrics) throws Exception {
        System.out.println("Event being processed in SearchRequestDispatchFunction:" + event);
        /**
         *  TODO below logic.
         *  1. Modify the sender(HCX) and recipient(recipient id from search filters) in the payload
         *  2. Modify the x-hcx-request_id with the new generated request id
         *  3. Dispatch request to the recipient by encoding the updated payload
         *          a. Successful Dispatch, Insert child record with status as Open
         *          b. In case of dispatch failure to the recipient, Insert child record with status as Fail
         */

        audit(event, true, context, metrics);

    }
}
