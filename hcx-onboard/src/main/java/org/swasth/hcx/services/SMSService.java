package org.swasth.hcx.services;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SMSService {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.accessSecret}")
    private String accessSecret;

    @Value("${aws.region}")
    private String awsRegion;

    @Async
    public CompletableFuture<String> sendOTP(String phone, String phoneOtp) {
        String message = "HCX mobile verification code is:" +phoneOtp;
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