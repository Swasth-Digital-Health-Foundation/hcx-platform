package org.swasth.hcx.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

 class CloudStorageConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(CloudStorageConfiguration.class);

    @Test
    void should_check_presence_of_cloud_storage_configuration_bean() {
        context.run(it -> assertThat(it).hasSingleBean(CloudStorageConfiguration.class));
    }
}
