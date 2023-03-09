package org.swasth.common.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SMSService {

    public CompletableFuture<String> sendSMS(String phone, String message,String accessKey,String accessSecret,String awsRegion) {

        String phoneNumber = "+91"+ phone;  // Ex: +91XXX4374XX
        AmazonSNS snsClient = AmazonSNSClient.builder().withCredentials(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new BasicAWSCredentials(accessKey, accessSecret);
            }

            @Override
            public void refresh() {

            }
        }).withRegion(awsRegion).build();

        PublishResult result = snsClient.publish(new PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber));
        return CompletableFuture.completedFuture(result.getMessageId());
    }
}
