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
  implicit val objectTypeInfo: TypeInformation[Object] = TypeExtractor.getForClass(classOf[Object])

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
  val senderReceiverFields = List("signing_cert_path", "primary_mobile","encryption_cert", "endpoint_url", "participant_name","status","roles","primary_email")
  val redisExpires: Int = Option(config.getInt("redis.expires")).getOrElse(3600)

  // Checkpointing config
  val enableCompressedCheckpointing: Boolean = config.getBoolean("task.checkpointing.compressed")
  val checkpointingInterval: Int = config.getInt("task.checkpointing.interval")
  val checkpointingPauseSeconds: Int = config.getInt("task.checkpointing.pause.between.seconds")
  val enableDistributedCheckpointing: Option[Boolean] = if (config.hasPath("job")) Option(config.getBoolean("job.enable.distributed.checkpointing")) else None
  val checkpointingBaseUrl: Option[String] = if (config.hasPath("job")) Option(config.getString("job.statebackend.base.url")) else None
  val checkpointingTimeout: Long = if (config.hasPath("task.checkpointing.timeout")) config.getLong("task.checkpointing.timeout") else 1800000L

  // Default output configurations
  val enrichedOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("enriched-events")
  val dispatcherOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("dispatched-events")
  val enrichedSubscriptionsOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("enriched-subscription-events")
  val auditOutputTag: OutputTag[String] = OutputTag[String]("audit-events")
  val auditTopic = if (config.hasPath("kafka.audit.topic")) config.getString("kafka.audit.topic") else ""

  // Producers
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
  val hcxRegistryCode: String = config.getString("registry.hcx.code")

  // HCX-APIs Url
  val hcxApisUrl = config.getString("hcx-apis.endPointUrl")

  // JWT Token
  val privateKey: String = config.getString("jwt-token.privateKey")
  val expiryTime: Long = config.getLong("jwt-token.expiryTime")

  //Postgres
  val postgresUser: String = config.getString("postgres.user")
  val postgresPassword: String = config.getString("postgres.password")
  val postgresTable: String = config.getString("postgres.table")
  val postgresDb: String = config.getString("postgres.database")
  val postgresHost: String = config.getString("postgres.host")
  val postgresPort: Int = config.getInt("postgres.port")
  val postgresMaxConnections: Int = config.getInt("postgres.maxConnections")

  val maxRetry: Int = config.getInt("max.retry")
  val allowedEntitiesForRetry: util.List[String] = config.getStringList("allowedEntitiesForRetry")

  // Elastic Search Config
  val esUrl: String = config.getString("es.basePath")
  val batchSize: Int = config.getInt("es.batchSize")
  val timeZone: String = config.getString("audit.timezone")
  val auditIndex: String = config.getString("audit.index")
  val auditAlias: String = config.getString("audit.alias")

  //HTTP-Codes
  val successCodes: util.List[Integer] = config.getIntList("errorCodes.successCodes")
  val errorCodes: util.List[Integer] = config.getIntList("errorCodes.errorCodes")

  val subscribeOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("subscribed-events")
  val onSubscribeOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("on-subscribed-events")

  val hcxInstanceName: String = config.getString("hcx.instanceName")

}
