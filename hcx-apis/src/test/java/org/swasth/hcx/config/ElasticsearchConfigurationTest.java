//package org.swasth.hcx.config;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.runner.ApplicationContextRunner;
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//public class ElasticsearchConfigurationTest {
//
//    ApplicationContextRunner applicationContextRunner =  new ApplicationContextRunner().
//            withUserConfiguration(ElasticSearchConfiguration.class);
//
//
//    @Test
//    void should_check_presence_of_elasticsearch_configuration_bean(){
//        applicationContextRunner.run(it -> assertThat(it).hasSameClassAs(ElasticSearchConfiguration.class));
//    }
//
//}
