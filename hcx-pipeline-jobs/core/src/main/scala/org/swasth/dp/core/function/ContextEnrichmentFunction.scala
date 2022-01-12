package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}

import java.util

class ContextEnrichmentFunction(config: BaseJobConfig) (implicit val stringTypeInfo: TypeInformation[String])
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ContextEnrichmentFunction])


  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
  }


  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val senderCode: String = getCode(event, "x-hcx-sender_code")
    val recipientCode: String = getCode(event, "x-hcx-recipient_code")
    val action: String = event.get("action").asInstanceOf[String]
    Console.println(s"Sender: $senderCode : Recipient: $recipientCode : Action: $action")

    val result: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]()

    // Fetch the sender and receiver details from registry or cache
    val sender = fetchDetails(senderCode)
    if (!sender.isEmpty) {
      val enrichedSender = createSenderContext(sender, action)
      if (MapUtils.isNotEmpty(enrichedSender))
        result.put("sender", enrichedSender)
    }

    val recipient = fetchDetails(recipientCode)
    if (!recipient.isEmpty) {
      val enrichedRecipient = createRecipientContext(recipient, action)
      if (MapUtils.isNotEmpty(enrichedRecipient))
        result.put("recipient", enrichedRecipient)
    }

    if (MapUtils.isNotEmpty(result)) event.put("cdata", result)
    context.output(config.enrichedOutputTag, event)
  }

  override def metricsList(): List[String] = {
    List()
  }
}
