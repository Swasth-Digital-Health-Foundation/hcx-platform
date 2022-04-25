package org.swasth.hcx.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;


public class GenericConfigurationTest {

    /*
     * I setup a context runner with the class GenericConfiguration
     * in it. For that, I use ApplicationContextRunner#withUserConfiguration()
     * methods to populate the context.
     */
    ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(GenericConfiguration.class);

    @Test
    public void should_check_presence_of_generic_configuration_bean() {
        /*
         * We start the context and we will be able to trigger
         * assertions in a lambda receiving a
         * AssertableApplicationContext
         */
        context.run(it -> {
            /*
             * I can use assertThat to assert on the context
             * and check if the @Bean configured is present
             * (and unique)
             */
            assertThat(it).hasSingleBean(GenericConfiguration.class);
        });
    }

}
