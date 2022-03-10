package org.swasth.retrybatch.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.swasth.retrybatch.functions.RetryFunction;

@SpringBootApplication(scanBasePackages={"org.swasth.retrybatch"})
@EnableScheduling
public class RetryBatchJob {

    private final static Logger logger = LoggerFactory.getLogger(RetryBatchJob.class);

    @Autowired
    RetryFunction retryFunction;

    public static void main(String[] args) {
        SpringApplication.run(RetryBatchJob.class, args);
    }

    @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
    public void task(){
        try {
            retryFunction.process();
        } catch (Exception e) {
            logger.error("Error while running the batch process: " + e.getMessage());
        }
    }

}