package org.swasth.commonscheduler.schedulers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.NotificationUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParticipantValidationScheduler extends BaseScheduler {

    private final Logger logger = LoggerFactory.getLogger(ParticipantValidationScheduler.class);

    @Autowired
    private RegistryService registryService;

    @Value("${topicCode.encryptionCertExpired}")
    private String topicCode;

    @Value("${kafka.topic.notification}")
    private String notifyTopic;

    @Value("${hcx.participantCode}")
    private String hcxParticipantCode;

    @Value("${hcx.privateKey}")
    private String hcxPrivateKey;

    @Value("${notification.expiry}")
    private String notificationExpiry;

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds.participantVerify}")
    public void process() throws Exception {
        logger.info("Participant validation scheduler started");
        List<Map<String,Object>> participants = registryService.getDetails("{ \"filters\": { \"encryption_cert_expiry\": { \"<\": " + System.currentTimeMillis() + " } } }");
        logger.info("Total number of participants with expired encryption certificate: {}", participants.size());
        if(!participants.isEmpty()) {
            List<String> participantCodes = participants.stream().map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
            String message = (String) NotificationUtils.getNotification(topicCode).get(Constants.MESSAGE);
            String notifyEvent = eventGenerator.createNotifyEvent(topicCode, hcxParticipantCode, Constants.PARTICIPANT_CODE, participantCodes, new Date(new Date().getTime() + notificationExpiry).getTime(), message, hcxPrivateKey);
            kafkaClient.send(notifyTopic, Constants.NOTIFICATION, notifyEvent);
            logger.info("Notify event is pushed to kafka: {}", notifyEvent);
        }
        logger.info("Participant validation scheduler ended");
    }

}
