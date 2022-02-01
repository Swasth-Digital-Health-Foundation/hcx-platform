package org.swasth.dp.auditindexer.task;

import com.typesafe.config.Config;
import org.swasth.dp.core.job.BaseJobConfig;

public class AuditIndexerConfig extends BaseJobConfig {

    private Config config;

    // kafka
    public String kafkaInputTopic;

    // Consumers
    public String auditIndexerConsumer = "audit-indexer-consumer";
    public int consumerParallelism;
    public int parallelism;

    // Elastic Search Config
    public String esUrl;
    public String timeZone;
    public String auditIndex = "hcx_audit";
    public String auditAlias = "hcx_audit";
    public int batchSize = 1000;

    public AuditIndexerConfig(Config config, String jobName) {
        super(config, jobName);
        this.config = config;
        initValues();
    }

    private void initValues(){
        kafkaInputTopic = config.getString("kafka.input.topic");
        consumerParallelism = config.getInt("task.consumer.parallelism");
        parallelism = config.getInt("task.parallelism");
        esUrl = config.getString("es.basePath");
        timeZone =  config.hasPath("timezone") ? config.getString("timezone") : "IST";
    }

}
