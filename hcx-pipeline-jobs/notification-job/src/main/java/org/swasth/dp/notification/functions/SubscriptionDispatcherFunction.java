package org.swasth.dp.notification.functions;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.DispatcherUtil;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SubscriptionDispatcherFunction extends BaseNotificationFunction {

    private final Logger logger = LoggerFactory.getLogger(SubscriptionDispatcherFunction.class);

    public SubscriptionDispatcherFunction(NotificationConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String, Object> inputEvent, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) throws Exception {
        int successfulDispatches = 0;
        int failedDispatches = 0;
        Map<String, Object> contextData = (Map) inputEvent.get(Constants.CDATA());
        Map<String, Object> recipientContextData = (Map) contextData.get(Constants.RECIPIENT());
        Map<String, Object> payloadMap = (Map) inputEvent.get(Constants.PAYLOAD());
        DispatcherResult result = dispatcherUtil.dispatch(recipientContextData, JSONUtil.serialize(payloadMap));
        if(result.success()) successfulDispatches++; else failedDispatches++;
        int totalDispatches = successfulDispatches+failedDispatches;
        System.out.println("Total number of subscriptions dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        logger.info("Total number of subscriptions dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        String action = (String) inputEvent.get(Constants.ACTION());
        // for /notification/on_subscribe
        if (action.equalsIgnoreCase(Constants.NOTIFICATION_ONSUBSCRIBE())) {
            String subscriptionId = (String) payloadMap.get(Constants.SUBSCRIPTION_ID());
            //Check for any subscriptions which are still in Pending state based on subscriptionId
            boolean hasPendingResponses = hasPendingResponses(subscriptionId);
            // set the status to pending and if all subscription responses were received then send complete
            String hcxStatus = hasPendingResponses ? Constants.PARTIAL_RESPONSE() : Constants.COMPLETE_RESPONSE();
            //Create audit event
            auditService.indexAudit(createOnSubscriptionAuditEvent(inputEvent, createErrorMap(result.error() != null ? result.error().get() : null),hcxStatus));
        }
    }

    private boolean hasPendingResponses(String subscriptionId) throws SQLException {
        String query = String.format("SELECT count(%s) FROM %s WHERE %s IN (SELECT DISTINCT %s from %s WHERE %s = '%s') AND %s = 'Pending' ",
                Constants.SUBSCRIPTION_REQUEST_ID(), config.subscriptionTableName, Constants.SUBSCRIPTION_REQUEST_ID(), Constants.SUBSCRIPTION_REQUEST_ID(),
                config.subscriptionTableName, Constants.SUBSCRIPTION_ID(), subscriptionId, Constants.SUBSCRIPTION_STATUS());
        ResultSet resultSet = postgresConnect.executeQuery(query);
        while (resultSet.next()) {
            int pendingCount = resultSet.getInt("count");
            if (pendingCount > 0)
                return true;
        }
        return false;
    }

}
