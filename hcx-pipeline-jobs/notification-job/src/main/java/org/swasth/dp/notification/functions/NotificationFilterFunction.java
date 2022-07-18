package org.swasth.dp.notification.functions;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.core.util.PostgresConnect;
import org.swasth.dp.core.util.PostgresConnectionConfig;
import org.swasth.dp.notification.task.NotificationConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationFilterFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(NotificationFilterFunction.class);
    private NotificationConfig config;
    private RegistryService registryService;
    private PostgresConnect postgresConnect;
    private Map<String,Object> consolidatedEvent;

    public NotificationFilterFunction(NotificationConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        registryService = new RegistryService(config);
        postgresConnect = new PostgresConnect(new PostgresConnectionConfig(config.postgresUser(), config.postgresPassword(), config.postgresDb(), config.postgresHost(), config.postgresPort(), config.postgresMaxConnections()));
        postgresConnect.getConnection();
    }

    @Override
    public void close() throws Exception {
        super.close();
        postgresConnect.closeConnection();
    }

    @Override
    public void processElement(Map<String,Object> inputEvent, ProcessFunction<Map<String,Object>, Map<String,Object>>.Context context, Collector<Map<String,Object>> collector) throws Exception {
        consolidatedEvent = new HashMap<>();
        System.out.println("Event: " + inputEvent);
        logger.debug("Event: " + inputEvent);
        consolidatedEvent.put(Constants.INPUT_EVENT(), inputEvent);
        String topicCode = getProtocolStringValue(Constants.TOPIC_CODE(), inputEvent);
        String senderCode = getProtocolStringValue(Constants.SENDER_CODE(), inputEvent);
        Map<String, Object> notification = getNotification(topicCode);
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
                // check recipients have active subscription
                participantCodes = getParticipantCodes(topicCode, senderCode, Constants.RECIPIENT_CODE(), recipientCodes);;
            }
        }
        List<Map<String, Object>> participantDetails = registryService.getParticipantDetails("{\"participant_code\":{\"or\":[" + addQuotes(participantCodes) + "]}}");
        System.out.println("Total number of participants: " + participantDetails.size());
        logger.debug("Total number of participants: " + participantDetails.size());
        consolidatedEvent.put(Constants.PARTICIPANT_DETAILS(), participantDetails);
        context.output(config.dispatcherOutputTag(), consolidatedEvent);
    }

    private Map<String,Object> getNotification(String topicCode) {
        JSONArray templateData = new JSONArray("[ { \"topic_code\": \"24e975d1-054d-45fa-968e-c91b1043d0a5\", \"title\": \"Organisation status update\", \"description\": \"A notification about the organisation status update in registry. This information will be useful acknowledge the current status of a given organisation and send requests.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"provider\", \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"Event\", \"priority\": 0, \"template\": \"{\\\"message\\\": \\\"${participant_name} status changed to ${status}\\\",\\n\\\"participant_code\\\": \\\"${participant_code}\\\", \\\"endpoint_url\\\": \\\"${endpoint_url}\\\"}\", \"status\": \"active\" }, { \"topic_code\": \"be0e578d-b391-42f9-96f7-1e6bacd91c20\", \"title\": \"Payer Downtime\", \"description\": \"A notification about the Payer System Downtime. This information will be useful for all participants.\", \"allowed_senders\": [ \"provider\" ], \"allowed_recipients\": [ \"payor\", \"agency.tpa\", \"agency.regulator\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"Explicit\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} system will not be available from ${startTime} for a duration of ${duration} on ${date}\\\",\\n\\\"participant_code\\\": \\\"${participant_code}\\\", \\\"endpoint_url\\\": \\\"${endpoint_url}\\\"}\", \"status\": \"active\" }, { \"topic_code\": \"ab0e578d-b391-42f9-96f7-1e6bacd91c20\", \"title\": \"Payer Downtime\", \"description\": \"A notification about the Payer System Downtime. This information will be useful for all participants.\", \"allowed_senders\": [ \"provider\" ], \"allowed_recipients\": [ \"payor\", \"agency.tpa\", \"agency.regulator\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"Explicit\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} system will not be available from ${startTime} for a duration of ${duration} on ${date}\\\",\\n\\\"participant_code\\\": \\\"${participant_code}\\\", \\\"endpoint_url\\\": \\\"${endpoint_url}\\\"}\", \"status\": \"Inactive\" }, { \"topic_code\": \"de0e578d-b391-42f9-96f7-1e6bacd91c20\", \"title\": \"Participant Downtime\", \"description\": \"A notification about the Participant System Downtime. This information will be useful for all participants.\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"agency.tpa\", \"agency.regulator\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"Explicit\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} system will not be available from ${startTime} for a duration of ${duration} on ${date}\\\"}\", \"status\": \"active\" } ]");
        for(Object data: templateData) {
            JSONObject obj = (JSONObject) data;
            if(obj.get(Constants.TOPIC_CODE()).equals(topicCode)){
                return JSONUtil.deserialize(obj.toString(), Map.class);
            }
        }
        return new HashMap<>();
    }

    private String resolveTemplate(Map<String, Object> notification, Map<String,Object> event) {
        StringSubstitutor sub = new StringSubstitutor(getProtocolMapValue(Constants.NOTIFICATION_DATA(), event));
        return sub.replace((JSONUtil.deserialize((String) notification.get(Constants.TEMPLATE()), Map.class)).get(Constants.MESSAGE()));
    }

    private String getProtocolStringValue(String key,Map<String,Object> event) {
        return (String) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, "");
    }

    private Map<String,Object> getProtocolMapValue(String key,Map<String,Object> event) {
        return (Map<String,Object>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new HashMap<>());
    }

    private List<String> getProtocolListValue(String key,Map<String,Object> event) {
        return (List<String>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new ArrayList<>());
    }

    private boolean isExpired(Long expiryTime){
        return new DateTime(expiryTime).isBefore(DateTime.now());
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

    private String addQuotes(List<String> list){
        return list.stream().map(plain ->  StringUtils.wrap(plain, "\"")).collect(Collectors.joining(","));
    }

}
