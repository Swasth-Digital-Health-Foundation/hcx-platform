package org.swasth.dp.notification.functions;


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
        String topicCode = (String) inputEvent.get(Constants.TOPIC_CODE());
        Map<String,Object> notificationHeaders = getProtocolMapValue(Constants.NOTIFICATION_HEADERS(), inputEvent);
        String senderCode = (String) notificationHeaders.get(Constants.SENDER_CODE());
        Map<String, Object> notification = notificationUtil.getNotification(topicCode);
        System.out.println("Notification Master data template: " + notification);
        logger.debug("Notification Master data template: " + notification);
        consolidatedEvent.put(Constants.MASTER_DATA(), notification);
        consolidatedEvent.put(Constants.INPUT_EVENT(), inputEvent);
        List<String> participantCodes;
        List<String> recipients = (List<String>) notificationHeaders.get(Constants.RECIPIENTS());
        String recipientType = (String) notificationHeaders.get(Constants.RECIPIENT_TYPE());
        if (recipientType.equalsIgnoreCase(Constants.SUBSCRIPTION())) {
            participantCodes = getParticipantCodes(topicCode, senderCode, Constants.SUBSCRIPTION_ID(), recipients);
        } else {
            if (recipientType.equalsIgnoreCase(Constants.PARTICIPANT_ROLE())) {
                // fetching participants based on the master data allowed recipient roles
                List<Map<String, Object>> fetchParticipants = registryService.getParticipantDetails("{\"roles\":{\"or\":[" + addQuotes(recipients) + "]}}");
                recipients = fetchParticipants.stream().map(obj -> (String) obj.get(Constants.PARTICIPANT_CODE())).collect(Collectors.toList());
            }
            System.out.println("Notification Filter function ----" + notification);
            System.out.println(" Topic code is ------" + topicCode);
            System.out.println("notification category -------" + notification.getOrDefault(Constants.CATEGORY(), "No category found"));
            if(notification.get(Constants.CATEGORY()).equals(Constants.NETWORK())) {
                participantCodes = recipients;
            } else {
                // check if recipients have any active subscription
                participantCodes = getParticipantCodes(topicCode, senderCode, Constants.RECIPIENT_CODE(), recipients);;
            }
        }
        List<Map<String, Object>> participantDetails = registryService.getParticipantDetails("{\"participant_code\":{\"or\":[" + addQuotes(participantCodes) + "]}}");
        System.out.println("Total number of participants: " + participantDetails.size());
        logger.debug("Total number of participants: " + participantDetails.size());
        consolidatedEvent.put(Constants.PARTICIPANT_DETAILS(), participantDetails);
        context.output(config.dispatcherOutputTag(), consolidatedEvent);
    }

    private List<String> getParticipantCodes(String topicCode, String senderCode, String id, List<String> recipients) throws SQLException {
        List<String> participantCodes = new ArrayList<>();
        String formatRecipients = recipients.stream().map(plain ->  "'" + plain + "'").collect(Collectors.joining(","));
        String query = String.format("SELECT %s,%s FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = 'Active' AND %s IN (%s)", Constants.RECIPIENT_CODE(), Constants.EXPIRY(),
                config.subscriptionTableName, Constants.SENDER_CODE(), senderCode, Constants.TOPIC_CODE(), topicCode, Constants.SUBSCRIPTION_STATUS(), id, formatRecipients);
        ResultSet resultSet = postgresConnect.executeQuery(query);
        while (resultSet.next()) {
            if (!isExpired(resultSet.getLong(Constants.EXPIRY())))
                participantCodes.add(resultSet.getString(Constants.RECIPIENT_CODE()));
        }
        return  participantCodes;
    }
}
