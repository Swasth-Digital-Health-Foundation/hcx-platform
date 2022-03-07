package org.swasth.dp.searchresponse.task

import com.typesafe.config.Config
import org.swasth.dp.core.job.BaseJobConfig

import java.util

class SearchResponseConfig(override val config: Config) extends BaseJobConfig(config, "SearchResponseJob") {

  private val serialVersionUID = 2905979434303791379L

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
