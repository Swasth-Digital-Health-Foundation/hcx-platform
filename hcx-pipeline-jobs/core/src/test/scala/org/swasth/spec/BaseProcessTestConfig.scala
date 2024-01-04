package org.swasth.spec

import com.typesafe.config.Config
import org.apache.flink.util.OutputTag
import org.swasth.dp.core.job.BaseJobConfig

class BaseProcessTestConfig(override val config: Config) extends BaseJobConfig(config, "Test-job") {
  private val serialVersionUID = -2349318979085017498L
  val mapOutputTag: OutputTag[java.util.Map[String, AnyRef]] = new OutputTag[java.util.Map[String, AnyRef]]("test-map-stream-tag")
  val stringOutputTag: OutputTag[String] =new OutputTag[String]("test-string-stream-tag")

  val kafkaMapInputTopic: String = config.getString("kafka.map.input.topic")
  val kafkaMapOutputTopic: String = config.getString("kafka.map.output.topic")
  val kafkaEventInputTopic: String = config.getString("kafka.event.input.topic")
  val kafkaEventOutputTopic: String = config.getString("kafka.event.output.topic")
  val kafkaEventDuplicateTopic: String = config.getString("kafka.event.duplicate.topic")
  val kafkaStringInputTopic: String = config.getString("kafka.string.input.topic")
  val kafkaStringOutputTopic: String = config.getString("kafka.string.output.topic")

  val testTopics = List(kafkaMapInputTopic, kafkaMapOutputTopic, kafkaEventInputTopic, kafkaEventOutputTopic,
    kafkaStringInputTopic, kafkaStringOutputTopic)

  override val kafkaConsumerParallelism: Int = config.getInt("task.consumer.parallelism")

  val dedupStore: Int = config.getInt("redis.database.duplicationstore.id")
  val cacheExpirySeconds: Int = config.getInt("redis.database.key.expiry.seconds")
  val mapEventCount = "map-event-count"
  val telemetryEventCount = "telemetry-event-count"
  val stringEventCount = "string-event-count"

}