package org.swasth.dp.notification.trigger.functions;

import org.apache.commons.text.StringSubstitutor;
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
    private final NotificationTriggerConfig config;
    private NotificationUtil notificationUtil;
    private RegistryService registryService;
    private PostgresConnect postgresConnect;
    private JWTUtil jwtUtil;

    public NotificationTriggerProcessFunction(NotificationTriggerConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        registryService = new RegistryService(config);
        postgresConnect = new PostgresConnect(new PostgresConnectionConfig(config.postgresUser(), config.postgresPassword(), config.postgresDb(), config.postgresHost(), config.postgresPort(), config.postgresMaxConnections()));
        postgresConnect.getConnection();
        notificationUtil = new NotificationUtil();
        jwtUtil = new JWTUtil(config);
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
        Map<String, Object> edata = (Map<String, Object>) event.get(Constants.EDATA());
        String action = (String) cdata.get(Constants.ACTION());
        String id = (String) ((Map<String, Object>) event.get(Constants.OBJECT())).get(Constants.ID());
        String topicCode = (String) config.apiActionAndTopicCodeMap.get(action);
        String notifyEvent = null;
        if (!config.notificationTriggersDisabled.contains(topicCode)) {
            // Network notifications
            if (config.networkNotificationsEnabled) {
                if (action.equals(Constants.PARTICIPANT_CREATE()) || action.equals(Constants.PARTICIPANT_DELETE())) {
                    Map<String, Object> notification = notificationUtil.getNotification(topicCode);
                    Map<String, Object> nData = new HashMap<>();
                    nData.put(Constants.PARTICIPANT_NAME(), cdata.get(Constants.PARTICIPANT_NAME()));
                    nData.put(Constants.HCX_NAME(), config.hcxInstanceName());
                    nData.put(Constants.DDMMYY(), new SimpleDateFormat("dd-MM-yyyy").format(event.get(Constants.ETS())));
                    nData.put(Constants.PARTICIPANT_NAME(), cdata.get(Constants.PARTICIPANT_NAME()));
                    nData.put(Constants.PARTICIPANT_CODE(), cdata.get(Constants.PARTICIPANT_CODE()));
                    String message = resolveTemplate(notification, nData);
                    notifyEvent = createNotifyEvent((String) notification.get(Constants.TOPIC_CODE()), config.hcxRegistryCode(), Constants.PARTICIPANT_ROLE(),
                            (List<String>) notification.get(Constants.ALLOWED_RECIPIENTS()), message);
                    pushToKafka(context, notifyEvent);
                } else if (action.equals(Constants.NOTIFICATION_SUBSCRIPTION_UPDATE())) {
                    Map<String, Object> notification = notificationUtil.getNotification(topicCode);
                    Map<String, Object> participant = registryService.getParticipantDetails("{\"participant_code\":{\"eq\":\"" + cdata.get(Constants.RECIPIENT_CODE()) + "\"}}").get(0);
                    Map<String, Object> nData = new HashMap<>();
                    nData.put(Constants.PARTICIPANT_NAME(), participant.get(Constants.PARTICIPANT_NAME()));
                    nData.put(Constants.SUBSCRIPTION_ID(), id);
                    nData.put(Constants.PROPERTIES(), edata.get(Constants.PROPS()));
                    String message = resolveTemplate(notification, nData);
                    notifyEvent = createNotifyEvent((String) notification.get(Constants.TOPIC_CODE()), config.hcxRegistryCode(), Constants.PARTICIPANT_CODE(),
                            Arrays.asList((String) cdata.get(Constants.RECIPIENT_CODE())), message);
                    pushToKafka(context, notifyEvent);
                }
            }
            // Workflow notifications
            if (config.workflowNotificationEnabled && config.workflowNotificationAllowedEntities.contains(getEntity(action))) {
                if (action.contains("on_") && config.workflowNotificationAllowedStatus.contains((String) cdata.get(Constants.HCX_STATUS()))) {
                    processWorkflowNotification(context, cdata, action);
                } else if (!action.contains("on_")) {
                    cdata.put(Constants.HCX_STATUS(), Constants.REQUEST_INITIATED());
                    processWorkflowNotification(context, cdata, action);
                }
            }
        }
    }

    private void processWorkflowNotification(ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Map<String, Object> cdata, String action) throws Exception {
        String topicCode = config.workflowUpdateTopicCode;
        String senderCode = (String) cdata.get(Constants.HCX_SENDER_CODE());
        String notifyEvent;
        List<String> subscriptions = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            String query = String.format("SELECT %s from %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s'", Constants.SUBSCRIPTION_ID(),
                    config.subscriptionTableName, Constants.SENDER_CODE(), senderCode, Constants.TOPIC_CODE(), topicCode, Constants.SUBSCRIPTION_STATUS(), Constants.ACTIVE());
            resultSet = postgresConnect.executeQuery(query);
            while (resultSet.next()) {
                subscriptions.add(resultSet.getString(Constants.SUBSCRIPTION_ID()));
            }
        } finally {
            if (resultSet != null) resultSet.close();
        }
        if (!subscriptions.isEmpty()) {
            Map<String, Object> participant = registryService.getParticipantDetails("{\"participant_code\":{\"eq\":\"" + senderCode + "\"}}").get(0);
            Map<String, Object> nData = new HashMap<>();
            nData.put(Constants.PARTICIPANT_NAME(), participant.get(Constants.PARTICIPANT_NAME()));
            nData.put(Constants.ENTITY_TYPE(), getEntity(action));
            nData.put(Constants.CORRELATIONID(), cdata.get(Constants.HCX_CORRELATION_ID()));
            nData.put(Constants.STATUS(), cdata.get(Constants.HCX_STATUS()));
            String message = resolveTemplate(notificationUtil.getNotification(topicCode), nData);
            notifyEvent = createNotifyEvent(topicCode, config.hcxRegistryCode(), Constants.SUBSCRIPTION(), subscriptions, message);
            pushToKafka(context, notifyEvent);
        }
    }

    private void pushToKafka(ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, String notifyEvent) {
        if (notifyEvent != null) {
            context.output(config.notifyOutputTag, notifyEvent);
            System.out.println("Notify event is pushed to kafka :: " + notifyEvent);
            logger.debug("Notify event is pushed to kafka :: " + notifyEvent);
        }
    }

    public String createNotifyEvent(String topicCode, String senderCode, String recipientType, List<String> recipients, String message) throws Exception {
        Map<String,Object> notificationHeaders = new HashMap<>();
        notificationHeaders.put(Constants.SENDER_CODE(), senderCode);
        notificationHeaders.put(Constants.TIMESTAMP(), System.currentTimeMillis());
        notificationHeaders.put(Constants.RECIPIENT_TYPE(), recipientType);
        notificationHeaders.put(Constants.RECIPIENTS(), recipients);
        notificationHeaders.put(Constants.HCX_CORRELATION_ID(), UUID.randomUUID().toString());
        notificationHeaders.put(Constants.EXPIRY(), new Date(new Date().getTime() + config.notificationExpiry).getTime());

        Map<String,Object> protocolHeaders = new HashMap<>();
        protocolHeaders.put(Constants.ALG(), Constants.RS256());
        protocolHeaders.put(Constants.NOTIFICATION_HEADERS(), notificationHeaders);

        Map<String,Object> payload = new HashMap<>();
        payload.put(Constants.TOPIC_CODE(), topicCode);
        payload.put(Constants.MESSAGE(), message);

        Map<String,Object> event = new HashMap<>();
        event.put(Constants.MID(), UUID.randomUUID().toString());
        event.put(Constants.ETS(), System.currentTimeMillis());
        event.put(Constants.ACTION(), Constants.NOTIFICATION_NOTIFY());
        event.put(Constants.TOPIC_CODE(), topicCode);
        event.put(Constants.MESSAGE(), message);
        event.put(Constants.PAYLOAD(), jwtUtil.generateJWS(protocolHeaders, payload));
        event.put(Constants.HEADERS(), Collections.singletonMap(Constants.PROTOCOL(), protocolHeaders));

        return JSONUtil.serialize(event);
    }


    private String resolveTemplate(Map<String, Object> notification, Map<String,Object> nData) {
        StringSubstitutor sub = new StringSubstitutor(nData);
        return sub.replace((JSONUtil.deserialize((String) notification.get(Constants.TEMPLATE()), Map.class)).get(Constants.MESSAGE()));
    }

    public String getEntity(String action) {
        if (action.contains("status")) {
            return "status";
        } else if (action.contains("search")) {
            return "search";
        } else {
            String[] str = action.split("/");
            return str[str.length-2];
        }
    }

}
