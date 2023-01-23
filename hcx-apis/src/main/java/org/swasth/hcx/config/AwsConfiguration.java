package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.AwsClient;
import org.swasth.ICloudService;

@Configuration
public class AwsConfiguration {

    @Value("${certificates.aws_accesskey}")
    private String awsAccesskey;

    @Value("${certificates.aws_secretKey}")
    private String awsSecretKey;

    @Value("${certificates.bucket_name}")
    private String bucketName;

    @Bean
    public ICloudService AwsClient(){
        return new AwsClient(awsAccesskey,awsSecretKey,bucketName);
    }

}
