package org.swasth.job.task;


import org.swasth.job.Platform;

public interface DenormaliserConfig {

    // Kafka Topics Configuration
    String kafkaInputTopic = Platform.getString("kafka.topic.ingest");
    String kafkaOutputTopic = Platform.getString("kafka.topic.denorm");
    Integer kafkaConsumerParallelism = Platform.getInteger("task.consumer.parallelism");
    Integer parallelism = Platform.getInteger("task.parallelism");

    // Consumers
    String eventConsumer = "denormaliser-consumer";
    String denormaliserFunction = "denormaliser";

    // Producers
    String eventProducer= "denormaliser-producer";

}
