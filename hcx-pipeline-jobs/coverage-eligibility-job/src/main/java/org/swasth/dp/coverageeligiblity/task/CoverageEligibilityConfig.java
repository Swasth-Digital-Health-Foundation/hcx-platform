package org.swasth.dp.coverageeligiblity.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class CoverageEligibilityConfig extends BaseJobConfig {

    private final Config config;

    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String coverageEligibilityConsumer = "coverage-eligibility-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public CoverageEligibilityConfig(Config config, String jobName) {
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
