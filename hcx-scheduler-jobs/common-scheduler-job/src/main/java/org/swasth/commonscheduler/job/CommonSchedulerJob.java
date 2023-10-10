package org.swasth.commonscheduler.job;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.swasth.commonscheduler.schedulers.ParticipantValidationScheduler;
import org.swasth.commonscheduler.schedulers.RetryScheduler;
import org.swasth.commonscheduler.schedulers.UserSecretScheduler;

@SpringBootApplication(scanBasePackages={"org.swasth.commonscheduler"})
public class CommonSchedulerJob implements CommandLineRunner {

    @Autowired
    ParticipantValidationScheduler participantValidationScheduler;

    @Autowired
    RetryScheduler retryScheduler;
    @Autowired
    UserSecretScheduler userSecretScheduler;
    public static void main(String[] args) {
        SpringApplication.run(CommonSchedulerJob.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && StringUtils.equalsIgnoreCase("ParticipantValidation", args[0])) {
            participantValidationScheduler.init();
            participantValidationScheduler.process();
        } else if (args.length > 0 && StringUtils.equalsIgnoreCase("Retry", args[0])) {
            retryScheduler.init();
            retryScheduler.process();
        } else if (args.length > 0 && StringUtils.equalsIgnoreCase("UserSecret", args[0])){
            userSecretScheduler.init();
            userSecretScheduler.process();
        }else {
            System.out.println("No input to process the scheduler.");
        }

    }
}