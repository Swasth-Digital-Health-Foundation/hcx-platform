package org.swasth.dp.notification.functions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.joda.time.DateTime;
import org.swasth.dp.core.function.ErrorResponse;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.service.RegistryService;
import org.swasth.dp.core.util.*;
import org.swasth.dp.notification.task.NotificationConfig;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseNotificationFunction extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    protected NotificationConfig config;
    protected AuditService auditService;
    protected RegistryService registryService;
    protected PostgresConnect postgresConnect;
    protected DispatcherUtil dispatcherUtil;

    public BaseNotificationFunction(NotificationConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) {
        auditService = new AuditService(config);
        registryService = new RegistryService(config);
        dispatcherUtil = new DispatcherUtil(config);
        postgresConnect = new PostgresConnect(new PostgresConnectionConfig(config.postgresUser(), config.postgresPassword(), config.postgresDb(), config.postgresHost(), config.postgresPort(), config.postgresMaxConnections()));
        postgresConnect.getConnection();
    }

    @Override
    public void close() throws Exception {
        super.close();
        postgresConnect.closeConnection();
    }

    protected String addQuotes(List<String> list){
        return list.stream().map(plain ->  StringUtils.wrap(plain, "\"")).collect(Collectors.joining(","));
    }

    protected String resolveTemplate(Map<String, Object> notification, Map<String,Object> event) {
        StringSubstitutor sub = new StringSubstitutor(getProtocolMapValue(Constants.NOTIFICATION_DATA(), event));
        return sub.replace((JSONUtil.deserialize((String) notification.get(Constants.TEMPLATE()), Map.class)).get(Constants.MESSAGE()));
    }

    protected String getProtocolStringValue(String key,Map<String,Object> event) {
        return (String) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, "");
    }

    protected Map<String,Object> getProtocolMapValue(String key,Map<String,Object> event) {
        return (Map<String,Object>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new HashMap<>());
    }

    protected List<String> getProtocolListValue(String key,Map<String,Object> event) {
        return (List<String>) ((Map<String,Object>) ((Map<String,Object>) event.get(Constants.HEADERS())).get(Constants.PROTOCOL())).getOrDefault(key, new ArrayList<>());
    }

    protected boolean isExpired(Long expiryTime){
        return new DateTime(expiryTime).isBefore(DateTime.now());
    }

    protected Map<String,Object> createErrorMap(ErrorResponse error){
        Map<String,Object> errorMap = new HashMap<>();
        if (error != null) {
            errorMap.put(Constants.CODE(), error.code().get());
            errorMap.put(Constants.MESSAGE(), error.message().get());
            errorMap.put(Constants.TRACE(), error.trace().get());
        }
        return errorMap;
    }

    protected Map<String,Object> createNotificationAuditEvent(Map<String,Object> event, String recipientCode, Map<String,Object> errorDetails){
        Map<String,Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.MID(), UUID.randomUUID().toString());
        audit.put(Constants.ACTION(), event.get(Constants.ACTION()));
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_CODE(), getProtocolStringValue(Constants.SENDER_CODE(),event));
        audit.put(Constants.RECIPIENT_CODE(), recipientCode);
        audit.put(Constants.NOTIFICATION_REQ_ID(), event.get(Constants.NOTIFICATION_REQ_ID()));
        audit.put(Constants.TOPIC_CODE(), getProtocolStringValue(Constants.TOPIC_CODE(),event));
        if(!errorDetails.isEmpty()) {
            audit.put(Constants.ERROR_DETAILS(), errorDetails);
            audit.put(Constants.STATUS(), Constants.ERROR_STATUS());
        } else {
            audit.put(Constants.STATUS(), Constants.DISPATCH_STATUS());
        }
        return audit;
    }

    protected Map<String, Object> createSubscriptionAuditEvent(String action, String topicCode, String recipientCode, String senderCode, String status) {
        Map<String, Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.MID(), UUID.randomUUID().toString());
        audit.put(Constants.ACTION(), action);
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_CODE(), senderCode);
        audit.put(Constants.RECIPIENT_CODE(), recipientCode);
        audit.put(Constants.TOPIC_CODE(), topicCode);
        audit.put(Constants.STATUS(), status);
        return audit;
    }

    protected Map<String, Object> createOnSubscriptionAuditEvent(Map<String,Object> eventMap, Map<String,Object> errorDetails,String hcxStatus) {
        Map<String, Object> audit = new HashMap<>();
        audit.put(Constants.EID(), Constants.AUDIT());
        audit.put(Constants.MID(), UUID.randomUUID().toString());
        audit.put(Constants.ACTION(), (String) eventMap.get(Constants.ACTION()));
        audit.put(Constants.ETS(), Calendar.getInstance().getTime());
        audit.put(Constants.SENDER_CODE(), (String) eventMap.get(Constants.HCX_SENDER_CODE()));
        audit.put(Constants.RECIPIENT_CODE(), (String) eventMap.get(Constants.HCX_RECIPIENT_CODE()));
        audit.put(Constants.SUBSCRIPTION_ID(), (String) ((Map) eventMap.get(Constants.PAYLOAD())).get(Constants.SUBSCRIPTION_ID()));
        audit.put(Constants.SUBSCRIPTION_STATUS(), (Double) ((Map) eventMap.get(Constants.PAYLOAD())).get(Constants.SUBSCRIPTION_STATUS()));
        audit.put(Constants.HCX_STATUS(), hcxStatus);
        if(!errorDetails.isEmpty()) {
            audit.put(Constants.ERROR_DETAILS(), errorDetails);
            audit.put(Constants.STATUS(), Constants.ERROR_STATUS());
        } else {
            audit.put(Constants.STATUS(), Constants.DISPATCH_STATUS());
        }
        return audit;
    }
}
