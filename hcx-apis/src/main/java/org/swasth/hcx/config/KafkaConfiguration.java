package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.kafka.client.IEventService;
import org.swasth.kafka.client.KafkaClient;

@Configuration
public class KafkaConfiguration {

    @Value("${bootstrap-servers}")
    private String kafkaServerUrl;

    @Bean
    public IEventService kafkaClient() {
        return new KafkaClient(kafkaServerUrl);
    }

}
