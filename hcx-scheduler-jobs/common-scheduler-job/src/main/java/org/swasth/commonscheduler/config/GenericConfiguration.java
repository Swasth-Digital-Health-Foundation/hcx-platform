package org.swasth.commonscheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.common.service.RegistryService;

@Configuration
public class GenericConfiguration {

    @Value("${registry.basePath}")
    private String registryUrl;

    @Bean
    public RegistryService registryService(){
        return new RegistryService(registryUrl);
    }
}
