package org.swasth.dp.status.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class StatusSearchConfig extends BaseJobConfig {

    private Config config;

    // From base-config file
    public String postgresUser;
    public String postgresPassword;
    public String postgresDb;
    public String postgresTable;
    public String postgresHost;
    public int postgresPort;
    public int postgresMaxConnections;

    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String statusSearchConsumer = "status-search-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public StatusSearchConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initvalues();
    }

    private void initvalues(){
        postgresUser = config.getString("postgres.user");
        postgresPassword = config.getString("postgres.password");
        postgresDb = config.getString("postgres.database");
        postgresTable = config.getString("postgres.table");
        postgresHost = config.getString("postgres.host");
        postgresPort = config.getInt("postgres.port");
        postgresMaxConnections = config.getInt("postgres.maxConnections");
        kafkaInputTopic = config.getString("kafka.input.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");

    }

}
