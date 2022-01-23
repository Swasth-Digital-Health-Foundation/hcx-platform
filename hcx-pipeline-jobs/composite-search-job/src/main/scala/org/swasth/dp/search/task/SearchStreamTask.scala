package org.swasth.dp.search.task

import java.io.File
import java.util
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.swasth.dp.core.job.FlinkKafkaConnector
import org.swasth.dp.core.util.FlinkUtil
import org.swasth.dp.search.functions.CompositeSearchFunction

class SearchStreamTask(config: SearchConfig, kafkaConnector: FlinkKafkaConnector) {

  private val serialVersionUID = 146697324640926024L

  def process(): Unit = {
    implicit val env: StreamExecutionEnvironment = FlinkUtil.getExecutionContext(config)
    implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])
    implicit val stringTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])
    val kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic)
    val searchStream =
      env.addSource(kafkaConsumer, config.searchConsumer)
        .uid(config.searchConsumer).setParallelism(config.kafkaConsumerParallelism)
        .rebalance()
        .process(new CompositeSearchFunction(config)).setParallelism(config.downstreamOperatorsParallelism)

    /** Sink for retry events */
    searchStream.getSideOutput(config.retryOutputTag).addSink(kafkaConnector.kafkaStringSink(config.retryTopic)).name(config.retryProducer).uid(config.retryProducer).setParallelism(config.downstreamOperatorsParallelism)

    /** Sink for audit events */
    searchStream.getSideOutput(config.auditOutputTag).addSink(kafkaConnector.kafkaStringSink(config.auditTopic)).name(config.auditProducer).uid(config.auditProducer).setParallelism(config.downstreamOperatorsParallelism)

    Console.println(config.jobName +" is processing")
    env.execute(config.jobName)
  }
}

// $COVERAGE-OFF$ Disabling scoverage as the below code can only be invoked within flink cluster
object SearchStreamTask {
  def main(args: Array[String]): Unit = {
    val configFilePath = Option(ParameterTool.fromArgs(args).get("config.file.path"))
    val config = configFilePath.map {
      path => ConfigFactory.parseFile(new File(path)).resolve()
    }.getOrElse(ConfigFactory.load("search.conf").withFallback(ConfigFactory.systemEnvironment()))
    val pipelinePreprocessorConfig = new SearchConfig(config)
    val kafkaUtil = new FlinkKafkaConnector(pipelinePreprocessorConfig)
    val task = new SearchStreamTask(pipelinePreprocessorConfig, kafkaUtil)
    task.process()
  }
}

// $COVERAGE-ON$
