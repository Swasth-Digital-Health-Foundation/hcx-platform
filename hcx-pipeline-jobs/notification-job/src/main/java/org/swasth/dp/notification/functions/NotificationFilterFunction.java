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
import java.text.SimpleDateFormat;
import java.util.*;
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
        String senderCode = getProtocolStringValue(Constants.HCX_SENDER_CODE(), inputEvent);
        Map<String,Object> notificationHeaders = getProtocolMapValue(Constants.NOTIFICATION_HEADERS(), inputEvent);
        Map<String, Object> notification = notificationUtil.getNotification(topicCode);
        System.out.println("Notification Master data template: " + notification);
        logger.debug("Notification Master data template: " + notification);
        consolidatedEvent.put(Constants.MASTER_DATA(), notification);
        // resolving notification message template
        Map<String,Object> updatedProtocolHeaders = getProtocolHeaders(inputEvent);
        String resolvedTemplate;
        if (inputEvent.get(Constants.MESSAGE()) != null) {
            resolvedTemplate = (String) inputEvent.get(Constants.MESSAGE());
        }
        else {
            resolvedTemplate = resolveTemplate(notification, inputEvent);
            Map<String,Object> notificationPayload = new HashMap<>();
            notificationPayload.put(Constants.TOPIC_CODE(), topicCode);
            notificationPayload.put(Constants.MESSAGE(), resolvedTemplate);
            updatedProtocolHeaders.put(Constants.PAYLOAD(), jwtUtil.generateJWS(notificationPayload));
            Map<String,Object> updatedNotificationHeaders = new HashMap<>();
            updatedNotificationHeaders.put(Constants.SENDER_CODE(), config.hcxRegistryCode());
            updatedNotificationHeaders.put(Constants.TIMESTAMP(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
            updatedProtocolHeaders.put(Constants.NOTIFICATION_HEADERS(), updatedNotificationHeaders);
        }
        ((Map<String,Object>) inputEvent.get(Constants.HEADERS())).put(Constants.PROTOCOL(), updatedProtocolHeaders);
        consolidatedEvent.put(Constants.INPUT_EVENT(), inputEvent);
        consolidatedEvent.put(Constants.RESOLVED_TEMPLATE(), resolvedTemplate);
        List<String> participantCodes;
        List<String> subscriptions = (List<String>) notificationHeaders.get(Constants.SUBSCRIPTIONS());
        List<String> recipientCodes = (List<String>) notificationHeaders.get(Constants.RECIPIENT_CODES());
        List<String> recipientRoles = (List<String>) notificationHeaders.get(Constants.RECIPIENT_ROLES());
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
        String query = String.format("SELECT %s,%s FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = 1 AND %s IN (%s)", Constants.RECIPIENT_CODE(), Constants.EXPIRY(),
                config.subscriptionTableName, Constants.SENDER_CODE(), senderCode, Constants.TOPIC_CODE(), topicCode, Constants.SUBSCRIPTION_STATUS(), id, joined);
        ResultSet resultSet = postgresConnect.executeQuery(query);
        while (resultSet.next()) {
            if (!isExpired(resultSet.getLong(Constants.EXPIRY())))
                participantCodes.add(resultSet.getString(Constants.RECIPIENT_CODE()));
        }
        return  participantCodes;
    }
}
