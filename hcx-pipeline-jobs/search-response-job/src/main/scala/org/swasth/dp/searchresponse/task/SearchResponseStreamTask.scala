package org.swasth.dp.searchresponse.task

import java.io.File
import java.util
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.swasth.dp.core.job.FlinkKafkaConnector
import org.swasth.dp.core.util.FlinkUtil
import org.swasth.dp.searchresponse.functions.SearchResponseFunction

class SearchResponseStreamTask(config: SearchResponseConfig, kafkaConnector: FlinkKafkaConnector) {

  private val serialVersionUID = 146697324640926024L

  def process(): Unit = {
    implicit val env: StreamExecutionEnvironment = FlinkUtil.getExecutionContext(config)
    implicit val mapTypeInfo: TypeInformation[util.Map[String, AnyRef]] = TypeExtractor.getForClass(classOf[util.Map[String, AnyRef]])
    implicit val stringTypeInfo: TypeInformation[String] = TypeExtractor.getForClass(classOf[String])
    val kafkaConsumer = kafkaConnector.kafkaMapSource(config.kafkaInputTopic)
    env.addSource(kafkaConsumer, config.searchResponseConsumer)
        .uid(config.searchResponseConsumer).setParallelism(config.kafkaConsumerParallelism)
        .rebalance()
        .process(new SearchResponseFunction(config)).setParallelism(config.downstreamOperatorsParallelism)

    Console.println(config.jobName +" is processing")
    env.execute(config.jobName)
  }
}

// $COVERAGE-OFF$ Disabling scoverage as the below code can only be invoked within flink cluster
object SearchResponseStreamTask {
  def main(args: Array[String]): Unit = {
    val configFilePath = Option(ParameterTool.fromArgs(args).get("config.file.path"))
    val config = configFilePath.map {
      path => ConfigFactory.parseFile(new File(path)).resolve()
    }.getOrElse(ConfigFactory.load("search-response.conf").withFallback(ConfigFactory.systemEnvironment()))
    val pipelinePreprocessorConfig = new SearchResponseConfig(config)
    val kafkaUtil = new FlinkKafkaConnector(pipelinePreprocessorConfig)
    val task = new SearchResponseStreamTask(pipelinePreprocessorConfig, kafkaUtil)
    task.process()
  }
}

// $COVERAGE-ON$
