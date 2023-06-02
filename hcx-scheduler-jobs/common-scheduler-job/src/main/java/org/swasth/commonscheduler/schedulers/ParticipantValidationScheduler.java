package org.swasth.commonscheduler.schedulers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.NotificationUtils;

import java.util.*;

@Component
public class ParticipantValidationScheduler extends BaseScheduler {

    private final Logger logger = LoggerFactory.getLogger(ParticipantValidationScheduler.class);

    @Autowired
    private RegistryService registryService;

    @Value("${topicCode.encryptionCertExpired}")
    private String expiryTopicCode;

    @Value("${topicCode.beforeExpiry}")
    private String beforeExpiryTopicCode;

    @Value("${kafka.topic.notification}")
    private String notifyTopic;

    @Value("${hcx.participantCode}")
    private String hcxParticipantCode;

    @Value("${hcx.privateKey}")
    private String hcxPrivateKey;

    @Value("${notification.expiry}")
    private int notificationExpiry;
    @Value("${certificate.expiry-days}")
    private List<Integer> beforeExpiryDaysList;

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds.participantVerify}")
    public void process() throws Exception {
        logger.info("Participant validation scheduler started");
        String expiryMessage = "";
        String beforeExpiryMessage = "";
        List<Map<String, Object>> participants = new ArrayList<>();
        List<String> expiredParticipantCodes = new ArrayList<>();
        List<String> aboutToExpireParticipantCodes = new ArrayList<>();
        for (int beforeExpiryDay : beforeExpiryDaysList) {
            long expiryTime = System.currentTimeMillis() + (1 + beforeExpiryDay) * 24L * 60 * 60 * 1000;
            participants = registryService.getDetails("{ \"filters\": { \"encryption_cert_expiry\": { \"<\": " + expiryTime + " } } }");
            for (Map<String, Object> participant : participants) {
                long certExpiry = (long) participant.get(Constants.ENCRYPTION_CERT_EXPIRY);
                String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE);
                long earlierDayTime = expiryTime - (24L * 60 * 60 * 1000) ;
                if (certExpiry <= System.currentTimeMillis()) {
                    expiredParticipantCodes.add(participantCode);
                    expiryMessage = getTemplateMessage(expiryTopicCode);
                } else if (certExpiry > earlierDayTime && certExpiry < expiryTime){
                    aboutToExpireParticipantCodes.add(participantCode);
                    beforeExpiryMessage = getTemplateMessage(beforeExpiryTopicCode).replace("${days}", String.valueOf(beforeExpiryDay));
                }
            }
            generateEvent(aboutToExpireParticipantCodes, beforeExpiryMessage, beforeExpiryTopicCode);
            aboutToExpireParticipantCodes.clear();
        }
        generateEvent(expiredParticipantCodes, expiryMessage, expiryTopicCode);
        logger.info("Participant validation scheduler ended");
        logger.info("Total number of participants with expired or expiring encryption certificate in {}", participants.size());
    }

    private void generateEvent(List<String> participantCodes, String message, String topiCode) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, notificationExpiry);
        String event = eventGenerator.createNotifyEvent(topiCode, hcxParticipantCode, Constants.PARTICIPANT_CODE, participantCodes, cal.getTime().toInstant().toEpochMilli(), message, hcxPrivateKey);
        kafkaClient.send(notifyTopic, Constants.NOTIFICATION, event);
        logger.info("Notify event is pushed to kafka: {}", event);
    }
    private String getTemplateMessage(String topicCode) throws Exception {
        return (String) JSONUtils.deserialize((String) (NotificationUtils.getNotification(topicCode).get(Constants.TEMPLATE)), Map.class).get(Constants.MESSAGE);
    }
}
