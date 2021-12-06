package org.swasth.dp.core.function

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}

import java.util

class ContextEnrichmentFunction(config: BaseJobConfig) (implicit val stringTypeInfo: TypeInformation[String])
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {
  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    // TODO: Implement the enrichment function
    val enrichedEvent = event
    context.output(config.enrichedOutputTag, enrichedEvent)
  }


  override def metricsList(): List[String] = {
    List()
  }
}
