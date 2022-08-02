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

    private Map<String,Object> getNotification(String topicCode) {
        //TODO revisit to read from configuration file
        JSONArray templateData = new JSONArray("[ { \"topic_code\": \"notif-participant-onboarded\", \"title\": \"Participant Onboarding\", \"description\": \"Notification about new participant joining the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"Event\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has been successfully onboarded on ${HCX name} on $(DDMMYYYY). Transactions relating ${participant_name} can be initiated using ${participant_code}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-de-boarded\", \"title\": \"Participant De-boarding\", \"description\": \"Notification about new participant leaving the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"Event\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has been deboarded from ${HCX name} on $(DDMMYYYY). Platform will not support the transaction relating ${participant_name}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-new-network-feature-added\", \"title\": \"New Feature Support\", \"description\": \"Notification about new feature launch for the participants on the network.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${HCX name} now supports $(feature_code) on its platform. All participants can now initiate transactions relationg to $(feature_code).\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-network-feature-removed\", \"title\": \"End of support for old feature\", \"description\": \"Notification about removing an old feature for the participants on the network.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${HCX name} now does not support $(feature_code) on its platform.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-protocol-version-support-ended\", \"title\": \"End of support for old protocol version\", \"description\": \"Notification about ending support for an older version of the protocol by the HCX.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${HCX name} now does not support $(version_code) on its platform. All participants are requested to upgrade to $(version_code) or above to transact on $(HCX_name).\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-gateway-downtime\", \"title\": \"Network maintenance/Downtime\", \"description\": \"Notification about planned downtime/maintenance of the gateway switch.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${HCX name} will be facing downtime from $(DDMMYYYY) to $(DDMMYYYY) due to planned maintenance. Sorry for inconvenience and please plan your operations accordingly.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-gateway-policy-sla-change\", \"title\": \"Policy change - SLA\", \"description\": \"Notification about the policy changes about the SLAs for the participant in the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${HCX name} is changing the SLA policy for the $(Usecase_code) going forward. Please plan your operations accordingly.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-policy-security-update\", \"title\": \"Policy change - Security & Privacy\", \"description\": \"Notification about the data security & privacy standards for the participant in the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Network\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${HCX name} is now compliant with latest FHIR security and privacy protocols. Please update security and privacy  protocols accordingly.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-compliance-expiry\", \"title\": \"Compliance expiry\", \"description\": \"Notification about the compliance certificate expiration for the participant in the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Network\", \"trigger\": \"Event\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} compliance certificate will be expiring on $(DDMMYYYY). Please renew your compliance certificate before $(DDMMYYYY).\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-subscription-expiry\", \"title\": \"Subscription expiry/renew\", \"description\": \"Notification about the notification subscription expiration for the participant in the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Network\", \"trigger\": \"Event\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} notification subscription to ${participant_name} will be expiring on $(DDMMYYYY). Please renew your subscription before $(DDMMYYYY) to continue receiving notifications.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-encryption-key-expiry\", \"title\": \"Encryption Key expiration\", \"description\": \"Notification about the encryption key expiration for the participant in the ecosystem.\", \"allowed_senders\": [ \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Network\", \"trigger\": \"Event\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} encryption key will be expiring on $(DDMMYYYY). Please renew your encryption key before $(DDMMYYYY) to carry on operating on HCX.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-system-downtime\", \"title\": \"System maintenance/Downtime\", \"description\": \"Notification about participant system downtime/maintenance\", \"allowed_senders\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} will be facing downtime from $(DDMMYYYY) to $(DDMMYYYY) due to planned maintenance. Sorry for inconvenience and please plan your operations accordingly.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-new-protocol-version-support\", \"title\": \"Support for new version of protocol\", \"description\": \"Notification about participant system supporting new version of the HCX protocol.\", \"allowed_senders\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} now supports $(version_code) on its platform. All participants are requested to upgrade to $(version_code) or above to transact with ${participant_name}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-terminology-version-support\", \"title\": \"Support for prescribed terminologies\", \"description\": \"Notification about participant system supporting particular format of terminologies of the HCX protocol.\", \"allowed_senders\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} now supports $(Master_list_version) on its platform. All participants are requested to upgrade to $(Master_list_version) or above to transact with ${participant_name}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-remove-protocol-feature\", \"title\": \"End of life for an existing capability\", \"description\": \"Notification about participant system ending/discontinuing a particular feature of the HCX protocol.\", \"allowed_senders\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} now does not support $(feature_code) on its platform.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-product-change-support\", \"title\": \"New insurance plans/products, end of support for existing products/plans\", \"description\": \"Notification about participant system adding new product or a particular feature of the HCX protocol.\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} now supports $(feature_code) on its platform. All participants can now initiate transactions relationg to $(feature_code).\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-policy-update\", \"title\": \"Policy Compliance changes  - terms of services\", \"description\": \"Notification about participant system changing terms of the services.\", \"allowed_senders\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} is changing the service terms for policy for the $(Usecase_code) going forward. Please plan your operations accordingly.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-participant-policy-sla-update\", \"title\": \"Policy Compliance changes  - SLAs\", \"description\": \"Notification about participant system SLA of the services.\", \"allowed_senders\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"allowed_recipients\": [ \"payor\", \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Broadcast\", \"category\": \"Participant\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} is changing the SLA policy for the $(Usecase_code) going forward. Please plan your operations accordingly.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-workflow-update\", \"title\": \"Staus change - Initiation, closure, approved, etc.\", \"description\": \"Notification about workflow updates on a policy.\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has updated the status for a policy to ${Status}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-admission-case\", \"title\": \"Admission case - For a particular disease\", \"description\": \"Notification about patient admission for a particular disease\", \"allowed_senders\": [ \"provider\" ], \"allowed_recipients\": [ \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has initiated a insurance claim workflow on admission for ${Disease_code}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-claim-initiation\", \"title\": \"Claim initiation for a particular disease\", \"description\": \"Notification about workflow updates for claim initiation for a particular disease..\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has initiated a insurance claim workflow on admission for ${Disease_code}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-claim-status-update\", \"title\": \"Notifications about status change of claims of a patient\", \"description\": \"Notification about workflow updates on a identified policy.\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has updated the status for ${policy_ID} to ${Status}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-coverage-eligibility-change\", \"title\": \"Notifications about change in coverage eligibility of a patient\", \"description\": \"Notification about change in coverage eligibility on a identified policy.\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has updated the coverage eligibility for ${policy_ID}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-claim-reimbursement\", \"title\": \"Notifications about reimbursements of claim for a patient\", \"description\": \"Notification about claim reimbursement..\", \"allowed_senders\": [ \"payor\" ], \"allowed_recipients\": [ \"provider\", \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has approved the reimbursement payment for ${policy_ID}.\\\"}\", \"status\": \"Active\" }, { \"topic_code\": \"notif-claim-reimbursement-inactive\", \"title\": \"Notifications about reimbursements of claim for a patient\", \"description\": \"Notification about claim reimbursement..\", \"allowed_senders\": [ \"payor\", \"provider\" ], \"allowed_recipients\": [ \"provider\", \"payor\", \"agency.tpa\", \"agency.regulator\", \"research\", \"member.isnp\", \"agency.sponsor\", \"HIE/HIO.HCX\" ], \"type\": \"Targeted\", \"category\": \"Workflow\", \"trigger\": \"API\", \"priority\": 1, \"template\": \"{\\\"message\\\": \\\"${participant_name} has approved the reimbursement payment for ${policy_ID}.\\\"}\", \"status\": \"InActive\" } ]");
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
