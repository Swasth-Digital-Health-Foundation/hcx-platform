package org.swasth.dp.coverageeligiblitycheck.task

import com.typesafe.config.Config
import org.swasth.dp.core.job.BaseJobConfig

class CoverageEligibilityCheckConfig(override val config: Config) extends BaseJobConfig(config, "CoverageEligibilityCheckJob") {

  private val serialVersionUID = 2905979434303791379L

  val kafkaInputTopic = config.getString("kafka.input.topic")

  // Consumers
  val eligibilityCheckConsumer = "eligibility-check-consumer"
  val downstreamOperatorsParallelism: Int = config.getInt("task.downstream.operators.parallelism")

}
