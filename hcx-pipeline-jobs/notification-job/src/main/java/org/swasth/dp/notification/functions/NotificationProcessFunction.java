package org.swasth.dp.notification.functions;


import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ErrorResponse;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.*;
import org.swasth.dp.notification.dto.ErrorDetails;
import org.swasth.dp.notification.task.NotificationConfig;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationProcessFunction extends ProcessFunction<Map<String,Object>, Object> {

    private final Logger logger = LoggerFactory.getLogger(NotificationProcessFunction.class);
    private NotificationConfig config;
    private RegistryService registryService;
    private PostgresConnect postgresConnect;
    private DispatcherUtil dispatcherUtil;
    private AuditService auditService;
    private Map<String,Object> event;

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
    public void processElement(Map<String,Object> inputEvent, ProcessFunction<Map<String,Object>, Object>.Context context, Collector<Object> collector) throws Exception {
        event = inputEvent;
        String notificationId = getProtocolStringValue(Constants.NOTIFICATION_ID());
        Map<String,Object> notificationMasterData = getNotificationMasterData(notificationId);
        System.out.println("Notification Master data template: " + notificationMasterData);
        String notificationType = (String) notificationMasterData.get(Constants.TYPE());
        // resolving notification message template
        String resolvedTemplate = resolveTemplate(notificationMasterData);
        // for broadcast notification type
        if(notificationType.equalsIgnoreCase(Constants.BROADCAST())) {
            // fetching participants based on the master data roles
            List<String> masterRoles = (ArrayList<String>) notificationMasterData.get(Constants.RECIPIENT());
            List<Map<String,Object>> participantDetails = new ArrayList<>();
            for(String role: masterRoles) {
                List<Map<String,Object>> fetchParticipants = registryService.getParticipantDetails("{\"roles\":{\"eq\":\"" + role + "\"}}");
                if(!fetchParticipants.isEmpty())
                  participantDetails.addAll(fetchParticipants);
            }
            System.out.println("Total number of participants: " + participantDetails.size());
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

    private void notificationDispatcher(Map<String, Object> notificationMasterData, String resolvedTemplate, List<Map<String, Object>> participantDetails) throws Exception {
        List<Object> dispatchResult = new ArrayList<>();
        int successfulDispatches = 0;
        int failedDispatches = 0;
        for(Map<String,Object> participant: participantDetails) {
            String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE());
            String endpointUrl = (String) participant.get(Constants.END_POINT());
            if (!(participantCode).contains("null") && !(endpointUrl).contains("null")) {
                participant.put(Constants.END_POINT(), endpointUrl + event.get(Constants.ACTION()));
                String payload = getPayload(resolvedTemplate, participantCode, notificationMasterData);
                System.out.println("Recipient Id: " + participantCode + " :: Notification payload: " + payload);
                DispatcherResult result = dispatcherUtil.dispatch(participant, payload);
                dispatchResult.add(JSONUtil.serialize(new ErrorDetails(participantCode, result.success(), createErrorMap(result))));
                if(result.success()) successfulDispatches++; else failedDispatches++;
            }
        }
        int totalDispatches = successfulDispatches+failedDispatches;
        System.out.println("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        Map<String,Object> dispatchResultDetails = new HashMap<>();
        dispatchResultDetails.put(Constants.TOTAL_DISPATCHES(), totalDispatches);
        dispatchResultDetails.put(Constants.SUCCESSFUL_DISPATCHES(), successfulDispatches);
        dispatchResultDetails.put(Constants.FAILED_DISPATCHES(), failedDispatches);
        dispatchResultDetails.put(Constants.RESULT_DETAILS(), dispatchResult);
        event.put(Constants.NOTIFICATION_DISPATCH_RESULT(), dispatchResultDetails);
        auditService.indexAudit(createNotificationAuditEvent());
    }

    private String resolveTemplate(Map<String, Object> notificationMasterData) {
        Map<String,Object> notificationData = getProtocolMapValue(Constants.NOTIFICATION_DATA());
        StringSubstitutor sub = new StringSubstitutor(notificationData);
        return sub.replace((JSONUtil.deserialize((String) notificationMasterData.get(Constants.TEMPLATE()), Map.class)).get(Constants.MESSAGE()));
    }

    private String getPayload(String notificationMessage, String recipientCode, Map<String,Object> notificationMasterData) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.SENDER_CODE(), config.hcxRegistryCode());
        request.put(Constants.RECIPIENT_CODE(), recipientCode);
        request.put(Constants.API_CALL_ID(), UUID.randomUUID());
        request.put(Constants.CORRELATION_ID(), getProtocolStringValue(Constants.CORRELATION_ID()));
        request.put(Constants.TIMESTAMP(), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
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
        audit.put(Constants.ACTION(), event.get(Constants.ACTION()));
        audit.put(Constants.STATUS(), getProtocolStringValue(Constants.STATUS()));
        audit.put(Constants.REQUESTED_TIME(), event.get(Constants.ETS()));
        audit.put(Constants.UPDATED_TIME(), event.getOrDefault(Constants.UPDATED_TIME(), Calendar.getInstance().getTime()));
        audit.put(Constants.AUDIT_TIMESTAMP(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_ROLE(), getProtocolStringValue(Constants.SENDER_ROLE()).equals(config.hcxRegistryCode())? Collections.singletonList("HIE/HIO.HCX") : Collections.emptyList());
        audit.put(Constants.RECIPIENT_ROLE(), event.getOrDefault(Constants.RECIPIENT_ROLE(), Collections.emptyList()));
        audit.put(Constants.NOTIFICATION_ID(), getProtocolStringValue(Constants.NOTIFICATION_ID()));
        audit.put(Constants.NOTIFICATION_DATA(), getProtocolMapValue(Constants.NOTIFICATION_DATA()));
        audit.put(Constants.NOTIFICATION_DISPATCH_RESULT(), event.get(Constants.NOTIFICATION_DISPATCH_RESULT()));
        audit.put(Constants.PAYLOAD(), "");
        return audit;
    }

    private Map<String,Object> createErrorMap(DispatcherResult result){
        Map<String,Object> errorMap = new HashMap<>();
        if (result.error() != null) {
            ErrorResponse error = result.error().get();
            errorMap.put(Constants.CODE(), error.code().get());
            errorMap.put(Constants.MESSAGE(), error.message().get());
            errorMap.put(Constants.TRACE(), error.trace().get());
        }
        return errorMap;
    }

}
