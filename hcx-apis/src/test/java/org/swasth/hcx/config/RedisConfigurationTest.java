package org.swasth.hcx.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;


public class RedisConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(RedisConfiguration.class);

    @Test
    void should_check_presence_of_redis_configuration_bean() {
        context.run(it -> assertThat(it).hasSingleBean(RedisConfiguration.class));
    }

}
