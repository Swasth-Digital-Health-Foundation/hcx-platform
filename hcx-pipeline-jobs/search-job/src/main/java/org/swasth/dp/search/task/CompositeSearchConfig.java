package org.swasth.dp.search.task;

import com.typesafe.config.Config;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.scala.OutputTag;
import org.swasth.dp.core.job.BaseJobConfig;

import java.util.List;
import java.util.Map;

public class CompositeSearchConfig extends BaseJobConfig {


    private Config searchConfig = null;

    public CompositeSearchConfig(Config config, String jobName) {
        super(config, jobName);
        this.searchConfig = config;
    }


    public String searchTable = searchConfig.getString("postgres.search");
    public String postgresTable = searchConfig.getString("postgres.table");
    public String postgresDb = searchConfig.getString("postgres.database");

    //From base-config file
    public String postgresUser = searchConfig.getString("postgres.user");
    public String postgresPassword = searchConfig.getString("postgres.password");
    public String postgresHost = searchConfig.getString("postgres.host");
    public int postgresPort = searchConfig.getInt("postgres.port");
    public int postgresMaxConnections = searchConfig.getInt("postgres.maxConnections");


    public String kafkaInputTopic = searchConfig.getString("kafka.input.topic");
    public String kafkaAuditTopic = searchConfig.getString("kafka.audit.topic");
    public String kafkaRetryTopic = searchConfig.getString("kafka.retry.topic");
    public String searchGroup = searchConfig.getString("kafka.groupId");

    // Consumers
    public String searchConsumer = "search-consumer";
    public int consumerParallelism = searchConfig.getInt("task.consumer.parallelism");
    public int downstreamOperatorsParallelism = searchConfig.getInt("task.downstream.operators.parallelism");

    public int timePeriod = searchConfig.getInt("search.time.period");
    public int maxTimePeriod = searchConfig.getInt("search.time.maxperiod");
    public List<String>  entityTypes = searchConfig.getStringList("entity.types");
    public long searchExpiry = searchConfig.getLong("search.expiry.time");


    private TypeInformation<Map<String,Object>> mapTypeInfo  = TypeExtractor.getForClass(mapTypeInfo().getTypeClass());
    public OutputTag<Map<String,Object>> searchOutputTag = new OutputTag<Map<String,Object>>("search-events", mapTypeInfo);
    public OutputTag<Map<String,Object>> searchResponseOutputTag = new OutputTag<Map<String,Object>>("search-response-events", mapTypeInfo);

    public String hcxRegistryCode = searchConfig.getString("registry.hcx.code");
}
