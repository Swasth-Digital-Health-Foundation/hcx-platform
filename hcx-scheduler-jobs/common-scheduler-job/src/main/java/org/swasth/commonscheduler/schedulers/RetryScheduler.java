package org.swasth.commonscheduler.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swasth.common.dto.Request;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@Component
public class RetryScheduler extends BaseScheduler {
    
    @Value("${max.retry}")
    private int maxRetry;

    @Value("${kafka.topic.output}")
    private String kafkaOutputTopic;

    @Value("${postgres.tablename}")
    private String postgresTableName;

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds.retry}")
    public void process() throws Exception {

        System.out.println("Retry batch job is started");
        ResultSet result;
        try(Connection connection = postgreSQLClient.getConnection(); Statement createStatement = connection.createStatement()){
            String selectQuery = String.format("SELECT * FROM %s WHERE status = '%s' AND retryCount <= %d;", postgresTableName, Constants.RETRY_STATUS, maxRetry);
            result = postgreSQLClient.executeQuery(selectQuery);
            int metrics = 0;
            while (result.next()) {
                String action = result.getString(Constants.ACTION);
                Request request = new Request(JSONUtils.deserialize(result.getString("data"), Map.class), action,"");
                request.setMid(result.getString(Constants.MID));
                request.setApiAction(action);
                int retryCount = result.getInt(Constants.RETRY_COUNT) + 1 ;
                String event = eventGenerator.generateMetadataEvent(request);
                Map<String,Object> eventMap = JSONUtils.deserialize(event, Map.class);
                eventMap.put(Constants.RETRY_INDEX, retryCount);
                kafkaClient.send(kafkaOutputTopic, request.getHcxSenderCode(), JSONUtils.serialize(eventMap));
                System.out.println("Event is pushed to kafka topic :: mid: " + request.getMid() + " :: retry count: " + retryCount);
                String updateQuery = String.format("UPDATE %s SET status = '%s', retryCount = %d, lastUpdatedOn = %d WHERE mid = '%s'", postgresTableName, Constants.RETRY_PROCESSING_STATUS, retryCount, System.currentTimeMillis(), request.getMid());
                createStatement.addBatch(updateQuery);
                metrics++;
            }
            createStatement.executeBatch();
            System.out.println("Total number of events processed: " + metrics);
            System.out.println("Job is completed");
        } catch (Exception e) {
            System.out.println("Error while processing event: " + e.getMessage());
            throw e;
        }
    }
}
