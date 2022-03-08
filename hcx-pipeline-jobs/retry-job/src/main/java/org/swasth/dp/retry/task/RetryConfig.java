package org.swasth.dp.retry.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class RetryConfig extends BaseJobConfig {

    private Config config;

    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String RetryConsumer = "retry-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public RetryConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initValues();
    }

    private void initValues(){
        kafkaInputTopic = config.getString("kafka.input.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");
    }

}

