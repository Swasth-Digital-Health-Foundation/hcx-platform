package org.swasth.commonscheduler.schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.swasth.common.exception.ClientException;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.swasth.common.utils.Constants.*;

@Component
public class BaseScheduler {

    @Autowired
    private Environment env;

    @Value("${postgres.url}")
    private String postgresUrl;

    @Value("${postgres.user}")
    private String postgresUser;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Value("${kafka.url}")
    private String kafkaUrl;

    protected KafkaClient kafkaClient;
    protected EventGenerator eventGenerator;
    protected PostgreSQLClient postgreSQLClient;


    @PostConstruct
    public void init() throws SQLException, ClientException {
        kafkaClient = new KafkaClient(kafkaUrl);
        eventGenerator = new EventGenerator(getProtocolHeaders(), getJoseHeaders(), getRedirectHeaders(), getErrorHeaders(), getNotificationHeaders());
        postgreSQLClient = new PostgreSQLClient(postgresUrl, postgresUser, postgresPassword);
    }

    private List<String> getProtocolHeaders(){
        List<String> protocolHeaders = env.getProperty(PROTOCOL_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        protocolHeaders.addAll(env.getProperty(PROTOCOL_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return protocolHeaders;
    }

    private List<String> getJoseHeaders(){
        return env.getProperty(JOSE_HEADERS, List.class);
    }

    private List<String> getRedirectHeaders(){
        List<String> redirectHeaders = env.getProperty(REDIRECT_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        redirectHeaders.addAll(env.getProperty(REDIRECT_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return  redirectHeaders;
    }

    private List<String> getErrorHeaders(){
        List<String> errorHeaders = env.getProperty(ERROR_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        errorHeaders.addAll(env.getProperty(ERROR_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return errorHeaders;
    }

    private List<String> getNotificationHeaders(){
        List<String> notificationHeaders = env.getProperty(NOTIFICATION_HEADERS_MANDATORY, List.class, new ArrayList<String>());
        notificationHeaders.addAll(env.getProperty(NOTIFICATION_HEADERS_OPTIONAL, List.class, new ArrayList<String>()));
        return notificationHeaders;
    }
}
