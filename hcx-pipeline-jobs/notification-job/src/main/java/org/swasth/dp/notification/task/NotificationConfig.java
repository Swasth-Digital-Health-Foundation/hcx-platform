package org.swasth.dp.notification.task;

import com.typesafe.config.Config;
import org.apache.flink.util.OutputTag;
import org.swasth.dp.core.job.BaseJobConfig;

public class NotificationConfig extends BaseJobConfig {

    private Config config;

    // kafka
    public String kafkaInputTopic;
    public String subscriptionInputTopic;
    public String onSubscriptionInputTopic;

    public OutputTag<String> messageOutputTag = new OutputTag<String>("message-events"){};
    // Consumers
    public String notificationConsumer = "notification-consumer";
    public String subscriptionConsumer = "subscription-consumer";
    public String onSubscriptionConsumer = "on-subscription-consumer";
    public String notificationProducer = "message-events-sink";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;
    public int dispatcherParallelism;
    public String messageTopic;
    public boolean emailNotificationEnabled;
    public String kafkaServiceUrl;
    // Postgres
    public String subscriptionTableName;

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
        dispatcherParallelism = config.getInt("task.downstream.operators.dispatcher.parallelism");
        subscriptionInputTopic = config.getString("kafka.subscription.input.topic");
        onSubscriptionInputTopic = config.getString("kafka.onsubscription.input.topic");
        kafkaServiceUrl = config.getString("kafka.bootstrap.servers");
        emailNotificationEnabled = config.getBoolean("kafka.email.notification.enabled");
        messageTopic = config.getString("kafka.message.topic");
    }

}
