package org.swasth.hcx.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

 class AwsConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(AwsConfiguration.class);

    @Test
    void should_check_presence_of_Aws_configuration_bean() {
        context.run(it -> assertThat(it).hasSingleBean(AwsConfiguration.class));
    }
}
