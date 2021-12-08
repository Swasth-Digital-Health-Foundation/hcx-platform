package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.common.exception.ClientException;
import org.swasth.kafka.client.IEventService;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.postgresql.PostgreSQLClient;

import java.security.PrivateKey;

@Configuration
public class GenericConfiguration {

    @Value("${bootstrap-servers}")
    private String kafkaServerUrl;

    @Value("${postgres.url}")
    private String postgresUrl;

    @Value("${postgres.user}")
    private String postgresUser;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Value("${postgres.tablename}")
    private String postgresTableName;

    @Bean
    public IEventService kafkaClient() {
        IEventService kafkaClient = new KafkaClient(kafkaServerUrl);
        return kafkaClient;
    }

    @Bean
    public IDatabaseService postgreSQLClient() {
       IDatabaseService postgreSQLClient = new PostgreSQLClient(postgresUrl, postgresUser, postgresPassword, postgresTableName);
       return postgreSQLClient;
    }

}
