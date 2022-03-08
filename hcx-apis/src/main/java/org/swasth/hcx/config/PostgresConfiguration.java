package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.postgresql.PostgreSQLClient;

@Configuration
public class PostgresConfiguration {

    @Value("${postgres.url}")
    private String postgresUrl;

    @Value("${postgres.user}")
    private String postgresUser;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Bean
    public IDatabaseService postgreSQLClient(){
        IDatabaseService postgreSQLClient = new PostgreSQLClient(postgresUrl, postgresUser, postgresPassword);
        return postgreSQLClient;
    }

}
