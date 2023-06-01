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
import java.util.stream.Collectors;

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
            long expiryTime = System.currentTimeMillis() + beforeExpiryDay * 24L * 60 * 60 * 1000;
             participants = registryService.getDetails("{ \"filters\": { \"encryption_cert_expiry\": { \"<\": " + expiryTime + " } } }");
            for (Map<String, Object> participant : participants) {
                long certExpiry = (long) participant.get("encryption_cert_expiry");
                String participantCode = (String) participant.get(Constants.PARTICIPANT_CODE);
                if (certExpiry <= System.currentTimeMillis()) {
                    expiredParticipantCodes.add(participantCode);
                     expiryMessage = (String) JSONUtils.deserialize((String) (NotificationUtils.getNotification(expiryTopicCode).get(Constants.TEMPLATE)), Map.class).get(Constants.MESSAGE);
                } else {
                    aboutToExpireParticipantCodes.add(participantCode);
                    beforeExpiryMessage = (String) JSONUtils.deserialize((String) (NotificationUtils.getNotification(beforeExpiryTopicCode).get(Constants.TEMPLATE)), Map.class).get(Constants.MESSAGE);
                }
            }
        }
        logger.info("Total number of participants with expired or expiring encryption certificate in {}", participants.size());
        generateEvent(expiredParticipantCodes,expiryMessage,expiryTopicCode);
        generateEvent(aboutToExpireParticipantCodes,beforeExpiryMessage,beforeExpiryTopicCode);
        logger.info("Participant validation scheduler ended");
    }

    private void generateEvent(List<String> participantCodes , String message , String topiCode) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, notificationExpiry);
        String event = eventGenerator.createNotifyEvent(topiCode, hcxParticipantCode, Constants.PARTICIPANT_CODE, participantCodes, cal.getTime().toInstant().toEpochMilli(), message, hcxPrivateKey);
        kafkaClient.send(notifyTopic,Constants.NOTIFICATION,event);
        logger.info("Notify event is pushed to kafka: {}", event);
    }
}
