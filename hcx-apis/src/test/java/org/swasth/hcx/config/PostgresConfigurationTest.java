package org.swasth.hcx.config;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.swasth.postgresql.PostgreSQLClient;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresConfigurationTest{

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(PostgresConfiguration.class);

    @Test
    public void should_check_presence_of_postgres_service() {
        context.run(it -> assertThat(it).hasSingleBean(PostgreSQLClient.class));
    }
}