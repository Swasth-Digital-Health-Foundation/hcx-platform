package org.swasth.dp.notification.functions;

import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.elasticsearch.common.recycler.Recycler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.*;
import org.swasth.dp.notification.task.NotificationConfig;

import java.sql.ResultSet;
import java.util.*;

public class NotificationProcessFunction extends ProcessFunction<Map<String,Object>, Object> {

    private final Logger logger = LoggerFactory.getLogger(NotificationProcessFunction.class);
    private final NotificationConfig config;
    private final RegistryService registryService;
    private final PostgresConnect postgresConnect;
    private final DispatcherUtil dispatcherUtil;
    private final AuditService auditService;
    private Map<String,Object> event;

    public NotificationProcessFunction(NotificationConfig config) {
        this.config = config;
        this.registryService = new RegistryService(config);
        this.postgresConnect = new PostgresConnect(new PostgresConnectionConfig(config.postgresUser(), config.postgresPassword(), config.postgresDb(), config.postgresHost(), config.postgresPort(), config.postgresMaxConnections()));
        this.dispatcherUtil = new DispatcherUtil(config);
        this.auditService = new AuditService(config);
    }

    @Override
    public void open(Configuration parameters) {
        postgresConnect.getConnection();
    }

    @Override
    public void close() throws Exception {
        super.close();
        postgresConnect.closeConnection();
    }

    @Override
    public void processElement(Map<String,Object> inputEvent, ProcessFunction<Map<String,Object>, Object>.Context context, Collector<Object> collector) throws Exception {
        event = inputEvent;
        String notificationId = getProtocolStringValue(Constants.NOTIFICATION_ID());
        Map<String,Object> notificationMasterData = new HashMap<>();
        String notificationType = (String) notificationMasterData.get(Constants.TYPE());
        // resolving notification message template
        String resolvedTemplate = resolveTemplate(notificationMasterData);
        // for broadcast notification type
        if(notificationType.equalsIgnoreCase(Constants.BROADCAST())) {
            // fetching participants based on the master data roles
            List<String> masterRoles = (ArrayList<String>) notificationMasterData.get(Constants.RECIPIENT());
            List<Map<String,Object>> participantDetails = new ArrayList<>();
            for(String role: masterRoles) {
                participantDetails.addAll(registryService.getParticipantDetails("{\"roles\":{\"eq\":\"" + role + "\"}}"));
            }
            // fetching unsubscribed list
            String query = String.format("SELECT " + Constants.RECIPIENT_ID() + " FROM %s WHERE notificationId = '%s' AND status = 0", config.subscriptionTableName, notificationId);
            ResultSet resultSet = postgresConnect.executeQuery(query);
            List<String> unsubscribedList = new ArrayList<>();
            while(resultSet.next()){
                unsubscribedList.add(resultSet.getString(Constants.RECIPIENT_ID()));
            }
            // filtering participants based on un-subscribed list
            if(!unsubscribedList.isEmpty()) {
                participantDetails.removeIf(participant -> unsubscribedList.contains((String) participant.get(Constants.PARTICIPANT_CODE())) || (participant.get(Constants.PARTICIPANT_CODE())).equals(getProtocolStringValue(Constants.SENDER_CODE())));
            }
            notificationDispatcher(notificationMasterData, resolvedTemplate, participantDetails);
        } else {
            // for targeted notification type
            // fetching subscribed list
            String query = String.format("SELECT " + Constants.RECIPIENT_ID() + " FROM %s WHERE notificationId = '%s' AND status = 1", config.subscriptionTableName, notificationId);
            ResultSet resultSet = postgresConnect.executeQuery(query);
            List<String> subscribedList = new ArrayList<>();
            while(resultSet.next()){
                subscribedList.add(resultSet.getString(Constants.RECIPIENT_ID()));
            }
            List<Map<String, Object>> participantDetails = new ArrayList<>(registryService.getParticipantDetails("{}"));
            if(!subscribedList.isEmpty()) {
                participantDetails.removeIf(participant -> !subscribedList.contains((String) participant.get(Constants.PARTICIPANT_CODE())));
            }
            notificationDispatcher(notificationMasterData, resolvedTemplate, participantDetails);
        }
    }

    private void notificationDispatcher(Map<String, Object> notificationMasterData, String resolvedTemplate, List<Map<String, Object>> participantDetails) throws Exception {
        Map<String,Object> dispatchResult = new HashMap<>();
        for(Map<String,Object> participant: participantDetails) {
            participant.put(Constants.END_POINT(), participant.get(Constants.END_POINT() + config.notificationDispatchAPI));
            String payload = getPayload(resolvedTemplate, (String) participant.get(Constants.PARTICIPANT_CODE()), notificationMasterData);
            System.out.println("Recipient Id: " + participant.get(Constants.PARTICIPANT_CODE()) + "Notification payload: " + payload);
            DispatcherResult result = dispatcherUtil.dispatch(participant, payload);
            dispatchResult.put((String) participant.get(Constants.PARTICIPANT_CODE()), result);
        }
        event.put(Constants.NOTIFICATION_DISPATCH_RESULT(), dispatchResult);
        auditService.indexAudit(createNotificationAuditEvent());
    }

    private String resolveTemplate(Map<String, Object> notificationMasterData) {
        Map<String,Object> notificationData = getProtocolMapValue(Constants.NOTIFICATION_DATA());
        StringSubstitutor sub = new StringSubstitutor(notificationData);
        return sub.replace(((Map<String, Object>) notificationMasterData.get(Constants.TEMPLATE())).get(Constants.MESSAGE()));
    }

    private String getPayload(String notificationMessage, String recipientCode, Map<String,Object> notificationMasterData) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.SENDER_CODE(), config.hcxRegistryCode());
        request.put(Constants.RECIPIENT_CODE(), recipientCode);
        request.put(Constants.API_CALL_ID(), UUID.randomUUID());
        request.put(Constants.CORRELATION_ID(), getProtocolStringValue(Constants.CORRELATION_ID()));
        request.put(Constants.TIMESTAMP(), DateTime.now());
        request.put(Constants.NOTIFICATION_DATA(), Collections.singletonMap(Constants.MESSAGE(), notificationMessage));
        request.put(Constants.NOTIFICATION_TITLE(), notificationMasterData.get(Constants.NAME()));
        request.put(Constants.NOTIFICATION_DESC(), notificationMasterData.get(Constants.DESCRIPTION()));
        if(!getProtocolStringValue(Constants.WORKFLOW_ID()).isEmpty())
          request.put(Constants.WORKFLOW_ID(), getProtocolStringValue(Constants.WORKFLOW_ID()));
        return JSONUtil.serialize(request);
    }

    private String getProtocolStringValue(String key) {
        return (String) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, "");
    }

    private Map<String,Object> getProtocolMapValue(String key) {
        return (Map<String,Object>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new HashMap<>());
    }

    private Map<String,Object> createNotificationAuditEvent(){
        Map<String,Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.SENDER_CODE(), getProtocolStringValue(Constants.SENDER_CODE()));
        audit.put(Constants.RECIPIENT_CODE(), getProtocolStringValue(Constants.RECIPIENT_CODE()));
        audit.put(Constants.API_CALL_ID(), getProtocolStringValue(Constants.API_CALL_ID()));
        audit.put(Constants.CORRELATION_ID(), getProtocolStringValue(Constants.CORRELATION_ID()));
        audit.put(Constants.WORKFLOW_ID(), getProtocolStringValue(Constants.WORKFLOW_ID()));
        audit.put(Constants.TIMESTAMP(), getProtocolStringValue(Constants.TIMESTAMP()));
        audit.put(Constants.MID(), event.get(Constants.MID()));
        audit.put(Constants.ACTION(), config.notificationDispatchAPI);
        audit.put(Constants.STATUS(), getProtocolStringValue(Constants.STATUS()));
        audit.put(Constants.REQUESTED_TIME(), event.get(Constants.ETS()));
        audit.put(Constants.UPDATED_TIME(), event.getOrDefault(Constants.UPDATED_TIME(), System.currentTimeMillis()));
        audit.put(Constants.AUDIT_TIMESTAMP(), System.currentTimeMillis());
        audit.put(Constants.SENDER_ROLE(), getProtocolStringValue(Constants.SENDER_ROLE()).equals(config.hcxRegistryCode())?"HIE/HIO.HCX":"");
        audit.put(Constants.RECIPIENT_ROLE(), event.getOrDefault(Constants.RECIPIENT_ROLE(), ""));
        audit.put(Constants.NOTIFICATION_ID(), getProtocolStringValue(Constants.NOTIFICATION_ID()));
        audit.put(Constants.NOTIFICATION_DATA(), getProtocolStringValue(Constants.NOTIFICATION_DATA()));
        audit.put(Constants.NOTIFICATION_DISPATCH_RESULT(), event.get(Constants.NOTIFICATION_DISPATCH_RESULT()));
        audit.put(Constants.PAYLOAD(), "");
        return audit;
    }

}
