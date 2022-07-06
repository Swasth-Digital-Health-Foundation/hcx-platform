package org.swasth.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class GenericConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(GenericConfiguration.class);

    @Test
    void should_check_presence_of_audit_indexer_bean() {
        context.run(it -> assertThat(it).hasBean("auditIndexer"));
    }

    @Test
    void should_check_presence_of_redis_cache_bean() {
        context.run(it -> assertThat(it).hasBean("redisCache"));
    }

    @Test
    void should_check_presence_of_notification_utils_bean() {
        context.run(it -> assertThat(it).hasBean("notificationUtils"));
    }
}
