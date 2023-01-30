package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.CloudStorageClient;
import org.swasth.ICloudService;

@Configuration
class CloudStorageConfiguration {

    @Value("${certificates.awsAccesskey}")
    private String awsAccesskey;

    @Value("${certificates.awsSecretKey}")
    private String awsSecretKey;

    @Bean
    public ICloudService awsClient() {
        return new CloudStorageClient(awsAccesskey, awsSecretKey);
    }

}
