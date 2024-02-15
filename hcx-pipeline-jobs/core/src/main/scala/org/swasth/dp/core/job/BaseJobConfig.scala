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
  val senderReceiverFields = List("signing_cert_path", "primary_mobile","encryption_cert", "endpoint_url", "participant_name","status","roles","primary_email", "participant_code")
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
//  val messageOutputTag: OutputTag[util.Map[String, AnyRef]] = OutputTag[util.Map[String, AnyRef]]("message-events")
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

  // Dispatch success metrics
  val coverageEligibilityDispatcherSuccessCount = "coverage-eligibility-dispatcher-success-count"
  val coverageEligibilityOnDispatcherSuccessCount = "coverage-eligibility-on-dispatcher-success-count"
  val preAuthDispatcherSuccessCount = "pre-auth-dispatcher-success-count"
  val preAuthOnDispatcherSuccessCount = "pre-auth-dispatcher-on-success-count"
  val predeterminationDispatcherSuccessCount = "predetermination-dispatcher-success-count"
  val predeterminationOnDispatcherSuccessCount = "predetermination-dispatcher-on-success-count"
  val claimDispatcherSuccessCount = "claim-dispatcher-success-count"
  val claimOnDispatcherSuccessCount = "claim-dispatcher-on-success-count"
  val communicationDispatcherSuccessCount = "communication-dispatcher-success-count"
  val communicationOnDispatcherSuccessCount = "communication-dispatcher-on-success-count"
  val paymentDispatcherSuccessCount = "payment-dispatcher-success-count"
  val paymentOnDispatcherSuccessCount = "payment-dispatcher-on-success-count"
  val fetchDispatcherSuccessCount = "fetch-dispatcher-success--count"
  val fetchOnDispatcherSuccessCount = "fetch-dispatcher-on-success-count"
  val searchDispatcherSuccessCount = "search-dispatcher-success-count"
  val searchOnDispatcherSuccessCount = "search-dispatcher-on-success-count"
  val retryDispatcherSuccessCount = "retry-dispatcher-success-count"


  // Dispatch failed metrics
  val coverageEligibilityDispatcherFailedCount = "coverage-eligibility-dispatcher-failed-count"
  val coverageEligibilityOnDispatcherFailedCount = "coverage-eligibility-on-dispatcher-failed-count"
  val preAuthDispatcherFailedCount = "pre-auth-dispatcher-failed-count"
  val preAuthOnDispatcherFailedCount = "pre-auth-dispatcher-on-Failed-count"
  val predeterminationDispatcherFailedCount = "predetermination-dispatcher-failed-count"
  val predeterminationOnDispatcherFailedCount = "predetermination-dispatcher-on-success-count"
  val claimDispatcherFailedCount = "claim-dispatcher-failed-count"
  val claimOnDispatcherFailedCount = "claim-dispatcher-on-failed-count"
  val communicationDispatcherFailedCount = "communication-dispatcher-failed-count"
  val communicationOnDispatcherFailedCount = "communication-dispatcher-on-failed-count"
  val paymentDispatcherFailedCount = "payment-dispatcher-failed-count"
  val paymentOnDispatcherFailedCount = "payment-dispatcher-on-failed-count"
  val fetchDispatcherFailedCount = "fetch-dispatcher-failed--count"
  val fetchOnDispatcherFailedCount = "fetch-dispatcher-on-failed-count"
  val searchDispatcherFailedCount = "search-dispatcher-failed-count"
  val searchOnDispatcherFailedCount = "search-dispatcher-on-failed-count"
  val retryDispatcherFailedCount = "retry-dispatcher-failed-count"

  // Dispatch retry metrics
  val coverageEligibilityDispatcherRetryCount = "coverage-eligibility-dispatcher-retry-count"
  val coverageEligibilityOnDispatcherRetryCount = "coverage-eligibility-on-dispatcher-retry-count"
  val preAuthDispatcherRetryCount = "pre-auth-dispatcher-retry-count"
  val preAuthOnDispatcherRetryCount = "pre-auth-dispatcher-on-retry-count"
  val predeterminationDispatcherRetryCount = "predetermination-dispatcher-retry-count"
  val predeterminationOnDispatcherRetryCount = "predetermination-dispatcher-on-retry-count"
  val claimDispatcherRetryCount = "claim-dispatcher-retry-count"
  val claimOnDispatcherRetryCount = "claim-dispatcher-on-retry-count"
  val communicationDispatcherRetryCount = "communication-dispatcher-retry-count"
  val communicationOnDispatcherRetryCount = "communication-dispatcher-on-retry-count"
  val paymentDispatcherRetryCount = "payment-dispatcher-retry-count"
  val paymentOnDispatcherRetryCount = "payment-dispatcher-on-retry-count"
  val fetchDispatcherRetryCount = "fetch-dispatcher-retry--count"
  val fetchOnDispatcherRetryCount = "fetch-dispatcher-on-retry-count"
  val searchDispatcherRetryCount = "search-dispatcher-retry-count"
  val searchOnDispatcherRetryCount = "search-dispatcher-on-retry-count"
  val retryDispatcherRetryCount = "retry-dispatcher-retry-count"

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

  //tag configuration
  val tag: String = config.getString("tag")
}
