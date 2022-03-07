package org.swasth.dp.status.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class StatusSearchConfig extends BaseJobConfig {

    private Config config;

    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String statusSearchConsumer = "status-search-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public StatusSearchConfig(Config config, String jobName) {
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
