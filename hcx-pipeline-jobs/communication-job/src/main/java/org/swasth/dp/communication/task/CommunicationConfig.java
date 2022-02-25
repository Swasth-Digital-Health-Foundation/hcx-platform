package org.swasth.dp.communication.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class CommunicationConfig extends BaseJobConfig {

    private final Config config;

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
    public String communicationConsumer = "communication-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    public CommunicationConfig(Config config, String jobName) {
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