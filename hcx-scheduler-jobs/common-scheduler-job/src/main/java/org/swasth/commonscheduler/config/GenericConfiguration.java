package org.swasth.commonscheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.NotificationUtils;

import java.io.IOException;

@Configuration
public class GenericConfiguration {

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${notification.masterListPath:notifications.yaml}")
    private String notificationsPath;

    @Bean
    public RegistryService registryService(){
        return new RegistryService(registryUrl);
    }

    @Bean
    public NotificationUtils notificationUtils() throws IOException {
        return new NotificationUtils(notificationsPath);
    }
}
