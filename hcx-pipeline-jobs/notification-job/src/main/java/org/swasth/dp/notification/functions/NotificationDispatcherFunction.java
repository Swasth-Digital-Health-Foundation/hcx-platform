package org.swasth.dp.notification.functions;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.core.function.DispatcherResult;
import org.swasth.dp.core.function.ErrorResponse;
import org.swasth.dp.core.util.Constants;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;
import scala.Option;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;


public class NotificationDispatcherFunction extends BaseNotificationFunction {

    private final Logger logger = LoggerFactory.getLogger(NotificationDispatcherFunction.class);

    public NotificationDispatcherFunction(NotificationConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String, Object> inputEvent, ProcessFunction<Map<String, Object>, Map<String,Object>>.Context context, Collector<Map<String,Object>> collector) throws Exception {
        Map<String,Object> actualEvent = (Map<String, Object>) inputEvent.get(Constants.INPUT_EVENT());
        List<Map<String, Object>> participantDetails = (List<Map<String, Object>>) inputEvent.get(Constants.PARTICIPANT_DETAILS());
        notificationDispatcher(participantDetails, actualEvent, context);
    }

    private void notificationDispatcher(List<Map<String, Object>> participantDetails, Map<String,Object> event , ProcessFunction<Map<String, Object>, Map<String,Object>>.Context context) throws Exception {
        int successfulDispatches = 0;
        int failedDispatches = 0;
        Long expiry = getProtocolLongValue(Constants.EXPIRY(), event);
        for(Map<String,Object> participant: participantDetails) {
            String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE());
            String endpointUrl = (String) participant.get(Constants.END_POINT());
            if (Constants.INVALID_STATUS().contains(participant.get(Constants.STATUS()))) {
                failedDispatches = getFailedDispatches(event, failedDispatches, participantCode, getErrorResponse(Constants.ERR_INVALID_RECIPIENT(), "Recipient is blocked or inactive as per the registry", ""));
            } else if (expiry != null && isExpired(expiry)) {
                failedDispatches = getFailedDispatches(event, failedDispatches, participantCode, getErrorResponse(Constants.ERR_NOTIFICATION_EXPIRED(), "Notification is expired", ""));
            } else if (Constants.VALID_STATUS().contains(participant.get(Constants.STATUS())) && !(participantCode).contains("null") && endpointUrl != null) {
                participant.put(Constants.END_POINT(), endpointUrl + event.get(Constants.ACTION()));
                String payload = getPayload(event);
                DispatcherResult result = dispatcherUtil.dispatch(participant, payload);
                String email = (String) participant.getOrDefault("primary_email", "");
                String userName = (String) participant.get("participant_name");
                System.out.println(userName + " ------USERNAME------ ");
                String topicCode = (String) event.getOrDefault(Constants.TOPIC_CODE(), "");
                System.out.println(topicCode+" TOPIC CODE ");
                String subject = "";
                switch (topicCode) {
                    case "notif-gateway-downtime":
                        System.out.println("----------------DOWN-TIME ----------------");
                        renderTemplate("downtime.ftl",usernameTemplate(userName));
                        System.out.println(renderTemplate("downtime.ftl",usernameTemplate(userName)));
                        subject = config.subGatewayDowntime;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-encryption-key-expiry":
                        System.out.println("--------------ENCRYPTION-KEY-EXPIRY-------------");
                        renderTemplate("encryption-key-expiration.ftl",usernameTemplate(userName));
                        subject = config.subEncKeyExpiry;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-participant-onboarded":
                        System.out.println("------------------PARTICIPANT-ONBOARD--------------");
                        renderTemplate("participant-onboarded.ftl",usernameTemplate(userName));
                        subject = config.subOnboarded;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-participant-de-boarded":
                        System.out.println("----------------PARTICIPANT-DE-BOARD-----------------");
                        renderTemplate("participant-deboarding.ftl",usernameTemplate(userName));
                        subject = config.subDeboarded;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-network-feature-removed":
                        System.out.println("----------------FEATURE-REMOVED----------------");
                        renderTemplate("end-of-feature-support.ftl",usernameTemplate(userName));
                        subject = config.subFeatRemoved;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-new-network-feature-added":
                        System.out.println("----------------NETWORK-FEATURE-ADDED------------");
                        renderTemplate("new-feature-support.ftl",usernameTemplate(userName));
                        subject = config.subFeatAdded;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-gateway-policy-sla-change":
                        System.out.println("---------------GATEWAY-POLICY-SLA-CHANGE-------------");
                        renderTemplate("policy-update.ftl",usernameTemplate(userName));
                        subject = config.subPolicyUpdate;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                    case "notif-certificate-revocation":
                        System.out.println("--------------CERT-REVOCATION-------------------");
                        renderTemplate("certificate-revocation.ftl",usernameTemplate(userName));
                        subject = config.subCertRevocation;
                        System.out.println(subject+" ====SUBJECT====");
                        break;
                }
                String message = (String) event.getOrDefault(Constants.MESSAGE(), "");
                Map<String,Object> emailEvent = getEmailMessageEvent(message, subject, List.of(email), new ArrayList<>(), new ArrayList<>());
                System.out.println(emailEvent+"  ------Email Event------");
                if (config.emailNotificationEnabled && !StringUtils.isEmpty(message) && !StringUtils.isEmpty(topicCode)) {
                    context.output(config.messageOutputTag, JSONUtil.serialize(emailEvent));
                }
                System.out.println("Recipient code: " + participantCode + " :: Dispatch status: " + result.success());
                logger.debug("Recipient code: " + participantCode + " :: Dispatch status: " + result.success());
                auditService.indexAudit(createNotificationAuditEvent(event, participantCode, createErrorMap(result.error() != null ? result.error().get() : null)));
                if(result.success()) successfulDispatches++; else failedDispatches++;
            }
        }

        int totalDispatches = successfulDispatches + failedDispatches;
        System.out.println("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
        logger.debug("Total number of notifications dispatched: " + totalDispatches + " :: successful dispatches: " + successfulDispatches + " :: failed dispatches: " + failedDispatches);
    }

    private ErrorResponse getErrorResponse(String errCode, String errMsg, String errCause) {
        return new ErrorResponse(Option.apply(errCode), Option.apply(errMsg), Option.apply(errCause));
    }

    private int getFailedDispatches(Map<String, Object> event, int failedDispatches, String participantCode, ErrorResponse error) {
        failedDispatches++;
        System.out.println("Recipient code: " + participantCode + " :: Dispatch status: false");
        logger.debug("Recipient code: " + participantCode + " :: Dispatch status: false");
        auditService.indexAudit(createNotificationAuditEvent(event, participantCode, createErrorMap(error)));
        return failedDispatches;
    }

    private String getPayload(Map<String,Object> event) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.PAYLOAD(), event.get(Constants.PAYLOAD()));
        return JSONUtil.serialize(payload);
    }

    public Map<String,Object> getEmailMessageEvent(String message, String subject, List<String> to, List<String> cc, List<String> bcc) throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("eid", "MESSAGE");
        event.put("mid", UUID.randomUUID());
        event.put("ets", System.currentTimeMillis());
        event.put("channel", "email");
        event.put("subject", subject);
        event.put("message", message);
        Map<String, Object> recipients = new HashMap<>();
        recipients.put("to", to);
        recipients.put("cc", cc);
        recipients.put("bcc", bcc);
        event.put("recipients", recipients);
        return event;
    }
    public String renderTemplate(String templateName, Map<String, Object> model) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(NotificationDispatcherFunction.class, "/templates");
        Template template = cfg.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    private Map<String, Object> usernameTemplate(String userName) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("USER_NAME", userName);
        return model;
    }
}
