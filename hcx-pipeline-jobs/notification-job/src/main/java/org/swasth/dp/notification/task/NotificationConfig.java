package org.swasth.dp.notification.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class NotificationConfig extends BaseJobConfig {

    private Config config;

    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String notificationConsumer = "notification-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    // Postgres
    public String subscriptionTableName;

    public String notificationDispatchAPI;

    public NotificationConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initValues();
    }

    private void initValues(){
        kafkaInputTopic = config.getString("kafka.input.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");
        subscriptionTableName = config.getString("postgres.subscription.table");
        notificationDispatchAPI = config.getString("notification.dispatch.api");
    }

}
