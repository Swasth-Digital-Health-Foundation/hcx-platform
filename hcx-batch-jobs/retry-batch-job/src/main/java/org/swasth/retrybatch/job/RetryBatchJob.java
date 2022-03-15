package org.swasth.retrybatch.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages={"org.swasth.retrybatch"})
@EnableScheduling
public class RetryBatchJob {

    public static void main(String[] args) {
        SpringApplication.run(RetryBatchJob.class, args);
    }

}