package org.swasth.hcx.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticSearchConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(ElasticSearchConfiguration.class);

    @Test
    void should_check_presence_of_elastic_search_configuration_bean() {
        context.run(it -> assertThat(it).hasSingleBean(ElasticSearchConfiguration.class));
    }

}