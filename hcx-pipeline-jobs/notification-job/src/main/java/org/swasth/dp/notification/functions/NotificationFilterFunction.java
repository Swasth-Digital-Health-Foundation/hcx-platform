package org.swasth.dp.notification.functions;


import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.*;
import org.swasth.dp.notification.task.NotificationConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class NotificationFilterFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(NotificationFilterFunction.class);
    private NotificationConfig config;
    private RegistryService registryService;
    private PostgresConnect postgresConnect;
    private DispatcherUtil dispatcherUtil;
    private AuditService auditService;
    private Map<String,Object> consolidatedEvent;

    public NotificationFilterFunction(NotificationConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        registryService = new RegistryService(config);
        dispatcherUtil = new DispatcherUtil(config);
        auditService = new AuditService(config);
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
        List<String> subscriptions = (List<String>) inputEvent.get(Constants.SUBSCRIPTIONS());
        List<String> recipientCodes = (List<String>) inputEvent.get(Constants.RECIPIENT_CODES());
        List<String> recipientRoles = (List<String>) inputEvent.get(Constants.RECIPIENT_ROLES());
        if (!subscriptions.isEmpty()) {
            participantCodes = getParticipantCodes(topicCode, senderCode, Constants.SUBSCRIPTION_ID(), subscriptions);
        } else {
            if (recipientCodes.isEmpty()) {
                // fetching participants based on the master data allowed recipient roles
                List<Map<String, Object>> fetchParticipants = registryService.getParticipantDetails("{\"roles\":{\"or\":" + recipientRoles + "}}");
                recipientCodes = fetchParticipants.stream().map(obj -> (String) obj.get(Constants.PARTICIPANT_CODE())).collect(Collectors.toList());
            }
            participantCodes = getParticipantCodes(topicCode, senderCode, Constants.RECIPIENT_CODE(), recipientCodes);
        }
        List<Map<String, Object>> participantDetails = registryService.getParticipantDetails("{\"participant_code\":{\"or\":" + participantCodes + "}}");
        System.out.println("Total number of participants: " + participantDetails.size());
        logger.info("Total number of participants: " + participantDetails.size());
        consolidatedEvent.put(Constants.PARTICIPANT_DETAILS(), participantDetails);
        context.output(config.dispatcherOutputTag(), consolidatedEvent);
    }

    private Map<String,Object> getNotification(String topicCode) {
        JSONArray templateData = new JSONArray(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("notifications.json")).toString());
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

    private boolean isExpired(Long expiryTime){
        return new DateTime(expiryTime).isBefore(DateTime.now());
    }

    private String replaceBraces(List<String> list){
        return list.toString().replace("[", "(").replace("]", ")");
    }

    private List<String> getParticipantCodes(String topicCode, String senderCode, String id, List<String> range) throws SQLException {
        List<String> participantCodes = new ArrayList<>();
        String query = String.format("SELECT %s,%s FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = 1 AND %s IN %s", Constants.RECIPIENT_CODE(), Constants.EXPIRY(),
                config.subscriptionTableName, Constants.SENDER_CODE(), senderCode, Constants.TOPIC_CODE(), topicCode, Constants.SUBSCRIPTION_STATUS(), id, replaceBraces(range));
        ResultSet resultSet = postgresConnect.executeQuery(query);
        while (resultSet.next()) {
            if (!isExpired(resultSet.getLong(Constants.EXPIRY())))
                participantCodes.add(resultSet.getString(Constants.RECIPIENT_CODE()));
        }
        return  participantCodes;
    }

}
