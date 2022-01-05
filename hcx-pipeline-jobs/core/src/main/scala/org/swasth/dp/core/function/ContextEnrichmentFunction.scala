package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.cache.{DataCache, RedisConnect}
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.{DispatcherUtil, JSONUtil}

import java.util
import scala.collection.JavaConverters._

class ContextEnrichmentFunction(config: BaseJobConfig) (implicit val stringTypeInfo: TypeInformation[String])
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[ContextEnrichmentFunction])
  private var registryDataCache: DataCache = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    registryDataCache =
      new DataCache(config, new RedisConnect(config.redisHost, config.redisPort, config),
        config.redisAssetStore, config.senderReceiverFields)
    registryDataCache.init()
  }



  def getCode(event: util.Map[String, AnyRef], key: String): String = {
    val protocolMap = event.get("headers").asInstanceOf[util.Map[String, AnyRef]].get("protocol").asInstanceOf[util.Map[String, AnyRef]]
    protocolMap.get(key).asInstanceOf[String]
  }

  def getReplacedAction(actionStr: String): String = {
    var replacedAction = actionStr
    val lastVal = actionStr.split("/").last
    if (!lastVal.startsWith("on_"))
      replacedAction = actionStr.replace(lastVal,"on_"+lastVal)
    replacedAction
  }

  def createSenderContext(sender: util.Map[String, AnyRef],actionStr: String): util.Map[String, AnyRef] = {
    //Sender Details
    var endpointUrl = sender.getOrDefault("endpoint_url", "").asInstanceOf[String]
    if (!StringUtils.isEmpty(endpointUrl)) {
      //If endPointUrl comes with /, remove it as action starts with /
      if (endpointUrl.endsWith("/"))
        endpointUrl = endpointUrl.substring(0, endpointUrl.length - 1)

      // fetch on_ action for the sender
      val replacedAction: String = getReplacedAction(actionStr)
      val appendedSenderUrl = endpointUrl.concat(replacedAction)
      sender.put("endpoint_url", appendedSenderUrl)
      sender
    } else new util.HashMap[String, AnyRef]()

  }

  def createRecipientContext(receiver: util.Map[String, AnyRef],actionStr: String): util.Map[String, AnyRef] = {
    //Receiver Details
    var endpointUrl = receiver.get("endpoint_url").asInstanceOf[String]
    if (!StringUtils.isEmpty(endpointUrl)) {
      //If endPointUrl comes with /, remove it as action starts with /
      if (endpointUrl.endsWith("/"))
        endpointUrl = endpointUrl.substring(0, endpointUrl.length - 1)
      val appendedReceiverUrl = endpointUrl.concat(actionStr)
      receiver.put("endpoint_url", appendedReceiverUrl)
      receiver
    } else new util.HashMap[String, AnyRef]()

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

  def fetchDetails(code: String): util.Map[String, AnyRef] = {
    try {
      if (registryDataCache.isExists(code)) {
        logger.info("Getting details from cache for :" + code)
        Console.println("Getting details from cache for :" + code)
        val mutableMap = registryDataCache.getWithRetry(code)
        mutableMap.asJava
      } else {
        //Fetch the details from API call
        Console.println("Could not find the details in cache for code:" + code)
        logger.info("Could not find the details in cache for code:" + code)
        val collectionMap = getDetails(code)
        if (!collectionMap.isEmpty) // Add the registry data into cache if it is not empty
          registryDataCache.hmSet(code, JSONUtil.serialize(collectionMap))
        collectionMap
      }
    } catch {
      case ex: Exception =>
        //In case of issues with redis cache, fetch the details from the registry
        getDetails(code)
    }
  }

  def getDetails(code: String): util.Map[String, AnyRef] = {
    val responseBody: String = DispatcherUtil.post(config.registryUrl, code)
    Console.println("registryResponse", responseBody)
    logger.info("Response from registry:", code)
    val responseArr = JSONUtil.deserialize[util.ArrayList[util.HashMap[String, AnyRef]]](responseBody)
    if (!responseArr.isEmpty) {
      val collectionMap = responseArr.get(0)
      collectionMap
    } else {
      new util.HashMap[String, AnyRef]
    }
  }

}
