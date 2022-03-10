package org.swasth.retrybatch.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.swasth.common.dto.Request;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@Component
public class RetryFunction {

    private final Logger logger = LoggerFactory.getLogger(RetryFunction.class);

    @Autowired
    private Environment env;

    @Value("${postgres.url}")
    private String postgresUrl;

    @Value("${postgres.user}")
    private String postgresUser;

    @Value("${postgres.password}")
    private String postgresPassword;

    @Value("${postgres.tablename}")
    private String postgresTableName;

    @Value("${max.retry}")
    private int maxRetry;

    @Value("${kafka.url}")
    private String kafkaUrl;

    @Value("${kafka.topic.output}")
    private String kafkaOutputTopic;

    @PostConstruct
    public void process() throws Exception {
        System.out.println("Testing " + kafkaUrl + postgresTableName);
        KafkaClient kafkaClient = new KafkaClient(kafkaUrl);
        EventGenerator eventGenerator = new EventGenerator(getProtocolHeaders(), getJoseHeaders(), getRedirectHeaders(), getErrorHeaders());
        PostgreSQLClient postgreSQLClient = new PostgreSQLClient(postgresUrl, postgresUser, postgresPassword);
        System.out.println("Retry batch job is started");
        ResultSet result = null;
        try {
            String selectQuery = String.format("SELECT * FROM %s WHERE status = '%s' AND retryCount <= %d;", postgresTableName, Constants.RETRY_STATUS, maxRetry);
            result = postgreSQLClient.executeQuery(selectQuery);
            int metrics = 0;
            while (result.next()) {
                Request request = new Request(JSONUtils.deserialize(result.getString("data"), Map.class));
                String mid = result.getString(Constants.MID);
                String action = result.getString(Constants.ACTION);
                int retryCount = result.getInt(Constants.RETRY_COUNT);
                String event = eventGenerator.generateMetadataEvent(mid, action, request);
                Map<String,Object> eventMap = JSONUtils.deserialize(event, Map.class);
                eventMap.put(Constants.RETRY_INDEX, retryCount + 1);
                kafkaClient.send(kafkaOutputTopic, request.getSenderCode(), JSONUtils.serialize(eventMap));
                System.out.println("Event is pushed to kafka topic, mid: " + mid + " retry count: " + retryCount);
                String updateQuery = String.format("UPDATE %s SET status = '%s', retryCount = retryCount + 1, lastUpdatedOn = %d WHERE mid = '%s'", postgresTableName, Constants.RETRY_PROCESSING_STATUS, System.currentTimeMillis(), mid);
                postgreSQLClient.execute(updateQuery);
                metrics++;
            }
            System.out.println("Total number of events processed: " + metrics);
            System.out.println("Job is completed");
        } catch (Exception e) {
            System.out.println("Error while processing event: " + e.getMessage());
            throw e;
        } finally {
            if(result != null) result.close();
        }
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

}
