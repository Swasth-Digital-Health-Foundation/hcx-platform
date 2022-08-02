package org.swasth.dp.notification.trigger.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.*;
import org.swasth.dp.notification.trigger.task.NotificationTriggerConfig;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationTriggerProcessFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(NotificationTriggerProcessFunction.class);
    private NotificationTriggerConfig config;
    private NotificationUtil notificationUtil;
    private RegistryService registryService;
    private PostgresConnect postgresConnect;

    public NotificationTriggerProcessFunction(NotificationTriggerConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        registryService = new RegistryService(config);
        postgresConnect = new PostgresConnect(new PostgresConnectionConfig(config.postgresUser(), config.postgresPassword(), config.postgresDb(), config.postgresHost(), config.postgresPort(), config.postgresMaxConnections()));
        postgresConnect.getConnection();
        notificationUtil = new NotificationUtil();
    }

    @Override
    public void close() throws Exception {
        super.close();
        postgresConnect.closeConnection();
    }

    @Override
    public void processElement(Map<String, Object> event,
                               ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context,
                               Collector<Map<String, Object>> collector) throws Exception {

        System.out.println("Audit Event :: " + event);
        logger.debug("Audit Event :: " + event);
        Map<String, Object> cdata = (Map<String, Object>) event.get(Constants.CDATA());
        String action = (String) cdata.get(Constants.ACTION());
        String id = (String) ((Map<String, Object>) event.get(Constants.OBJECT())).get(Constants.ID());
        String notifyEvent = null;
        // Network notifications
        if (config.networkNotificationsEnabled && action.equals(Constants.PARTICIPANT_CREATE())) {
            Map<String, Object> notification = notificationUtil.getNotification(Constants.PARTICIPANT_ONBOARD());
            Map<String, Object> participant = registryService.getParticipantDetails("{\"participant_code\":{\"eq\":\"" + id + "\"}}").get(0);
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put(Constants.PARTICIPANT_NAME(), participant.get(Constants.PARTICIPANT_NAME()));
            notificationData.put(Constants.HCX_NAME(), config.hcxInstanceName());
            notificationData.put(Constants.DDMMYY(), new SimpleDateFormat("dd-MM-yyyy").format(event.get(Constants.ETS())));
            notificationData.put(Constants.PARTICIPANT_NAME(), participant.get(Constants.PARTICIPANT_NAME()));
            notificationData.put(Constants.PARTICIPANT_CODE(), participant.get(Constants.PARTICIPANT_CODE()));
            notifyEvent = createNotifyEvent((String) notification.get(Constants.TOPIC_CODE()), Collections.emptyList(),
                    (List<String>) notification.get(Constants.ALLOWED_RECIPIENTS()), Collections.emptyList(), notificationData);
            context.output(config.notifyOutputTag, notifyEvent);
        } else if(action.equals(Constants.NOTIFICATION_SUBSCRIPTION_UPDATE())) {
            Map<String, Object> notification = notificationUtil.getNotification(Constants.SUBSCRIPTION_UPDATE());
            Map<String, Object> participant = registryService.getParticipantDetails("{\"participant_code\":{\"eq\":\"" + cdata.get(Constants.RECIPIENT_CODE()) + "\"}}").get(0);
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put(Constants.PARTICIPANT_NAME(), participant.get(Constants.PARTICIPANT_NAME()));
            notificationData.put(Constants.SUBSCRIPTION_ID(), id);
            notifyEvent = createNotifyEvent((String) notification.get(Constants.TOPIC_CODE()), Arrays.asList((String) cdata.get(Constants.RECIPIENT_CODE())),
                    Collections.emptyList(), Collections.emptyList(), notificationData);
            context.output(config.notifyOutputTag, notifyEvent);
        }
        // Workflow notifications
        String topicCode = (String) config.topicCodeAndAPIActionMap.get(action);
        if (!config.notificationTriggersDisabled.contains(topicCode)){
            if (action.contains("on_") && cdata.get(Constants.HCX_STATUS()).equals(Constants.COMPLETE_RESPONSE()))
                processWorkflowNotification(context, cdata, topicCode, notifyEvent);
            else if (!action.contains("on_"))
                processWorkflowNotification(context, cdata, topicCode, notifyEvent);
        }
        if (notifyEvent != null)
            System.out.println("Notify event is pushed to kafka :: Event ::" + notifyEvent);
            logger.debug("Notify event is pushed to kafka :: Event :: " + notifyEvent);
    }

    private void processWorkflowNotification(ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Map<String, Object> cdata, String topicCode, String notifyEvent) throws Exception {
        String senderCode = (String) cdata.get(Constants.HCX_SENDER_CODE());
        List<String> subscriptions = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            String query = String.format("SELECT %s from %s WHERE %s = '%s' AND %s = '%s' AND %s = 1", Constants.SUBSCRIPTION_ID(),
                    config.subscriptionTableName, Constants.SENDER_CODE(), senderCode, Constants.TOPIC_CODE(), topicCode, Constants.SUBSCRIPTION_STATUS());
            resultSet = postgresConnect.executeQuery(query);
            while (resultSet.next()) {
                subscriptions.add(resultSet.getString(Constants.SUBSCRIPTION_ID()));
            }
        } finally {
            if (resultSet != null) resultSet.close();
        }
        if (!subscriptions.isEmpty()) {
            Map<String, Object> participant = registryService.getParticipantDetails("{\"participant_code\":{\"eq\":\"" + senderCode + "\"}}").get(0);
            notifyEvent = createNotifyEvent(topicCode, Collections.emptyList(), Collections.emptyList(),
                    subscriptions, Collections.singletonMap(Constants.PARTICIPANT_NAME(), participant.get(Constants.PARTICIPANT_NAME())));
            context.output(config.notifyOutputTag, notifyEvent);
        }
    }

    public String createNotifyEvent(String topicCode, List<String> recipientCodes, List<String> recipientRoles,
                                    List<String> subscriptions, Map<String,Object> notificationData) throws Exception {
        Map<String,Object> event = new HashMap<>();
        event.put(Constants.TOPIC_CODE(), topicCode);
        event.put(Constants.RECIPIENT_CODES(), recipientCodes);
        event.put(Constants.RECIPIENT_ROLES(), recipientRoles);
        event.put(Constants.SUBSCRIPTIONS(), subscriptions);
        event.put(Constants.NOTIFICATION_DATA(), notificationData);
        return JSONUtil.serialize(event);
    }

}
