package org.swasth.retrybatch.task;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RetryConfig {

    private Config config;

    // From base-config file
    public String postgresUser;
    public String postgresPassword;
    public String postgresTable;
    public String postgresUrl;

    // kafka
    public String kafkaUrl;
    public String kafkaInputTopic;

    public int maxRetry;
    public List<String> protocolHeaders;
    public List<String> joseHeaders;
    public List<String> redirectHeaders;
    public List<String> errorHeaders;

    public RetryConfig() {
        this.config = ConfigFactory.load().withFallback(ConfigFactory.systemEnvironment());
        initValues();
    }

    private void initValues(){
        postgresUrl = config.getString("postgres.url");
        postgresUser = config.getString("postgres.user");
        postgresPassword = config.getString("postgres.password");
        postgresTable = config.getString("postgres.table");
        kafkaUrl = config.getString("kafka.url");
        kafkaInputTopic = config.getString("kafka.topic.input");
        maxRetry = config.getInt("max.retry");
        protocolHeaders = config.getStringList("protocol.headers.mandatory");
        protocolHeaders.addAll(config.getStringList("protocol.headers.optional"));
        joseHeaders = config.getStringList("headers.jose");
        redirectHeaders = config.getStringList("redirect.headers.mandatory");
        redirectHeaders.addAll(config.getStringList("redirect.headers.optional"));
        errorHeaders = config.getStringList("plainrequest.headers.mandatory");
        errorHeaders.addAll(config.getStringList("plainrequest.headers.optional"));
    }

}
