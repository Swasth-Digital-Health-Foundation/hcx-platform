package org.swasth.hcx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Set the initial number of threads in the pool
        executor.setMaxPoolSize(20);   // Set the maximum number of threads in the pool
        executor.setQueueCapacity(100); // Set the capacity of the task queue
        executor.setThreadNamePrefix("User-"); // Set the prefix for thread names
        executor.initialize(); // Initialize the executor
        return executor;
    }
}
