package org.swasth.dp.core.cache

import java.util
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.Constants
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.{JedisConnectionException, JedisException}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.Map

class DataCache(val config: BaseJobConfig, val redisConnect: RedisConnect, val dbIndex: Int, val fields: List[String]) {

  private[this] val logger = LoggerFactory.getLogger(classOf[DataCache])
  private var redisConnection: Jedis = _
  val gson = new Gson()

  def init() {
    this.redisConnection = redisConnect.getConnection(dbIndex)
  }

  def close() {
    this.redisConnection.close()
  }

  def hgetAllWithRetry(key: String): mutable.Map[String, AnyRef] = {
    try {
      convertToComplexDataTypes(hgetAll(key))
    } catch {
      case ex: JedisException =>
        logger.error("Exception when retrieving data from redis cache", ex)
        this.redisConnection.close()
        this.redisConnection = redisConnect.getConnection(dbIndex)
        convertToComplexDataTypes(hgetAll(key))
    }
  }

  def isArray(value: String): Boolean = {
    val redisValue = value.trim
    redisValue.length > 0 && redisValue.startsWith("[")
  }

   def isObject(value: String) = {
     val redisValue = value.trim
     redisValue.length > 0 && redisValue.startsWith("{")
   }

  def convertToComplexDataTypes(data: mutable.Map[String, String]): mutable.Map[String, AnyRef] = {
    val result = mutable.Map[String, AnyRef]()
    data.keys.map {
      redisKey =>
        val redisValue = data(redisKey)
        if(isArray(redisValue)) {
          result += redisKey -> gson.fromJson(redisValue, new util.ArrayList[AnyRef]().getClass)
        } else if (isObject(redisValue)) {
          result += redisKey -> gson.fromJson(redisValue, new util.HashMap[String, AnyRef]().getClass)
        } else {
          result += redisKey -> redisValue
        }
    }
    result
  }

  private def hgetAll(key: String): mutable.Map[String, String] = {
    val dataMap = redisConnection.hgetAll(key)
    if (dataMap.size() > 0) {
      if(fields.nonEmpty) dataMap.keySet().retainAll(fields.asJava)
      dataMap.values().removeAll(util.Collections.singleton(""))
      dataMap.asScala
    } else {
      mutable.Map[String, String]()
    }
  }

  def getWithRetry(key: String): mutable.Map[String, AnyRef] = {
    try {
      get(key)
    } catch {
      case ex: JedisException =>
        logger.error("Exception when retrieving data from redis cache", ex)
        this.redisConnection.close()
        this.redisConnection = redisConnect.getConnection(dbIndex)
        get(key)
    }

  }

  private def get(key: String): mutable.Map[String, AnyRef] = {
    val data = redisConnection.get(key)
    if (data != null && !data.isEmpty) {
      val dataMap = gson.fromJson(data, new util.HashMap[String, AnyRef]().getClass)
      if(fields.nonEmpty) dataMap.keySet().retainAll(fields.asJava)
      dataMap.values().removeAll(util.Collections.singleton(""))
      dataMap.asScala
    } else {
      mutable.Map[String, AnyRef]()
    }
  }

  def getMultipleWithRetry(keys: List[String]): List[Map[String, AnyRef]] = {
    for (key <- keys) yield {
      getWithRetry(key)
    }
  }

  def isExists(key: String): Boolean = {
    redisConnection.exists(key)
  }

  def hmSet(key: String, value: util.Map[String, String]): Unit = {
    try {
      redisConnection.hmset(key, value)
    } catch {
      // Write testcase for catch block
      // $COVERAGE-OFF$ Disabling scoverage
      case ex: JedisException => {
        println("dataCache")
        logger.error("Exception when inserting data to redis cache", ex)
        this.redisConnection.close()
        this.redisConnection = redisConnect.getConnection(dbIndex)
        this.redisConnection.hmset(key, value)
      }
      // $COVERAGE-ON$
    }
  }

  def hmSet(key: String, value: String, ttl: Int): Unit = {
    try {
      redisConnection.setex(key, ttl, value)
    } catch {
      case ex: JedisException => {
        logger.error("Exception when inserting data to redis cache", ex)
        this.redisConnection.close()
        this.redisConnection = redisConnect.getConnection(dbIndex)
        this.redisConnection.setnx(key, value)
      }
    }
  }

  def setWithRetry(key: String, value: String): Unit = {
    try {
      set(key, value);
    } catch {
      // Write testcase for catch block
      // $COVERAGE-OFF$ Disabling scoverage
      case ex@(_: JedisConnectionException | _: JedisException) =>
        logger.error("Exception when update data to redis cache", ex)
        this.redisConnection.close()
        this.redisConnection = redisConnect.getConnection(dbIndex);
        set(key, value)
      // $COVERAGE-ON$
    }
  }

  def set(key: String, value: String): Unit = {
    redisConnection.setex(key, config.redisExpires, value)
  }

  def sMembers(key: String): util.Set[String] = {
    redisConnection.smembers(key)
  }
  def getKeyMembers(key: String): util.Set[String] = {
    try {
      sMembers(key)
    } catch {
      // $COVERAGE-OFF$ Disabling scoverage
      case ex: JedisException =>
        logger.error("Exception when retrieving data from redis cache", ex)
        this.redisConnection.close()
        this.redisConnection = redisConnect.getConnection(dbIndex)
        sMembers(key)
      // $COVERAGE-ON$
    }
  }

}