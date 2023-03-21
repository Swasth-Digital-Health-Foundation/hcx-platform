package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.springcommon.service.EmailService;
import org.swasth.springcommon.service.SMSService;


@Configuration
public class GenericConfiguration {
    @Bean
    public JWTUtils jwtUtils() {
        return new JWTUtils();
    }

    @Bean
    public EventGenerator eventGenerator() {
        return new EventGenerator();
    }

    @Bean
    public EmailService emailService(){
        return new EmailService();
    }
    @Bean
    public SMSService smsService(){
        return  new SMSService();
    }

}
