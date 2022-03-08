package org.swasth.dp.retrybatch.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.common.dto.Request;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.JSONUtils;
import org.swasth.dp.retrybatch.task.RetryConfig;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.PostgreSQLClient;

import java.sql.ResultSet;
import java.util.Map;

public class RetryFunction {

    private final Logger logger = LoggerFactory.getLogger(RetryFunction.class);
    private RetryConfig config;
    private final KafkaClient kafkaClient = new KafkaClient(config.kafkaUrl);
    private final EventGenerator eventGenerator = new EventGenerator(config.protocolHeaders, config.joseHeaders, config.redirectHeaders, config.errorHeaders);
    private final PostgreSQLClient postgreSQLClient = new PostgreSQLClient(config.postgresUrl, config.postgresUser, config.postgresPassword);

    public RetryFunction(RetryConfig config) {
        this.config = config;
    }

    public void process() throws Exception {
        try {
            String selectQuery = String.format("SELECT * FROM %s WHERE status = \"request.retry\" AND retryCount < %d;", config.postgresTable, config.maxRetry);
            ResultSet result = postgreSQLClient.executeQuery(selectQuery);
            while (result.next()) {
                Request request = new Request(JSONUtils.deserialize(result.getString("payload"), Map.class));
                String mid = result.getString("mid");
                String action = result.getString("action");
                int retryCount = result.getInt("retryCount");
                String event = eventGenerator.generateMetadataEvent(mid, action, request);
                Map<String,Object> eventMap = JSONUtils.deserialize(event, Map.class);
                eventMap.put("retryCount", retryCount);
                kafkaClient.send(config.kafkaInputTopic, request.getSenderCode(), JSONUtils.serialize(eventMap));
                logger.info("Event is pushed to kafka topic, mid: " + mid + " retry count: " + retryCount);
                String updateQuery = String.format("UPDATE %s SET status = \"request.retry.processing\", lastUpdatedOn = %d WHERE mid = %s", config.postgresTable, System.currentTimeMillis(), mid);
                postgreSQLClient.execute(updateQuery);
            }
        } catch (Exception e) {
            logger.error("Error while processing event: " + e.getMessage());
            throw e;
        }
    }

}
