package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.swasth.common.exception.ClientException;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.postgresql.PostgreSQLClient;

import java.sql.SQLException;

@Configuration
public class PostgresConfiguration {

    @Value("${postgres.url}")
    private String postgresUrl;

    @Value("${postgres.user}")
    private String postgresUser;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Value("${postgres.mock-service.url}")
    private String mockServiceDatabaseUrl;

    @Primary
    @Bean
    public IDatabaseService postgreSQLClient() throws ClientException, SQLException {
        return new PostgreSQLClient(postgresUrl, postgresUser, postgresPassword);
    }
    @Bean
    public IDatabaseService postgresClientMockService() throws SQLException, ClientException {
        return new PostgreSQLClient(mockServiceDatabaseUrl,postgresUser,postgresPassword);
    }

}
