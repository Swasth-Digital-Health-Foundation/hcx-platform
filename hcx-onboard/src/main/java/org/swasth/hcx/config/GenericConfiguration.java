package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.helpers.EventGenerator;


@Configuration
public class GenericConfiguration {

    @Autowired
    protected Environment env;

    @Value("${registry.basePath}")
    private String registryUrl;

    @Bean
    public RegistryService registryService() {
        return new RegistryService(registryUrl);
    }


    @Bean
    public JWTUtils jwtUtils() {
        return new JWTUtils();
    }

    @Bean
    public EventGenerator eventGenerator() {
        return new EventGenerator();
    }

}
