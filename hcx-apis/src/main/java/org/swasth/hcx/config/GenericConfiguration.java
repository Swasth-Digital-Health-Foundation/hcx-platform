package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.JWTUtils;
import org.swasth.common.utils.NotificationUtils;
import org.swasth.springcommon.service.EmailService;
import org.swasth.springcommon.service.SMSService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.swasth.common.utils.Constants.*;

@Configuration
public class GenericConfiguration {

    @Autowired
    protected Environment env;

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${notification.networkPath:networkNotifications.yaml}")
    private String networkPath;

    @Value("${notification.participantPath:participantNotifications.yaml}")
    private String participantPath;

    @Value("${notification.workflowPath:workflowNotifications.yaml}")
    private String workflowPath;

    @Value("${tag}")
    private String tag;
    @Bean
    public RegistryService registryService() {
        return new RegistryService(registryUrl);
    }

    @Bean
    public NotificationUtils notificationUtils() throws IOException {
        return new NotificationUtils(networkPath, participantPath, workflowPath);
    }

    @Bean
    public JWTUtils jwtUtils() {
        return new JWTUtils();
    }

    @Bean
    public SMSService smsService(){
        return new SMSService();
    }
    @Bean
    public EmailService emailService(){
        return new EmailService();
    }
    @Bean
    public EventGenerator eventGenerator() {
        return new EventGenerator(getProtocolHeaders(), getJoseHeaders(), getRedirectHeaders(), getErrorHeaders(), getNotificationHeaders() ,tag);
    }

    private List<String> getProtocolHeaders() {
        List<String> protocolHeaders = env.getProperty(PROTOCOL_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        protocolHeaders.addAll(env.getProperty(PROTOCOL_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return protocolHeaders;
    }

    private List<String> getJoseHeaders() {
        return env.getProperty(JOSE_HEADERS, List.class);
    }

    private List<String> getRedirectHeaders() {
        List<String> redirectHeaders = env.getProperty(REDIRECT_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        redirectHeaders.addAll(env.getProperty(REDIRECT_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return redirectHeaders;
    }

    private List<String> getErrorHeaders() {
        List<String> errorHeaders = env.getProperty(ERROR_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        errorHeaders.addAll(env.getProperty(ERROR_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return errorHeaders;
    }

    private List<String> getNotificationHeaders() {
        List<String> notificationHeaders = env.getProperty(NOTIFICATION_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        notificationHeaders.addAll(env.getProperty(NOTIFICATION_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return notificationHeaders;
    }
}
