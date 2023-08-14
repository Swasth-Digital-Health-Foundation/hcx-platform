package org.swasth.dp.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class ProtocolRequestProcessorConfig extends BaseJobConfig {

    private final Config config;
    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String protocolRequestConsumer = "protocol-request-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public ProtocolRequestProcessorConfig(Config config, String jobName) {
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
