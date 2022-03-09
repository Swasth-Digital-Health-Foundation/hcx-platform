package org.swasth.hcx.config;


import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticSearchConfigurationTest{

    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(ElasticSearchConfiguration.class);

    @Test
    public void should_check_presence_of_elastic_service() {
        context.run(it -> assertThat(it).hasSingleBean(RestHighLevelClient.class));
    }
}
