package org.swasth.job.task;


import org.apache.flink.util.OutputTag;
import org.swasth.job.Platform;

public interface DenormaliserConfig {

    // Kafka Topics Configuration
    String kafkaInputTopic = Platform.getString("kafka.topic.ingest");
    String kafkaOutputTopic = Platform.getString("kafka.topic.denorm");
    String kafkaInvalidTopic = Platform.getString("kafka.topic.invalid");
    String kafkaAuditTopic = Platform.getString("kafka.topic.audit");
    Integer kafkaConsumerParallelism = Platform.getInteger("task.consumer.parallelism");
    Integer parallelism = Platform.getInteger("task.parallelism");

    // Consumers
    String eventConsumer = "denormaliser-consumer";
    String denormaliserFunction = "denormaliser";

    // Producers
    String eventProducer= "denormaliser-producer";

    //Tags
    OutputTag<String> invalidOutTag = new OutputTag<>("invalid-data") {};
    OutputTag<String> auditOutTag = new OutputTag<>("audit-data") {};

}
