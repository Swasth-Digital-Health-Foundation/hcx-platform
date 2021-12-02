package org.swasth.dp.coverageeligiblitycheck.task

import java.io.File
import java.util
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.swasth.dp.core.function.ContextEnrichmentFunction
import org.swasth.dp.core.job.FlinkKafkaConnector
import org.swasth.dp.core.util.FlinkUtil
import org.swasth.dp.coverageeligiblitycheck.functions.CoverageEligibilityProcessFunction

class CoverageEligibilityCheckStreamTask(config: CoverageEligibilityCheckConfig, kafkaConnector: FlinkKafkaConnector) {

  private val serialVersionUID = 146697324640926024L

  def process(): Unit = {
    implicit val env: StreamExecutionEnvironment = FlinkUtil.getExecutionContext(config)
    implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])
    implicit val stringTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])
    val kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic)
    val enrichedStream =
      env.addSource(kafkaConsumer, config.eligibilityCheckConsumer)
        .uid(config.eligibilityCheckConsumer).setParallelism(config.kafkaConsumerParallelism)
        .rebalance()
        .process(new ContextEnrichmentFunction(config)).setParallelism(config.downstreamOperatorsParallelism)


    val eventStream = enrichedStream.getSideOutput(config.enrichedOutputTag)
        .process(new CoverageEligibilityProcessFunction(config)).setParallelism(config.downstreamOperatorsParallelism)

    /** Sink for retry events */
    eventStream.getSideOutput(config.retryOutputTag).addSink(kafkaConnector.kafkaMapSink(config.retryTopic)).name(config.retryProducer).uid(config.retryProducer).setParallelism(config.downstreamOperatorsParallelism)

    /** Sink for audit events */
    eventStream.getSideOutput(config.auditOutputTag).addSink(kafkaConnector.kafkaMapSink(config.auditTopic)).name(config.auditProducer).uid(config.auditProducer).setParallelism(config.downstreamOperatorsParallelism)

    env.execute(config.jobName)
  }
}

// $COVERAGE-OFF$ Disabling scoverage as the below code can only be invoked within flink cluster
object CoverageEligibilityCheckStreamTask {
  def main(args: Array[String]): Unit = {
    val configFilePath = Option(ParameterTool.fromArgs(args).get("config.file.path"))
    val config = configFilePath.map {
      path => ConfigFactory.parseFile(new File(path)).resolve()
    }.getOrElse(ConfigFactory.load("coverage-eligibility-check.conf").withFallback(ConfigFactory.systemEnvironment()))
    val pipelinePreprocessorConfig = new CoverageEligibilityCheckConfig(config)
    val kafkaUtil = new FlinkKafkaConnector(pipelinePreprocessorConfig)
    val task = new CoverageEligibilityCheckStreamTask(pipelinePreprocessorConfig, kafkaUtil)
    task.process()
  }
}

// $COVERAGE-ON$
