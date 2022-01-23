package org.swasth.dp.core.job

import java.util
import java.util.Properties
import java.io.Serializable
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.kafka.clients.consumer.ConsumerConfig

class BaseJobConfig(val config: Config, val jobName: String) extends Serializable {

  private val serialVersionUID = - 4515020556926788923L

  implicit val metricTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])
  implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])

  val kafkaBrokerServers: String = config.getString("kafka.broker-servers")
  val zookeeper: String = config.getString("kafka.zookeeper")
  // Producer Properties
  val kafkaProducerMaxRequestSize: Int = config.getInt("kafka.producer.max-request-size")
  val kafkaProducerBatchSize: Int = config.getInt("kafka.producer.batch.size")
  val kafkaProducerLingerMs: Int = config.getInt("kafka.producer.linger.ms")
  val groupId: String = config.getString("kafka.groupId")
  val restartAttempts: Int = config.getInt("task.restart-strategy.attempts")
  val delayBetweenAttempts: Long = config.getLong("task.restart-strategy.delay")
  val parallelism: Int = config.getInt("task.parallelism")
  val kafkaConsumerParallelism: Int = config.getInt("task.consumer.parallelism")
  // Only for Tests
  val kafkaAutoOffsetReset: Option[String] = if (config.hasPath("kafka.auto.offset.reset")) Option(config.getString("kafka.auto.offset.reset")) else None

  // Redis
  val redisHost: String = Option(config.getString("redis.host")).getOrElse("localhost")
  val redisPort: Int = Option(config.getInt("redis.port")).getOrElse(6379)
  val redisConnectionTimeout: Int = Option(config.getInt("redisdb.connection.timeout")).getOrElse(30000)
  val redisAssetStore: Int = Option(config.getInt("redisdb.assetstore.id")).getOrElse(0)
  val senderReceiverFields = List("signing_cert_path", "primary_mobile","encryption_cert", "endpoint_url", "participant_name","status","roles")

  val metaRedisHost: String = Option(config.getString("redis-meta.host")).getOrElse("localhost")
  val metaRedisPort: Int = Option(config.getInt("redis-meta.port")).getOrElse(6379)

  // Checkpointing config
  val enableCompressedCheckpointing: Boolean = config.getBoolean("task.checkpointing.compressed")
  val checkpointingInterval: Int = config.getInt("task.checkpointing.interval")
  val checkpointingPauseSeconds: Int = config.getInt("task.checkpointing.pause.between.seconds")
  val enableDistributedCheckpointing: Option[Boolean] = if (config.hasPath("job")) Option(config.getBoolean("job.enable.distributed.checkpointing")) else None
  val checkpointingBaseUrl: Option[String] = if (config.hasPath("job")) Option(config.getString("job.statebackend.base.url")) else None

  // Default output configurations
  val enrichedOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("enriched-events")

  val retryOutputTag: OutputTag[String] = OutputTag[String]("retry-events")
  val retryTopic = config.getString("kafka.retry.topic")

  val auditOutputTag: OutputTag[String] = OutputTag[String]("audit-events")
  val auditTopic = config.getString("kafka.audit.topic")

  // Producers
  val retryProducer = "retry-events-sink"
  val auditProducer = "audit-events-sink"

  // Default job metrics
  val dispatcherSuccessCount = "dispatcher-success-count"
  val dispatcherValidationFailedCount = "dispatcher-validation-failed-count"
  val dispatcherValidationSuccessCount = "dispatcher-validation-success-count"
  val dispatcherFailedCount = "dispatcher-failed-count"
  val dispatcherRetryCount = "dispatcher-retry-count"
  val auditEventsCount = "audit-events-count"

  def kafkaConsumerProperties: Properties = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaBrokerServers)
    properties.setProperty("group.id", groupId)
    properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed")
    kafkaAutoOffsetReset.map { properties.setProperty("auto.offset.reset", _) }
    properties
  }

  def kafkaProducerProperties: Properties = {
    val properties = new Properties()
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerServers)
    properties.put(ProducerConfig.LINGER_MS_CONFIG, new Integer(kafkaProducerLingerMs))
    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, new Integer(kafkaProducerBatchSize))
    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")
    properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, new Integer(kafkaProducerMaxRequestSize))
    properties
  }

  //Registry Url
  val registryUrl = config.getString("registry.endPointUrl")

  //Keycloak
  val keycloakUrl = config.getString("keycloak.url")
  val keycloakClientId = config.getString("keycloak.client_id")
  val keycloakUsername = config.getString("keycloak.username")
  val keycloakPassword = config.getString("keycloak.password")
  val keycloakRealm = config.getString("keycloak.realm")
}
