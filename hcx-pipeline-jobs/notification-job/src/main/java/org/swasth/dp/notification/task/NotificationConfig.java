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
    // Consumers
    public String notificationConsumer = "notification-consumer";
    public String subscriptionConsumer = "subscription-consumer";
    public String onSubscriptionConsumer = "on-subscription-consumer";
    public String notificationMessageProducer = "message-events-sink";
    public OutputTag<String> messageOutputTag = new OutputTag<String>("notify-message-events"){};
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;
    public int dispatcherParallelism;
    public String messageTopic;
    public boolean emailNotificationEnabled;
    public String kafkaServiceUrl;
    // Postgres
    public String subscriptionTableName;
    public String subGatewayDowntime;
    public String subEncKeyExpiry;
    public String subOnboarded;
    public String subDeboarded;
    public String subFeatRemoved;
    public String subPolicyUpdate;
    public String subCertRevocation;
    public String subFeatAdded;

    public NotificationConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initValues();
    }

    private void initValues() {
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
        subCertRevocation = config.getString("sub.notif-certificate-revocation");
        subDeboarded = config.getString("sub.notif-participant-de-boarded");
        subOnboarded = config.getString("sub.notif-participant-onboarded");
        subEncKeyExpiry = config.getString("sub.notif-encryption-key-expiry");
        subFeatRemoved = config.getString("sub.notif-network-feature-removed");
        subPolicyUpdate = config.getString("sub.notif-gateway-policy-sla-change");
        subGatewayDowntime = config.getString("sub.notif-gateway-downtime");
        subFeatAdded = config.getString("sub.notif-new-network-feature-added");
    }

}
