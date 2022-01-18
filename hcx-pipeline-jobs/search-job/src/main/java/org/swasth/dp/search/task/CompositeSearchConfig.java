package org.swasth.dp.search.task;

import com.typesafe.config.Config;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.scala.OutputTag;
import org.swasth.dp.core.job.BaseJobConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeSearchConfig extends BaseJobConfig {


    private Config searchConfig = null;


    public CompositeSearchConfig(Config config, String jobName) {
        super(config, jobName);
        this.searchConfig = config;
        initConfigValues();
    }

    private void initConfigValues() {
        setSearchTable(searchConfig.getString("postgres.search"));
        setPostgresTable(searchConfig.getString("postgres.table"));
        setPostgresDb(searchConfig.getString("postgres.database"));
        setKafkaInputTopic(searchConfig.getString("kafka.input.topic"));
        setKafkaAuditTopic(searchConfig.getString("kafka.audit.topic"));
        setKafkaRetryTopic(searchConfig.getString("kafka.retry.topic"));
        setSearchGroup(searchConfig.getString("kafka.groupId"));
        setConsumerParallelism(searchConfig.getInt("task.consumer.parallelism"));
        setDownstreamOperatorsParallelism(searchConfig.getInt("task.downstream.operators.parallelism"));
        setTimePeriod(searchConfig.getInt("search.time.period"));
        setMaxTimePeriod(searchConfig.getInt("search.time.maxperiod"));
        setEntityTypes(searchConfig.getStringList("search.entity.types"));
        setSearchExpiry(searchConfig.getLong("search.expiry.time"));
        setHcxRegistryCode(searchConfig.getString("registry.hcx.code"));
        setPostgresUser(searchConfig.getString("postgres.user"));
        setPostgresPassword(searchConfig.getString("postgres.password"));
        setPostgresHost(searchConfig.getString("postgres.host"));
        setPostgresPort(searchConfig.getInt("postgres.port"));
        setPostgresMaxConnections(searchConfig.getInt("postgres.maxConnections"));
    }

    public static final String auditSearch = "audit-search-sink";
    private String searchTable;
    private String postgresTable;
    private String postgresDb;

    private String kafkaInputTopic;
    private String kafkaAuditTopic;
    private String kafkaRetryTopic;
    private String searchGroup;

    // Consumers
    private String searchConsumer = "search-consumer";
    private int consumerParallelism;
    private int downstreamOperatorsParallelism;

    private int timePeriod;
    private int maxTimePeriod;
    private List<String>  entityTypes;
    private long searchExpiry;
    private String hcxRegistryCode;

    private String postgresUser;
    private String postgresPassword;
    private String postgresHost;
    private int postgresPort;
    private int postgresMaxConnections;

    public String getPostgresUser() {
        return postgresUser;
    }

    public void setPostgresUser(String postgresUser) {
        this.postgresUser = postgresUser;
    }

    public String getPostgresPassword() {
        return postgresPassword;
    }

    public void setPostgresPassword(String postgresPassword) {
        this.postgresPassword = postgresPassword;
    }

    public String getPostgresHost() {
        return postgresHost;
    }

    public void setPostgresHost(String postgresHost) {
        this.postgresHost = postgresHost;
    }

    public int getPostgresPort() {
        return postgresPort;
    }

    public void setPostgresPort(int postgresPort) {
        this.postgresPort = postgresPort;
    }

    public int getPostgresMaxConnections() {
        return postgresMaxConnections;
    }

    public void setPostgresMaxConnections(int postgresMaxConnections) {
        this.postgresMaxConnections = postgresMaxConnections;
    }

    public String getSearchTable() {
        return searchTable;
    }

    public void setSearchTable(String searchTable) {
        this.searchTable = searchTable;
    }

    public String getPostgresTable() {
        return postgresTable;
    }

    public void setPostgresTable(String postgresTable) {
        this.postgresTable = postgresTable;
    }

    public String getPostgresDb() {
        return postgresDb;
    }

    public void setPostgresDb(String postgresDb) {
        this.postgresDb = postgresDb;
    }

    public String getKafkaInputTopic() {
        return kafkaInputTopic;
    }

    public void setKafkaInputTopic(String kafkaInputTopic) {
        this.kafkaInputTopic = kafkaInputTopic;
    }

    public String getKafkaAuditTopic() {
        return kafkaAuditTopic;
    }

    public void setKafkaAuditTopic(String kafkaAuditTopic) {
        this.kafkaAuditTopic = kafkaAuditTopic;
    }

    public String getKafkaRetryTopic() {
        return kafkaRetryTopic;
    }

    public void setKafkaRetryTopic(String kafkaRetryTopic) {
        this.kafkaRetryTopic = kafkaRetryTopic;
    }

    public String getSearchGroup() {
        return searchGroup;
    }

    public void setSearchGroup(String searchGroup) {
        this.searchGroup = searchGroup;
    }

    public String getSearchConsumer() {
        return searchConsumer;
    }

    public void setSearchConsumer(String searchConsumer) {
        this.searchConsumer = searchConsumer;
    }

    public int getConsumerParallelism() {
        return consumerParallelism;
    }

    public void setConsumerParallelism(int consumerParallelism) {
        this.consumerParallelism = consumerParallelism;
    }

    public int getDownstreamOperatorsParallelism() {
        return downstreamOperatorsParallelism;
    }

    public void setDownstreamOperatorsParallelism(int downstreamOperatorsParallelism) {
        this.downstreamOperatorsParallelism = downstreamOperatorsParallelism;
    }

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public int getMaxTimePeriod() {
        return maxTimePeriod;
    }

    public void setMaxTimePeriod(int maxTimePeriod) {
        this.maxTimePeriod = maxTimePeriod;
    }

    public List<String> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public long getSearchExpiry() {
        return searchExpiry;
    }

    public void setSearchExpiry(long searchExpiry) {
        this.searchExpiry = searchExpiry;
    }

    public String getHcxRegistryCode() {
        return hcxRegistryCode;
    }

    public void setHcxRegistryCode(String hcxRegistryCode) {
        this.hcxRegistryCode = hcxRegistryCode;
    }

    private TypeInformation<Map<String,Object>> mapTypeInfo  = TypeExtractor.getForClass(mapTypeInfo().getTypeClass());

    public OutputTag<Map<String,Object>> searchRequestOutputTag = new OutputTag<Map<String,Object>>("search-request-events", mapTypeInfo);
    public OutputTag<Map<String,Object>> searchResponseOutputTag = new OutputTag<Map<String,Object>>("search-response-events", mapTypeInfo);
    public OutputTag<Map<String,Object>> searchCompleteResponseOutputTag = new OutputTag<Map<String,Object>>("search-complete-response-events", mapTypeInfo);
}
