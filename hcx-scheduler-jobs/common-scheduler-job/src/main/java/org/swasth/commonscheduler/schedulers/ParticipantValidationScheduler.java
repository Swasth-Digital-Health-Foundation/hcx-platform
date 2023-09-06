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

    @Value("${topicCode.encryption-cert-expired}")
    private String encryptionCertExpiredTopicCode;

    @Value("${topicCode.before-encryption-cert-expiry}")
    private String beforeEncryptionCertExpiryTopicCode;

    @Value("${topicCode.signing-cert-expired}")
    private String signingCertExpiredTopicCode;

    @Value("${topicCode.before-signing-cert-expiry}")
    private String beforeSigningCertExpiryTopicCode;
    @Value("${kafka.topic.notification}")
    private String notifyTopic;

    @Value("${hcx.participantCode}")
    private String hcxParticipantCode;

    @Value("${hcx.privateKey}")
    private String hcxPrivateKey;

    @Value("${notification.expiry}")
    private int notificationExpiry;
    @Value("${certificate.expiry-days}")
    private List<Integer> certificateExpiryDaysList;

    public void process() throws Exception {
        logger.info("Participant validation scheduler started");
        certExpiry(Constants.SIGNING_CERT_PATH_EXPIRY, beforeSigningCertExpiryTopicCode, signingCertExpiredTopicCode);
        certExpiry(Constants.ENCRYPTION_CERT_EXPIRY, beforeEncryptionCertExpiryTopicCode, encryptionCertExpiredTopicCode);
    }

    public void certExpiry(String certType, String expiryTopicCode, String expiredTopicCode) throws Exception {
        String expiryMessage = "";
        String beforeExpiryMessage = "";
        List<String> expiredParticipantCodes = new ArrayList<>();
        List<String> aboutToExpireParticipantCodes = new ArrayList<>();
        List<Map<String, Object>> participants = new ArrayList<>();
        for (int beforeExpiryDay : certificateExpiryDaysList) {
            long expiryTime = System.currentTimeMillis() + (1 + beforeExpiryDay) * 24L * 60 * 60 * 1000;
            participants = registryService.getDetails("{ \"filters\": { \"" + certType + "\": { \"<\": " + expiryTime + " } } }");
            for (Map<String, Object> participant : participants) {
                long certExpiry = (long) participant.get(certType);
                String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE);
                long earlierDayTime = expiryTime - (24L * 60 * 60 * 1000);
                if (certExpiry <= System.currentTimeMillis()) {
                    expiredParticipantCodes.add(participantCode);
                    expiryMessage = getTemplateMessage(expiredTopicCode);
                } else if (certExpiry > earlierDayTime && certExpiry < expiryTime) {
                    aboutToExpireParticipantCodes.add(participantCode);
                    beforeExpiryMessage = getTemplateMessage(expiryTopicCode).replace("${days}", String.valueOf(beforeExpiryDay));
                }
            }
            generateEvent(aboutToExpireParticipantCodes, beforeExpiryMessage, expiryTopicCode);
            aboutToExpireParticipantCodes.clear();
        }
        generateEvent(expiredParticipantCodes, expiryMessage, expiredTopicCode);
        logger.info("Total number of participants with expired or expiring {} certificate in {}", certType, participants.size());
        logger.info("Participant validation scheduler ended");
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
