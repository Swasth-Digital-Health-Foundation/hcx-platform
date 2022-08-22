package org.swasth.dp.notification.functions;


import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.notification.task.NotificationConfig;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationFilterFunction extends BaseNotificationFunction {

    private final Logger logger = LoggerFactory.getLogger(NotificationFilterFunction.class);
    private Map<String,Object> consolidatedEvent;

    public NotificationFilterFunction(NotificationConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String,Object> inputEvent, ProcessFunction<Map<String,Object>, Map<String,Object>>.Context context, Collector<Map<String,Object>> collector) throws Exception {
        consolidatedEvent = new HashMap<>();
        System.out.println("Event: " + inputEvent);
        logger.debug("Event: " + inputEvent);
        consolidatedEvent.put(Constants.INPUT_EVENT(), inputEvent);
        String topicCode = getProtocolStringValue(Constants.TOPIC_CODE(), inputEvent);
        String senderCode = getProtocolStringValue(Constants.SENDER_CODE(), inputEvent);
        Map<String, Object> notification = notificationUtil.getNotification(topicCode);
        System.out.println("Notification Master data template: " + notification);
        logger.debug("Notification Master data template: " + notification);
        consolidatedEvent.put(Constants.MASTER_DATA(), notification);
        // resolving notification message template
        String resolvedTemplate = resolveTemplate(notification, inputEvent);
        consolidatedEvent.put(Constants.RESOLVED_TEMPLATE(), resolvedTemplate);
        List<String> participantCodes = new ArrayList<>();
        List<String> subscriptions = getProtocolListValue (Constants.SUBSCRIPTIONS(), inputEvent);
        List<String> recipientCodes = getProtocolListValue (Constants.RECIPIENT_CODES(), inputEvent);
        List<String> recipientRoles = getProtocolListValue (Constants.RECIPIENT_ROLES(), inputEvent);
        if (!subscriptions.isEmpty()) {
            participantCodes = getParticipantCodes(topicCode, senderCode, Constants.SUBSCRIPTION_ID(), subscriptions);
        } else {
            if (recipientCodes.isEmpty()) {
                // fetching participants based on the master data allowed recipient roles
                List<Map<String, Object>> fetchParticipants = registryService.getParticipantDetails("{\"roles\":{\"or\":[" + addQuotes(recipientRoles) + "]}}");
                recipientCodes = fetchParticipants.stream().map(obj -> (String) obj.get(Constants.PARTICIPANT_CODE())).collect(Collectors.toList());
            }
            if(notification.get(Constants.CATEGORY()).equals(Constants.NETWORK())) {
                participantCodes = recipientCodes;
            } else {
                // check if recipients have any active subscription
                participantCodes = getParticipantCodes(topicCode, senderCode, Constants.RECIPIENT_CODE(), recipientCodes);;
            }
        }
        List<Map<String, Object>> participantDetails = registryService.getParticipantDetails("{\"participant_code\":{\"or\":[" + addQuotes(participantCodes) + "]}}");
        System.out.println("Total number of participants: " + participantDetails.size());
        logger.debug("Total number of participants: " + participantDetails.size());
        consolidatedEvent.put(Constants.PARTICIPANT_DETAILS(), participantDetails);
        context.output(config.dispatcherOutputTag(), consolidatedEvent);
    }

    private List<String> getParticipantCodes(String topicCode, String senderCode, String id, List<String> range) throws SQLException {
        List<String> participantCodes = new ArrayList<>();
        String joined = range.stream().map(plain ->  StringUtils.wrap(plain, "'")).collect(Collectors.joining(","));
        String query = String.format("SELECT %s,%s FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = 'Active' AND %s IN (%s)", Constants.RECIPIENT_CODE(), Constants.EXPIRY(),
                config.subscriptionTableName, Constants.SENDER_CODE(), senderCode, Constants.TOPIC_CODE(), topicCode, Constants.SUBSCRIPTION_STATUS(), id, joined);
        ResultSet resultSet = postgresConnect.executeQuery(query);
        while (resultSet.next()) {
            if (!isExpired(resultSet.getLong(Constants.EXPIRY())))
                participantCodes.add(resultSet.getString(Constants.RECIPIENT_CODE()));
        }
        return  participantCodes;
    }
}
