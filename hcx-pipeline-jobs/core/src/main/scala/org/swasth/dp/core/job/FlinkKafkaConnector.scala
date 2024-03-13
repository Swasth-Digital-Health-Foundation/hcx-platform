package org.swasth.dp.core.job

import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.swasth.dp.core.serde._

import java.util._
class FlinkKafkaConnector(config: BaseJobConfig) extends Serializable {

def kafkaMapSource(kafkaTopic: String): KafkaSource[java.util.Map[String, AnyRef]] = {
  KafkaSource.builder[java.util.Map[String, AnyRef]]()
    .setTopics(kafkaTopic)
    .setDeserializer(new MapDeserializationSchema)
    .setProperties(config.kafkaConsumerProperties)
    .build()
  }

  def kafkaMapSink(kafkaTopic: String): KafkaSink[java.util.Map[String, AnyRef]] = {
    KafkaSink.builder[java.util.Map[String, AnyRef]]()
      .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(new MapSerializationSchema(kafkaTopic))
      .setKafkaProducerConfig(config.kafkaProducerProperties)
      .build()
  }

  def kafkaStringSource(kafkaTopic: String): KafkaSource[String] = {
    KafkaSource.builder[String]()
      .setTopics(kafkaTopic)
      .setDeserializer(new StringDeserializationSchema)
      .setProperties(config.kafkaConsumerProperties)
      .build()
  }

  def kafkaStringSink(kafkaTopic: String): KafkaSink[String] = {
    KafkaSink.builder[String]()
      .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(new StringSerializationSchema(kafkaTopic))
      .setKafkaProducerConfig(config.kafkaProducerProperties)
      .build()
  }
}