package org.swasth.job.connector;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.swasth.job.Platform;

public class FlinkKafkaConnector {

    private static final String kafkaServerUrl = Platform.getString("bootstrap-servers","localhost:9092");

    public static KafkaSource<String> kafkaStringSource(String kafkaTopic) {
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(kafkaServerUrl)
                .setTopics(kafkaTopic)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        return source;
    }

    public static KafkaSink<String> kafkaStringSink(String kafkaTopic) {
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaServerUrl)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(kafkaTopic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
        return sink;
    }

}