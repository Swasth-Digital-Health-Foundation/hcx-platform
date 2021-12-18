package org.swasth.dp.core.function

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

  def createSenderReceiverMap(senderMap: util.Map[String, AnyRef], receiverMap: util.Map[String, AnyRef], actionStr: String): util.Map[String, AnyRef] = {
    val resultMap: util.Map[String, AnyRef] = new util.HashMap[String,AnyRef]()
    //Receiver Details
    var receiverEndPointUrl = receiverMap.get("endpointUrl").asInstanceOf[String]
    //If endPointUrl comes with /, remove it as action starts with /
    if(receiverEndPointUrl.endsWith("/"))
      receiverEndPointUrl = receiverEndPointUrl.substring(0,receiverEndPointUrl.length-1)
    val appendedReceiverUrl = receiverEndPointUrl.concat(actionStr)
    receiverMap.put("endpoint_url", appendedReceiverUrl)

    //Sender Details
    var senderEndPointUrl = senderMap.get("endpointUrl").asInstanceOf[String]
    //If endPointUrl comes with /, remove it as action starts with /
    if(senderEndPointUrl.endsWith("/"))
      senderEndPointUrl = senderEndPointUrl.substring(0,senderEndPointUrl.length-1)

    // fetch on_ action for the sender
    val replacedAction: String = getReplacedAction(actionStr)
    val appendedSenderUrl = senderEndPointUrl.concat(replacedAction)
    senderMap.put("endpoint_url", appendedSenderUrl)

    resultMap.put("recipient",receiverMap)
    resultMap.put("sender",senderMap)

   resultMap
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val enrichedEvent = event
    val senderCode:String = getCode(event,"x-hcx-sender_code")
    val recipientCode:String = getCode(event,"x-hcx-recipient_code")
    val actionStr: String = event.get("action").asInstanceOf[String]
    Console.println(s"Sender: $senderCode : Recipient: $recipientCode : Action: $actionStr")

    // Fetch the sender and receiver details from registry or cache
    val receiverMap = fetchDetails(recipientCode)
    val senderMap = fetchDetails(senderCode)

    val resultMap: util.Map[String, AnyRef] = createSenderReceiverMap(senderMap,receiverMap,actionStr)
    //Add the cdata to the incoming data with the sender and receiver details after appending endPointUrl and action
    enrichedEvent.put("cdata",resultMap)
    context.output(config.enrichedOutputTag, enrichedEvent)
  }

  override def metricsList(): List[String] = {
    List()
  }

  def fetchDetails(code: String): util.Map[String, AnyRef] = {
    if (registryDataCache.isExists(code)) {
      logger.info("Getting details from cache for :"+ code)
      Console.println("Getting details from cache for :"+ code)
      val mutableMap = registryDataCache.getWithRetry(code)
      mutableMap.asJava
    } else {
      //Fetch the details from API call
      Console.println("Could not find the details in cache for code:"+ code)
      logger.info("Could not find the details in cache for code:"+ code)
      val collectionMap = getDetails(code)
      registryDataCache.hmSet(code,JSONUtil.serialize(collectionMap))
      collectionMap
    }
  }

  def getDetails(code: String): util.Map[String, AnyRef] = {
    val responseBody: String = DispatcherUtil.post(config.registryUrl,code)
    Console.println("registryResponse", responseBody)
    logger.info("Response from registry", responseBody)
    val responseArr = JSONUtil.deserialize[util.ArrayList[util.HashMap[String, AnyRef]]](responseBody)
    val collectionMap = responseArr.get(0)
    collectionMap
  }

}
