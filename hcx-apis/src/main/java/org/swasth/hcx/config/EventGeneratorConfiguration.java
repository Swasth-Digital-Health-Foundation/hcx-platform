package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.swasth.common.helpers.EventGenerator;

import java.util.List;

import static org.swasth.common.utils.Constants.*;

@Configuration
public class EventGeneratorConfiguration {

    @Autowired
    protected Environment env;

    @Bean
    public EventGenerator eventGenerator(){
        return new EventGenerator(getProtocolHeaders(), getJoseHeaders(), getRedirectHeaders(), getErrorHeaders());
    }

    private List<String> getProtocolHeaders(){
        List<String> protocolHeaders = env.getProperty(PROTOCOL_HEADERS_MANDATORY, List.class);
        protocolHeaders.addAll(env.getProperty(PROTOCOL_HEADERS_OPTIONAL, List.class));
        return protocolHeaders;
    }

    private List<String> getJoseHeaders(){
        return env.getProperty(JOSE_HEADERS, List.class);
    }

    private List<String> getRedirectHeaders(){
        List<String> redirectHeaders = env.getProperty(REDIRECT_HEADERS_MANDATORY, List.class);
        redirectHeaders.addAll(env.getProperty(REDIRECT_HEADERS_OPTIONAL, List.class));
        return  redirectHeaders;
    }

    private List<String> getErrorHeaders(){
        List<String> errorHeaders = env.getProperty(ERROR_HEADERS_MANDATORY, List.class);
        errorHeaders.addAll(env.getProperty(ERROR_HEADERS_OPTIONAL, List.class));
        return errorHeaders;
    }

}
