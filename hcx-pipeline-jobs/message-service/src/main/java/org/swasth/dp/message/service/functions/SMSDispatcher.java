package org.swasth.dp.message.service.functions;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
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

public class SMSDispatcher extends BaseDispatcher {

    private final Logger logger = LoggerFactory.getLogger(SMSDispatcher.class);
    public SMSDispatcher(MessageServiceConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) {
        try {
            System.out.println("Processing SMS Event :: Mid: " + event.get("mid"));
            List<String> recipients = (List<String>) ((Map<String,Object>) event.getOrDefault("recipients", new HashMap<>())).getOrDefault("to", new ArrayList<>());
            if (!recipients.isEmpty()) {
                for (String recipient : recipients) {
                    String msgId = sendSMS(recipient, event.get("message").toString());
                    auditService.indexAudit(config.onboardIndex, config.onboardIndexAlias, eventGenerator.createMessageDispatchAudit(event, new HashMap<>()));
                    System.out.println("SMS is successfully sent :: Mid: " + event.get("mid") + " Message Id: " + msgId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            auditService.indexAudit(config.onboardIndex, config.onboardIndexAlias, eventGenerator.createMessageDispatchAudit(event, createErrorMap("", e.getMessage(), "")));
            System.out.println("Exception while sending SMS: " + e.getMessage());
            throw e;
        }
    }

    public String sendSMS(String phone, String content){
        System.out.println("aws " + config.awsAccessKey + config.awsAccessSecret);
        String phoneNumber = "+91"+ phone;
        AmazonSNS snsClient = AmazonSNSClient.builder().withCredentials(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(config.awsAccessKey, config.awsAccessSecret);
            }

            @Override
            public void refresh() {

            }
        }).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://sns.ap-south-1.amazonaws.com", (config.awsRegion))).build();

        PublishResult result = snsClient.publish(new PublishRequest()
                .withMessage(content)
                .withPhoneNumber(phoneNumber));
        return result.getMessageId();
    }
}
