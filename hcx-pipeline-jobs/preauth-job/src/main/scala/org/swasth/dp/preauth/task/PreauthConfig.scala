package org.swasth.dp.preauth.task

import com.typesafe.config.Config
import org.swasth.dp.core.job.BaseJobConfig

class PreauthConfig(override val config: Config) extends BaseJobConfig(config, "PreauthJob") {

  private val serialVersionUID = 2905979434303791379L

  val kafkaInputTopic: String = config.getString("kafka.input.topic")

  // Consumers
  val eligibilityCheckConsumer = "preauth-consumer"
  val downstreamOperatorsParallelism: Int = config.getInt("task.downstream.operators.parallelism")

}
