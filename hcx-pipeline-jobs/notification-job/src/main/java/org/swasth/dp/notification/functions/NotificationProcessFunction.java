package org.swasth.dp.notification.functions;


import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.*;
import org.swasth.dp.notification.task.NotificationConfig;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationProcessFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(NotificationProcessFunction.class);
    private NotificationConfig config;
    private RegistryService registryService;
    private PostgresConnect postgresConnect;
    private DispatcherUtil dispatcherUtil;
    private AuditService auditService;
    private Map<String,Object> consolidatedEvent;

    public NotificationProcessFunction(NotificationConfig config) {
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
        consolidatedEvent.put(Constants.INPUT_EVENT(),inputEvent);
        String notificationId = getProtocolStringValue(Constants.NOTIFICATION_ID(),inputEvent);
        Map<String,Object> notificationMasterData = getNotificationMasterData(notificationId);
        System.out.println("Notification Master data template: " + notificationMasterData);
        logger.debug("Notification Master data template: " + notificationMasterData);
        consolidatedEvent.put(Constants.MASTER_DATA(),notificationMasterData);
        String notificationType = (String) notificationMasterData.get(Constants.TYPE());
        // resolving notification message template
        String resolvedTemplate = resolveTemplate(notificationMasterData,inputEvent);
        consolidatedEvent.put(Constants.RESOLVED_TEMPLATE(),resolvedTemplate);
        List<Map<String, Object>> participantDetails = null;
        // for broadcast notification type
        if(notificationType.equalsIgnoreCase(Constants.BROADCAST())) {
            // fetching participants based on the master data roles
            List<String> masterRoles = (ArrayList<String>) notificationMasterData.get(Constants.RECIPIENT());
            participantDetails = new ArrayList<>();
            for(String role: masterRoles) {
                List<Map<String,Object>> fetchParticipants = registryService.getParticipantDetails("{\"roles\":{\"eq\":\"" + role + "\"}}");
                if(!fetchParticipants.isEmpty())
                  participantDetails.addAll(fetchParticipants);
            }
            System.out.println("Total number of participants: " + participantDetails.size());
            logger.info("Total number of participants: " + participantDetails.size());
            // fetching unsubscribed list
            String query = String.format("SELECT " + Constants.RECIPIENT_ID() + " FROM %s WHERE notificationId = '%s' AND status = 0", config.subscriptionTableName, notificationId);
            ResultSet resultSet = postgresConnect.executeQuery(query);
            List<String> unsubscribedList = new ArrayList<>();
            while(resultSet.next()){
                unsubscribedList.add(resultSet.getString(Constants.RECIPIENT_ID()));
            }
            // filtering participants based on un-subscribed list
            if(!unsubscribedList.isEmpty()) {
                participantDetails.removeIf(participant -> unsubscribedList.contains((String) participant.get(Constants.PARTICIPANT_CODE())) || (participant.get(Constants.PARTICIPANT_CODE())).equals(getProtocolStringValue(Constants.SENDER_CODE(),inputEvent)));
            }
        } else {
            // for targeted notification type
            // fetching subscribed list
            String query = String.format("SELECT " + Constants.RECIPIENT_ID() + " FROM %s WHERE notificationId = '%s' AND status = 1", config.subscriptionTableName, notificationId);
            ResultSet resultSet = postgresConnect.executeQuery(query);
            List<String> subscribedList = new ArrayList<>();
            while(resultSet.next()){
                subscribedList.add(resultSet.getString(Constants.RECIPIENT_ID()));
            }
            participantDetails = new ArrayList<>(registryService.getParticipantDetails("{}"));
            if(!subscribedList.isEmpty()) {
                participantDetails.removeIf(participant -> !subscribedList.contains((String) participant.get(Constants.PARTICIPANT_CODE())));
            }
        }
        consolidatedEvent.put(Constants.PARTICIPANT_DETAILS(),participantDetails);
        context.output(config.dispatcherOutputTag(),consolidatedEvent);
    }

    //FIXME Fetch the notifications master data from the configuration file
    private Map<String,Object> getNotificationMasterData(String notificationId) {
        JSONArray templateData = new JSONArray("[ { \"id\": \"24e975d1-054d-45fa-968e-c91b1043d0a5\", \"name\": \"Organisation status update\", \"description\": \"A notification about the organisation status update in registry. This information will be useful acknowledge the current status of a given organisation and send requests.\", \"sender\": [ \"HIE/HIO.HCX\" ], \"recipient\": [ \"payor\" ], \"type\": \"Broadcast\", \"category\": \"System\", \"trigger\": \"Event\", \"eventType\": [ \"ORGANISATION_STATUS_CHANGE\" ], \"template\": \"{\\\"message\\\": \\\"${participant_name} status changed to ${status}\\\",\\n\\\"participant_code\\\": \\\"${participant_code}\\\", \\\"endpoint_url\\\": \\\"${endpoint_url}\\\"}\", \"status\": \"active\" }, { \"id\": \"e7f6fb71-e19d-4c9e-94a6-d63f2786844f\", \"name\": \"Claim Cycle Completion\", \"description\": \"A notification about the Claim Cycle Completion. This information will be useful for providers.\", \"sender\": [ \"payor\" ], \"recipient\": [ \"provider\" ], \"type\": \"Targeted\", \"category\": \"System\", \"trigger\": \"Event\", \"eventType\": [ \"CYCLE_COMPLETION\" ], \"template\": \"{\\\"message\\\": \\\"${participant_name} has approved claim request with correlation id: ${correlationId}\\\"}\", \"status\": \"active\" }, { \"id\": \"fa55cbb2-53bb-437a-ac72-466af457fa4c\", \"name\": \"Payment Cycle Completion\", \"description\": \"A notification about the Payment Cycle Completion. This information will be useful for payors.\", \"sender\": [ \"payor\" ], \"recipient\": [ \"provider\" ], \"type\": \"Targeted\", \"category\": \"System\", \"trigger\": \"Event\", \"eventType\": [ \"CYCLE_COMPLETION\" ], \"template\": \"{\\\"message\\\": \\\"${participant_name} has approved paymentnotice request with correlation id: ${correlationId}\\\"}\", \"status\": \"active\" }, { \"id\": \"be0e578d-b391-42f9-96f7-1e6bacd91c20\", \"name\": \"payor Downtime\", \"description\": \"A notification about the payor System Downtime. This information will be useful for all participants.\", \"sender\": [ \"payor\" ], \"recipient\": [ \"provider\" ], \"type\": \"Broadcast\", \"category\": \"Business\", \"trigger\": \"Explicit\", \"eventType\": [ \"CYCLE_COMPLETION\" ], \"template\": \"{\\\"message\\\": \\\"${participant_name} system will not be available from ${startTime} for a duration of ${duration} on ${date}\\\",\\n\\\"participant_code\\\": \\\"${participant_code}\\\", \\\"endpoint_url\\\": \\\"${endpoint_url}\\\"}\", \"status\": \"active\" } ]");
        for(Object data: templateData) {
            JSONObject obj = (JSONObject) data;
            if(obj.get("id").equals(notificationId)){
                return JSONUtil.deserialize(obj.toString(), Map.class);
            }
        }
        return new HashMap<>();
    }

    private String resolveTemplate(Map<String, Object> notificationMasterData,Map<String,Object> event) {
        Map<String,Object> notificationData = getProtocolMapValue(Constants.NOTIFICATION_DATA(),event);
        StringSubstitutor sub = new StringSubstitutor(notificationData);
        return sub.replace((JSONUtil.deserialize((String) notificationMasterData.get(Constants.TEMPLATE()), Map.class)).get(Constants.MESSAGE()));
    }

    private String getProtocolStringValue(String key,Map<String,Object> event) {
        return (String) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, "");
    }

    private Map<String,Object> getProtocolMapValue(String key,Map<String,Object> event) {
        return (Map<String,Object>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new HashMap<>());
    }

}
