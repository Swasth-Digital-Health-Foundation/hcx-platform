package org.swasth.dp.core.job

import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.scala.metrics.ScalaGauge
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.swasth.dp.core.cache.{DataCache, RedisConnect}
import org.swasth.dp.core.service.RegistryService
import org.swasth.dp.core.util.{Constants, DispatcherUtil, JSONUtil}

import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import scala.collection.JavaConverters._

case class Metrics(metrics: ConcurrentHashMap[String, AtomicLong]) {
  def incCounter(metric: String): Unit = {
    metrics.get(metric).getAndIncrement()
  }

  def getAndReset(metric: String): Long = metrics.get(metric).getAndSet(0L)
  def get(metric: String): Long = metrics.get(metric).get()
  def reset(metric: String): Unit = metrics.get(metric).set(0L)
}

trait JobMetrics {
  def registerMetrics(metrics: List[String]): Metrics = {
    val metricMap = new ConcurrentHashMap[String, AtomicLong]()
    metrics.map { metric => metricMap.put(metric, new AtomicLong(0L)) }
    Metrics(metricMap)
  }
}

abstract class BaseProcessFunction[T, R](config: BaseJobConfig) extends ProcessFunction[T, R] with BaseDeduplication with JobMetrics {

  private val metrics: Metrics = registerMetrics(metricsList())
  private var registryDataCache: DataCache = _
  var dispatcherUtil: DispatcherUtil = _
  var registryService: RegistryService = _


  override def open(parameters: Configuration): Unit = {
    metricsList().map { metric =>
      getRuntimeContext.getMetricGroup.addGroup(config.jobName)
        .gauge[Long, ScalaGauge[Long]](metric, ScalaGauge[Long]( () => metrics.getAndReset(metric) ))
    }
    registryDataCache =
      new DataCache(config, new RedisConnect(config.redisHost, config.redisPort, config),
        config.redisAssetStore, config.senderReceiverFields)
    registryDataCache.init()
    dispatcherUtil = new DispatcherUtil(config)
    registryService = new RegistryService(config)
  }

  def processElement(event: T, context: ProcessFunction[T, R]#Context, metrics: Metrics): Unit
  def metricsList(): List[String]

  override def processElement(event: T, context: ProcessFunction[T, R]#Context, out: Collector[R]): Unit = {
    processElement(event, context, metrics)
  }

  def getProtocolStringValue(event: util.Map[String, AnyRef], key: String): String = {
    event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(key,"").asInstanceOf[String]
  }

  def getProtocolMapValue(event: util.Map[String, AnyRef], key: String): util.Map[String, AnyRef] = {
    event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(key,new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
  }

  def getCDataListValue(event: util.Map[String, AnyRef], participant: String, key: String): util.List[String] = {
    event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(participant, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(key, new util.ArrayList[String]()).asInstanceOf[util.List[String]]  }

  def getCDataStringValue(event: util.Map[String, AnyRef], participant: String, key: String): String = {
    event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(participant, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].get(key).asInstanceOf[String]
  }

  def setStatus(event: util.Map[String, AnyRef], status: String): Unit = {
    if (Constants.ALLOWED_STATUS_UPDATE.contains(event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.HCX_STATUS, "")))
      event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].put(Constants.HCX_STATUS, status)
    else  {
      event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].put(Constants.HCX_STATUS, event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.HCX_STATUS, ""))
    }
  }

  def setErrorDetails(event: util.Map[String, AnyRef], errorDetails: util.Map[String, AnyRef]): Unit ={
    event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].put(Constants.ERROR_DETAILS, errorDetails)
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
    var endpointUrl = sender.getOrDefault(Constants.END_POINT, "").asInstanceOf[String]
    if (!StringUtils.isEmpty(endpointUrl)) {
      //If endPointUrl comes with /, remove it as action starts with /
      if (endpointUrl.endsWith("/"))
        endpointUrl = endpointUrl.substring(0, endpointUrl.length - 1)

      // fetch on_ action for the sender
      val replacedAction: String = getReplacedAction(actionStr)
      val appendedSenderUrl = endpointUrl.concat(replacedAction)
      sender.put(Constants.END_POINT, appendedSenderUrl)
      sender
    } else new util.HashMap[String, AnyRef]()

  }

  def createRecipientContext(receiver: util.Map[String, AnyRef],actionStr: String): util.Map[String, AnyRef] = {
    //Receiver Details
    var endpointUrl = receiver.get(Constants.END_POINT).asInstanceOf[String]
    if (!StringUtils.isEmpty(endpointUrl)) {
      //If endPointUrl comes with /, remove it as action starts with /
      if (endpointUrl.endsWith("/"))
        endpointUrl = endpointUrl.substring(0, endpointUrl.length - 1)
      val appendedReceiverUrl = endpointUrl.concat(actionStr)
      receiver.put(Constants.END_POINT, appendedReceiverUrl)
      receiver
    } else new util.HashMap[String, AnyRef]()

  }

  def fetchDetails(code: String): util.Map[String, AnyRef] = {
    try {
      if (registryDataCache.isExists(code)) {
        Console.println("Getting details from cache for :" + code)
        val mutableMap = registryDataCache.getWithRetry(code)
        mutableMap.asJava
      } else {
        //Fetch the details from API call
        Console.println("Could not find the details in cache for code:" + code)
        val collectionMap = getDetails(code)
        if (!collectionMap.isEmpty) // Add the registry data into cache if it is not empty
          registryDataCache.hmSet(code, JSONUtil.serialize(collectionMap), config.redisExpires)
        collectionMap
      }
    } catch {
      case ex: Exception =>
        //In case of issues with redis cache, fetch the details from the registry
        getDetails(code)
    }
  }

  def getDetails(code: String): util.Map[String, AnyRef] = {
    val key = Constants.PARTICIPANT_CODE
    val responseBody = registryService.getParticipantDetails(s"""{"$key":{"eq":"$code"}}""")
    if (!responseBody.isEmpty) {
      val collectionMap = responseBody.get(0)
      collectionMap
    } else {
      new util.HashMap[String, AnyRef]
    }
  }
}
