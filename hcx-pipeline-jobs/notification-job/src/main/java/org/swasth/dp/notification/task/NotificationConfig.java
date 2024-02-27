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
        subCertRevocation = config.getString("notif-certificate-revocation.subject");
        subDeboarded = config.getString("notif-participant-de-boarded.subject");
        subOnboarded = config.getString("notif-participant-onboarded.subject");
        subEncKeyExpiry = config.getString("notif-encryption-key-expiry.subject");
        subFeatRemoved = config.getString("notif-network-feature-removed.subject");
        subPolicyUpdate = config.getString("notif-gateway-policy-sla-change.subject");
        subGatewayDowntime = config.getString("notif-gateway-downtime.subject");
        subFeatAdded = config.getString("notif-new-network-feature-added.subject");
    }

}
