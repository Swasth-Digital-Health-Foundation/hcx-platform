package org.swasth.retrybatch.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.swasth.retrybatch.functions.RetryFunction;

@SpringBootApplication
@EnableScheduling
public class RetryBatchJob {

    private final static Logger logger = LoggerFactory.getLogger(RetryBatchJob.class);
    RetryConfig config = new RetryConfig();

    public static void main(String[] args) {
        SpringApplication.run(RetryBatchJob.class, args);
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void task(){
        try {
            RetryFunction retryFunction = new RetryFunction(config);
            retryFunction.process();
        } catch (Exception e) {
            logger.error("Error while running the batch process: " + e.getMessage());
            e.printStackTrace();
        }
    }

}