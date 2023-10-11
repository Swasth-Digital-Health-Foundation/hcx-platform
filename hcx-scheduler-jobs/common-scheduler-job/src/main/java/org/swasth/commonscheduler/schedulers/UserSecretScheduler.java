package org.swasth.commonscheduler.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.NotificationUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Component
public class UserSecretScheduler extends BaseScheduler {
    private final Logger logger = LoggerFactory.getLogger(UserSecretScheduler.class);
    @Value("${postgres.table}")
    private String postgresTable;

    @Value("${secret.expiry-days}")
    private List<Integer> secretExpiryDays;

    @Value("${kafka.topic.notification}")
    private String notifyTopic;

    @Value("${topicCode.secret-expired}")
    private String secretExpired;

    @Value("${topicCode.before-secret-expiry}")
    private String beforeSecretExpiry;

    @Value("${notification.expiry}")
    private int notificationExpiry;

    @Value("${hcx.participantCode}")
    private String hcxParticipantCode;

    @Value("${hcx.privateKey}")
    private String hcxPrivateKey;
    public void process() throws Exception {
        logger.info("User secret job started");
        processExpiredSecret();
        processExpirySecret();
    }

    public void processExpiredSecret() throws Exception {
        logger.info("User secret expired batch job started");
        String expiryMessage = "";
        List<String> expiredParticipantCodes = new ArrayList<>();
        try (Connection connection = postgreSQLClient.getConnection();
             Statement createStatement = connection.createStatement()) {
            String secretExpiredQuery = String.format("SELECT * FROM %s  WHERE secret_expiry_date <= %d ;", postgresTable, System.currentTimeMillis());
            ResultSet result = postgreSQLClient.executeQuery(secretExpiredQuery);
            while (result.next()) {
                String participants = result.getString("username");
                expiredParticipantCodes.add(String.valueOf(participants));
                expiryMessage = getTemplateMessage(secretExpired);
            }
            generateEvent(expiredParticipantCodes, expiryMessage, secretExpired);
            createStatement.executeBatch();
            logger.info("Job is completed");
        } catch (Exception e) {
            throw e;
        }
    }

    public void processExpirySecret() throws Exception {
        logger.info("User secret expiry batch job started");
        String beforeExpiryMessage = "";
        List<String> aboutToExpireParticipantCodes = new ArrayList<>();
        try (Connection connection = postgreSQLClient.getConnection();
             Statement createStatement = connection.createStatement()) {
            for (int beforeExpiryDay : secretExpiryDays) {
                long expiryTime = System.currentTimeMillis() + (1 + beforeExpiryDay) * 24L * 60 * 60 * 1000;
                long earlierDayTime = expiryTime - (24L * 60 * 60 * 1000);
                String query = String.format("SELECT * FROM %s  WHERE secret_expiry_date > %d AND secret_expiry_date < %d;", postgresTable, earlierDayTime, expiryTime);
                ResultSet resultSet = postgreSQLClient.executeQuery(query);
                while (resultSet.next()) {
                    String participants = resultSet.getString("username");
                    aboutToExpireParticipantCodes.add(String.valueOf(participants));
                    beforeExpiryMessage = getTemplateMessage(beforeSecretExpiry).replace("${days}", String.valueOf(beforeExpiryDay));
                }
                generateEvent(aboutToExpireParticipantCodes, beforeExpiryMessage, beforeSecretExpiry);
                aboutToExpireParticipantCodes.clear();
            }
            createStatement.executeBatch();
            logger.info("Job is completed");
        } catch (Exception e) {
            throw e;
        }
    }

    private void generateEvent(List<String> participantCodes, String message, String topiCode) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, notificationExpiry);
        String event = eventGenerator.createNotifyEvent(topiCode, hcxParticipantCode, Constants.PARTICIPANT_CODE, participantCodes, cal.getTime().toInstant().toEpochMilli(), message, hcxPrivateKey);
        kafkaClient.send(notifyTopic, Constants.NOTIFICATION, event);
    }

    private String getTemplateMessage(String topicCode) throws Exception {
        return (String) JSONUtils.deserialize((String) (NotificationUtils.getNotification(topicCode).get(Constants.TEMPLATE)), Map.class).get(Constants.MESSAGE);
    }
}
