package org.swasth.retrybatch.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.common.dto.Request;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.JSONUtils;
import org.swasth.retrybatch.task.RetryConfig;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import java.sql.ResultSet;
import java.util.Map;

public class RetryFunction {

    private final Logger logger = LoggerFactory.getLogger(RetryFunction.class);
    private RetryConfig config;
    private final KafkaClient kafkaClient;
    private final EventGenerator eventGenerator;
    private final PostgreSQLClient postgreSQLClient;

    public RetryFunction(RetryConfig config) {
        this.config = config;
        kafkaClient = new KafkaClient(config.kafkaUrl);
        eventGenerator = new EventGenerator(config.protocolHeaders, config.joseHeaders, config.redirectHeaders, config.errorHeaders);
        postgreSQLClient = new PostgreSQLClient(config.postgresUrl, config.postgresUser, config.postgresPassword);
    }

    public void process() throws Exception {
        System.out.println("Retry batch job is started");
        try {
            String selectQuery = String.format("SELECT * FROM %s WHERE status = '%s' AND retryCount < %d;", config.postgresTable, "request.retry", config.maxRetry);
            ResultSet result = postgreSQLClient.executeQuery(selectQuery);
            if(!result.next()) {
                System.out.println("No events to process");
            }
            while (result.next()) {
                Request request = new Request(JSONUtils.deserialize(result.getString("data"), Map.class));
                String mid = result.getString("mid");
                String action = result.getString("action");
                int retryCount = result.getInt("retryCount");
                String event = eventGenerator.generateMetadataEvent(mid, action, request);
                Map<String,Object> eventMap = JSONUtils.deserialize(event, Map.class);
                eventMap.put("retryCount", retryCount);
                kafkaClient.send(config.kafkaInputTopic, request.getSenderCode(), JSONUtils.serialize(eventMap));
                System.out.println("Event is pushed to kafka topic, mid: " + mid + " retry count: " + retryCount);
                String updateQuery = String.format("UPDATE %s SET status = '%s', lastUpdatedOn = %d WHERE mid = '%s'", config.postgresTable, "request.retry.processing", System.currentTimeMillis(), mid);
                postgreSQLClient.execute(updateQuery);
            }
            System.out.println("Job is completed");
        } catch (Exception e) {
            System.out.println("Error while processing event: " + e.getMessage());
            throw e;
        }
    }

}
