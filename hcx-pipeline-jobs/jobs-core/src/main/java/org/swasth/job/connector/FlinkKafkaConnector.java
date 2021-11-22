package org.swasth.job.connector;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.swasth.job.Platform;

import java.util.Properties;

public class FlinkKafkaConnector {

    private final String kafkaBrokerServers = Platform.getString("bootstrap-servers","localhost:9092");

    public FlinkKafkaConsumer<String> kafkaStringSource(String kafkaTopic) {
        return new FlinkKafkaConsumer<String>(kafkaTopic, new SimpleStringSchema(), kafkaConsumerProperties());
    }

    public FlinkKafkaProducer<String> kafkaStringSink(String kafkaTopic) {
        return new FlinkKafkaProducer<String>(kafkaTopic, new SimpleStringSchema(), kafkaProducerProperties());
    }

    public Properties kafkaConsumerProperties(){
        Properties properties= new Properties();
        properties.put("bootstrap.servers", kafkaBrokerServers);
        return properties;
    }

    public Properties kafkaProducerProperties(){
        Properties properties= new Properties();
        properties.put("bootstrap.servers", kafkaBrokerServers);
        return properties;
    }

}