package org.swasth.dp.message.service.task;

import com.typesafe.config.Config;
import org.apache.flink.util.OutputTag;
import org.swasth.dp.core.job.BaseJobConfig;

import java.util.Map;

public class MessageServiceConfig extends BaseJobConfig {

    private Config config;

    // kafka
    public String inputTopic;

    // Consumers
    public String messageServiceConsumer = "message-service-consumer";
    public int consumerParallelism;
    public int downstreamOperatorsParallelism;
    public int emailDispatcherParallelism;
    public int smsDispatcherParallelism;
    public String awsAccessKey;
    public String awsAccessSecret;
    public String awsRegion;
    public String emailId;
    public String emailPwd;

    public String onboardIndex;

    public String onboardIndexAlias;

    public OutputTag<Map<String,Object>> emailOutputTag = new OutputTag<>("email-events") {
    };

    public OutputTag<Map<String,Object>> smsOutputTag = new OutputTag<>("sms-events"){};

    public MessageServiceConfig(Config config, String jobName) {
        super(config,jobName);
        this.config = config;
        initValues();
    }

    public void initValues(){
        inputTopic = config.getString("kafka.input.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        downstreamOperatorsParallelism = config.getInt("task.downstream.operators.parallelism");
        emailDispatcherParallelism = config.getInt("task.email.dispatcher.parallelism");
        smsDispatcherParallelism = config.getInt("task.sms.dispatcher.parallelism");
        awsAccessKey = config.getString("aws.access-key");
        awsAccessSecret = config.getString("aws.access-secret");
        awsRegion = config.getString("aws.region");
        emailId = config.getString("email.id");
        emailPwd = config.getString("email.pwd");
        onboardIndex = config.getString("audit.onboard.index");
        onboardIndexAlias = config.getString("audit.onboard.alias");
    }
}
