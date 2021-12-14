package org.swasth.dp.core.function

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.cache.{DataCache, RedisConnect}
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.JSONUtil
import scala.collection.JavaConverters._

import java.util

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
    replacedAction = actionStr.replace(lastVal,"on_"+lastVal)
    replacedAction
  }

  def createSenderReceiverMap(senderMap: util.Map[String, AnyRef], receiverMap: util.Map[String, AnyRef], actionStr: String): util.Map[String, AnyRef] = {
    val resultMap: util.Map[String, AnyRef] = new util.HashMap[String,AnyRef]()
    //Receiver Details
    var receiverEndPointUrl = receiverMap.get("endpoint_url").asInstanceOf[String]
    //If endPointUrl comes with /, remove it as action starts with /
    if(receiverEndPointUrl.endsWith("/"))
      receiverEndPointUrl = receiverEndPointUrl.substring(0,receiverEndPointUrl.length-1)
    val appendedReceiverUrl = receiverEndPointUrl.concat(actionStr)
    receiverMap.put("endpoint_url", appendedReceiverUrl)

    //Sender Details
    var senderEndPointUrl = senderMap.get("endpoint_url").asInstanceOf[String]
    //If endPointUrl comes with /, remove it as action starts with /
    if(senderEndPointUrl.endsWith("/"))
      senderEndPointUrl = senderEndPointUrl.substring(0,senderEndPointUrl.length-1)

    // write method to determine the on_ for the sender action
    val replacedAction: String = getReplacedAction(actionStr)
    val appendedSenderUrl = senderEndPointUrl.concat(replacedAction)
    senderMap.put("endpoint_url", appendedSenderUrl)

    resultMap.put("recipient",receiverMap)
    resultMap.put("sender",senderMap)

   resultMap
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    // TODO: Implement the enrichment function
    val enrichedEvent = event
    val senderCode:String = getCode(event,"x-hcx-sender_code")
    val recipientCode:String = getCode(event,"x-hcx-recipient_code")
    val actionStr: String = event.get("action").asInstanceOf[String]
    Console.println(s"Sender: $senderCode : Recipient: $recipientCode : Action: $actionStr")

    // Fetch the sender and receiver details from registry or cache
    val receiverMap = fetchReceiverDetails(recipientCode)
    val senderMap = fetchSenderDetails(senderCode)

    val resultMap: util.Map[String, AnyRef] = createSenderReceiverMap(senderMap,receiverMap,actionStr)
    //Add the cdata to the incoming data with the sender and receiver details after appending endPointUrl and action
    enrichedEvent.put("cdata",resultMap)
    context.output(config.enrichedOutputTag, enrichedEvent)
  }

  override def metricsList(): List[String] = {
    List()
  }

  def fetchSenderDetails(code: String): util.Map[String, AnyRef] = {
    if (registryDataCache.isExists(code)) {
      logger.info("Getting details from cache for :" + code)
      Console.println("Getting details from cache for :" + code)
      val mutableMap = registryDataCache.getWithRetry(code)
      mutableMap.asJava
    } else {
      //TODO Need to fetch the details from API call
      Console.println("Could not find the sender/receiver details in cache for code:" + code)
      logger.info("Could not find the sender/receiver details in cache for code:" + code)
      val collectionMap = createSenderMap()
      registryDataCache.hmSet(code, JSONUtil.serialize(collectionMap))
      collectionMap
    }
  }
    def fetchReceiverDetails(code: String): util.Map[String, AnyRef] = {
      if (registryDataCache.isExists(code)) {
        logger.info("Getting details from cache for :"+ code)
        Console.println("Getting details from cache for :"+ code)
        val mutableMap = registryDataCache.getWithRetry(code)
        mutableMap.asJava
      } else {
        //TODO Need to fetch the details from API call
        Console.println("Could not find the sender/receiver details in cache for code:"+ code)
        logger.info("Could not find the sender/receiver details in cache for code:"+ code)
        val collectionMap = createReceiverMap()
        registryDataCache.hmSet(code,JSONUtil.serialize(collectionMap))
        collectionMap
      }
    }

  def createSenderMap(): util.Map[String, AnyRef] = {
    val collectionMap = new util.HashMap[String, AnyRef]()
    collectionMap.put("participant_code", "12345")
    collectionMap.put("signing_cert_path", "")
    collectionMap.put("roles", "admin")
    collectionMap.put("encryption_cert", "")
    collectionMap.put("endpoint_url", "http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080")
    collectionMap.put("participant_name", "Test Provider")
    collectionMap.put("hfr_code", "0001")
    collectionMap.put("status", "Created")
    collectionMap
  }

  def createReceiverMap(): util.Map[String,AnyRef] = {
    val collectionMap = new util.HashMap[String, AnyRef]()
    collectionMap.put("participant_code", "67890")
    collectionMap.put("signing_cert_path", "urn:isbn:0-476-27557-4")
    collectionMap.put("roles", "admin")
    collectionMap.put("encryption_cert", "urn:isbn:0-4234")
    collectionMap.put("endpoint_url", "http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080")
    collectionMap.put("participant_name", "Test Provider")
    collectionMap.put("hfr_code", "0001")
    collectionMap.put("status", "Created")
    collectionMap
  }
}
