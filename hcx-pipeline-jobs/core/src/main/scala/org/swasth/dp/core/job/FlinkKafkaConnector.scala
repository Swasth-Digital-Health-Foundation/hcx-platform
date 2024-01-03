package org.swasth.dp.core.job

import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.swasth.dp.core.serde._

import scala.collection.mutable
class FlinkKafkaConnector(config: BaseJobConfig) extends Serializable {

//  def kafkaMapSource(kafkaTopic: String): SourceFunction[util.Map[String, AnyRef]] = {
//    new FlinkKafkaConsumer[util.Map[String, AnyRef]](kafkaTopic, new MapDeserializationSchema, config.kafkaConsumerProperties)
//  }
def kafkaMapSource(kafkaTopic: String): KafkaSource[mutable.Map[String, AnyRef]] = {
  KafkaSource.builder[mutable.Map[String, AnyRef]]()
    .setTopics(kafkaTopic)
    .setDeserializer(new MapDeserializationSchema)
    .setProperties(config.kafkaConsumerProperties)
    .build()
  }
//  def kafkaMapSink(kafkaTopic: String): SinkFunction[util.Map[String, AnyRef]] = {
//    new FlinkKafkaProducer[util.Map[String, AnyRef]](kafkaTopic, new MapSerializationSchema(kafkaTopic), config.kafkaProducerProperties, Semantic.AT_LEAST_ONCE)
//  }
  def kafkaMapSink(kafkaTopic: String): KafkaSink[mutable.Map[String, AnyRef]] = {
    KafkaSink.builder[mutable.Map[String, AnyRef]]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(new MapSerializationSchema(kafkaTopic))
      .setKafkaProducerConfig(config.kafkaProducerProperties)
      .build()
  }
//  def kafkaStringSource(kafkaTopic: String): SourceFunction[String] = {
//    new FlinkKafkaConsumer[String](kafkaTopic, new StringDeserializationSchema, config.kafkaConsumerProperties)
//  }
  def kafkaStringSource(kafkaTopic: String): KafkaSource[String] = {
    KafkaSource.builder[String]()
      .setTopics(kafkaTopic)
      .setDeserializer(new StringDeserializationSchema)
      .setProperties(config.kafkaConsumerProperties)
      .build()
  }
//  def kafkaStringSink(kafkaTopic: String): SinkFunction[String] = {
//    new FlinkKafkaProducer[String](kafkaTopic, new StringSerializationSchema(kafkaTopic), config.kafkaProducerProperties, Semantic.AT_LEAST_ONCE)
//  }
  def kafkaStringSink(kafkaTopic: String): KafkaSink[String] = {
    KafkaSink.builder[String]()
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(new StringSerializationSchema(kafkaTopic))
      .setKafkaProducerConfig(config.kafkaProducerProperties)
      .build()
  }
}