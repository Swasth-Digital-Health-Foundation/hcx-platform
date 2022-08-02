package org.swasth.dp.notification.trigger.task;

import com.typesafe.config.Config;
import org.apache.flink.util.OutputTag;
import org.swasth.dp.core.job.BaseJobConfig;

import java.util.List;
import java.util.Map;

public class NotificationTriggerConfig  extends BaseJobConfig {

    private Config config;

    // kafka
    public String kafkaInputTopic;
    public String kafkaOutputTopic;

    // Consumers
    public String notificationTriggerConsumer = "notification-trigger-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;

    // Postgres
    public String subscriptionTableName;

    public OutputTag<String> notifyOutputTag = new OutputTag<String>("notify-events"){};
    public String notifyProducer = "notify-events-sink";
    public boolean networkNotificationsEnabled;
    public List<String> notificationTriggersDisabled;
    public Map<String,Object> topicCodeAndAPIActionMap;

    public NotificationTriggerConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initValues();
    }

    private void initValues() {
        kafkaInputTopic = config.getString("kafka.input.topic");
        kafkaOutputTopic = config.getString("kafka.output.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");
        subscriptionTableName = config.getString("postgres.subscription.table");
        networkNotificationsEnabled = config.getBoolean("notification.network.enabled");
        notificationTriggersDisabled = config.getStringList("notification.triggers.disabled");
        topicCodeAndAPIActionMap = (Map<String, Object>) config.getAnyRef("notification.workflow.topicCodeAndAPIActionMap");
    }
}
