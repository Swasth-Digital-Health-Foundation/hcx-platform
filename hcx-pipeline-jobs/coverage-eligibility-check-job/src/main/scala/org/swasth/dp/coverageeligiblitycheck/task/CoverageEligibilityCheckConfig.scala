package org.swasth.dp.coverageeligiblitycheck.task

import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.api.scala.OutputTag
import org.swasth.dp.core.job.BaseJobConfig

import scala.collection.JavaConverters._

class CoverageEligibilityCheckConfig(override val config: Config) extends BaseJobConfig(config, "CoverageEligibilityCheckJob") {

  private val serialVersionUID = 2905979434303791379L

  val postgresUser: String = config.getString("postgres.user")
  val postgresPassword: String = config.getString("postgres.password")
  val postgresTable: String = config.getString("postgres.table")
  val postgresDb: String = config.getString("postgres.database")
  val postgresHost: String = config.getString("postgres.host")
  val postgresPort: Int = config.getInt("postgres.port")
  val postgresMaxConnections: Int = config.getInt("postgres.maxConnections")

  val payloadTable = "payload"

  val kafkaInputTopic = "hcx.request.eligibilitycheck"

  // Consumers
  val eligibilityCheckConsumer = "eligibility-check-consumer"
  val downstreamOperatorsParallelism: Int = config.getInt("task.downstream.operators.parallelism")

}
