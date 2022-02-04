package org.swasth.dp.searchresponse.task

import com.typesafe.config.Config
import org.swasth.dp.core.job.BaseJobConfig

import java.util

class SearchResponseConfig(override val config: Config) extends BaseJobConfig(config, "SearchResponseJob") {

  private val serialVersionUID = 2905979434303791379L

  val postgresUser: String = config.getString("postgres.user")
  val postgresPassword: String = config.getString("postgres.password")
  val postgresTable: String = config.getString("postgres.table")
  val postgresDb: String = config.getString("postgres.database")
  val postgresHost: String = config.getString("postgres.host")
  val postgresPort: Int = config.getInt("postgres.port")
  val postgresMaxConnections: Int = config.getInt("postgres.maxConnections")

  val payloadTable: String = config.getString("postgres.table")

  val kafkaInputTopic: String = config.getString("kafka.input.topic")

  // Consumers
  val searchResponseConsumer = "search-response-consumer"
  val downstreamOperatorsParallelism: Int = config.getInt("task.downstream.operators.parallelism")

  //Search configurations
  val searchTime: Int = config.getInt("search.time.period")
  val searchMaxTime: Int = config.getInt("search.time.maxperiod")
  val entityList: util.List[String] = config.getStringList("search.entity.types")
  val expiryTime: Long = config.getLong("search.expiry.time")

  val searchTable: String = config.getString("postgres.search")

}
