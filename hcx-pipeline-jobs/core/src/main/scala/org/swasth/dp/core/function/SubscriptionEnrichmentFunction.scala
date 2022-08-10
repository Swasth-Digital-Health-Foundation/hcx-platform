package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.Constants

import java.util


class SubscriptionEnrichmentFunction(config: BaseJobConfig) (implicit val stringTypeInfo: TypeInformation[String])
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val senderCode: String = event.get(Constants.HCX_SENDER_CODE).asInstanceOf[String]
    val recipientCode: String = event.get(Constants.HCX_RECIPIENT_CODE).asInstanceOf[String]
    val action: String =  event.get(Constants.ACTION).asInstanceOf[String]
    Console.println(s"Sender: $senderCode : Recipient: $recipientCode : Action: $action")

    val result: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]()

    // Fetch the sender and receiver details from registry or cache
    val sender = fetchDetails(senderCode)
    if (!sender.isEmpty) {
      val enrichedSender = createSenderContext(sender, action)
      if (MapUtils.isNotEmpty(enrichedSender))
        result.put(Constants.SENDER, enrichedSender)
    }

    val recipient = fetchDetails(recipientCode)
    if (!recipient.isEmpty) {
      val enrichedRecipient = createRecipientContext(recipient, action)
      if (MapUtils.isNotEmpty(enrichedRecipient))
        result.put(Constants.RECIPIENT, enrichedRecipient)
    }

    if (MapUtils.isNotEmpty(result)) event.put(Constants.CDATA, result)
    context.output(config.enrichedSubscriptionsOutputTag, event)
  }

  override def metricsList(): List[String] = {
    List()
  }

}

