package org.swasth.dp.preauth.task;


import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class PreauthConfig extends BaseJobConfig {

  private final Config config;

  // kafka
  public String kafkaInputTopic;

  // Consumers
  public String preauthConsumer = "preauth-consumer";
  public int consumerParallelism;
  public int downstreamOperatorsParallelism;

  public PreauthConfig(Config config, String jobName) {
    super(config, jobName);
    this.config = config;
    initValues();
  }

  private void initValues() {
    kafkaInputTopic = config.getString("kafka.input.topic");
    consumerParallelism = config.getInt("task.consumer.parallelism");
    downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");
  }

}
