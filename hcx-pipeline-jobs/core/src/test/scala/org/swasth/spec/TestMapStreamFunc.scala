package org.swasth.spec

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.OutputTag
import org.swasth.dp.core.job.{BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.Constants


class TestMapStreamFunc(config: BaseProcessTestConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseProcessFunction[java.util.Map[String, AnyRef], java.util.Map[String, AnyRef]](config) {

  override def metricsList(): List[String] = {
    List(config.mapEventCount)
  }

  override def processElement(event: java.util.Map[String, AnyRef], context: ProcessFunction[java.util.Map[String, AnyRef], java.util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val configMapOutputTag: OutputTag[Object] = config.mapOutputTag
    metrics.get(config.mapEventCount)
    metrics.reset(config.mapEventCount)
    metrics.incCounter(config.mapEventCount)
    context.output(configMapOutputTag,  Map(Constants.TOPIC -> config.kafkaMapOutputTopic, Constants.MESSAGE -> event))
  }
}
