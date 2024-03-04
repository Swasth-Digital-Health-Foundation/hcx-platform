package org.swasth.dp.core.serde

import java.nio.charset.StandardCharsets

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

class StringDeserializationSchema extends KafkaRecordDeserializationSchema[String] {

  private val serialVersionUID = -3224825136576915426L

  override def getProducedType: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])

  override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]], out: Collector[String]): Unit = {
    out.collect(new String(record.value(), StandardCharsets.UTF_8))
  }
}

class StringSerializationSchema(topic: String, key: Option[String] = None) extends KafkaRecordSerializationSchema[String] {

  private val serialVersionUID = -4284080856874185929L

  override def serialize(element: String, context: KafkaRecordSerializationSchema.KafkaSinkContext, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
    key.map { kafkaKey =>
      new ProducerRecord[Array[Byte], Array[Byte]](topic, kafkaKey.getBytes(StandardCharsets.UTF_8), element.getBytes(StandardCharsets.UTF_8))
    }.getOrElse(new ProducerRecord[Array[Byte], Array[Byte]](topic, element.getBytes(StandardCharsets.UTF_8)))
  }
}
