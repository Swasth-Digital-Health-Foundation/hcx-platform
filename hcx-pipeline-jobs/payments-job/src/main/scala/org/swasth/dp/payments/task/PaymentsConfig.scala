package org.swasth.dp.payments.task

import com.typesafe.config.Config
import org.swasth.dp.core.job.BaseJobConfig

class PaymentsConfig(override val config: Config) extends BaseJobConfig(config, "PaymentsJob") {

  private val serialVersionUID = 2905979434303791379L

  val kafkaInputTopic = config.getString("kafka.input.topic")

  // Consumers
  val eligibilityCheckConsumer = "payments-consumer"
  val downstreamOperatorsParallelism: Int = config.getInt("task.downstream.operators.parallelism")

}
