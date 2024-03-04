package org.swasth.dp.core.serde

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.swasth.dp.core.util.JSONUtil

import java.nio.charset.StandardCharsets
import java.util
import scala.collection.mutable

class MapDeserializationSchema extends KafkaRecordDeserializationSchema[util.Map[String, AnyRef]] {

  private val serialVersionUID = -3224825136576915426L

  override def getProducedType: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]], out: Collector[util.Map[String, AnyRef]]): Unit = {
    val msg = JSONUtil.deserialize[util.Map[String, AnyRef]](record.value())
    out.collect(msg)
  }
}

class MapSerializationSchema(topic: String, key: Option[String] = None) extends KafkaRecordSerializationSchema[util.Map[String, AnyRef]] {

  private val serialVersionUID = -4284080856874185929L

  override def serialize(element: util.Map[String, AnyRef], context: KafkaRecordSerializationSchema.KafkaSinkContext, timestamp: java.lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
    val out = JSONUtil.serialize(element)
    key.map { kafkaKey =>
      new ProducerRecord[Array[Byte], Array[Byte]](topic, kafkaKey.getBytes(StandardCharsets.UTF_8), out.getBytes(StandardCharsets.UTF_8))
    }.getOrElse(new ProducerRecord[Array[Byte], Array[Byte]](topic, out.getBytes(StandardCharsets.UTF_8)))
  }
}