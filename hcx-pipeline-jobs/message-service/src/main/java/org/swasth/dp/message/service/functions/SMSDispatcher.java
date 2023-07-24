package org.swasth.dp.message.service.functions;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.message.service.task.MessageServiceConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSDispatcher extends ProcessFunction<Map<String,Object>, Map<String,Object>> {

    private final Logger logger = LoggerFactory.getLogger(SMSDispatcher.class);

    protected MessageServiceConfig config;

    public SMSDispatcher(MessageServiceConfig config) {
        this.config = config;
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) {
        try {
            List<String> recipients = (List<String>) ((Map<String,Object>) event.getOrDefault("recipients", new HashMap<>())).getOrDefault("to", new ArrayList<>());
            if (!recipients.isEmpty()) {
                for (String recipient : recipients) {
                    String msgId = sendSMS(recipient, event.get("message").toString());
                    logger.info("SMS is successfully sent :: Mid: {} Message Id: {}", event.get("mid"), msgId);
                }
            }
        } catch (Exception e) {
            logger.error("Exception while sending SMS: " + e.getMessage());
            throw e;
        }
    }

    public String sendSMS(String phone, String content){
        String phoneNumber = "+91"+ phone;
        AmazonSNS snsClient = AmazonSNSClient.builder().withCredentials(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(config.awsAccessKey, config.awsAccessSecret);
            }

            @Override
            public void refresh() {

            }
        }).withRegion(config.awsRegion).build();

        PublishResult result = snsClient.publish(new PublishRequest()
                .withMessage(content)
                .withPhoneNumber(phoneNumber));
        return result.getMessageId();
    }
}
