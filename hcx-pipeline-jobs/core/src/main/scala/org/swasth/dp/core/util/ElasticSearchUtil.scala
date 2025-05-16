package org.swasth.dp.core.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpEntity
import org.apache.http.client.methods.{HttpGet, HttpHead, HttpPut}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util

class ElasticSearchUtil(connectionInfo: String, indexName: String, batchSize: Int = 1000) extends Serializable {

  private val resultLimit = 100
  private val httpClient: CloseableHttpClient = HttpClients.createDefault()
  private val mapper = new ObjectMapper

  private[this] val logger = LoggerFactory.getLogger(classOf[ElasticSearchUtil])
  
  // Parse connectionInfo to extract base URLs
  private val esBaseUrls: Array[String] = connectionInfo.split(",").map(info => {
    val host = info.split(":")(0)
    val port = info.split(":")(1)
    s"http://$host:$port"
  })
  
  // For simple load balancing/failover, we'll rotate through available nodes
  private var currentUrlIndex = 0
  
  // Version check flag to handle API differences between ES versions
  private var checkedEsVersion = false
  private var isVersion8orHigher = false
  
  private def getNextBaseUrl(): String = {
    val url = esBaseUrls(currentUrlIndex)
    currentUrlIndex = (currentUrlIndex + 1) % esBaseUrls.length
    
    // Check ES version if not done already
    if (!checkedEsVersion) {
      checkElasticsearchVersion(url)
    }
    
    url
  }
  
  private def checkElasticsearchVersion(baseUrl: String): Unit = {
    try {
      val httpGet = new HttpGet(s"$baseUrl")
      val response = httpClient.execute(httpGet)
      try {
        if (response.getStatusLine.getStatusCode == 200) {
          val entity = response.getEntity
          if (entity != null) {
            val responseBody = EntityUtils.toString(entity)
            val responseMap = mapper.readValue(responseBody, new TypeReference[util.Map[String, AnyRef]]() {})
            
            if (responseMap.containsKey("version")) {
              val versionMap = responseMap.get("version").asInstanceOf[util.Map[String, AnyRef]]
              val versionNumber = versionMap.get("number").toString
              isVersion8orHigher = versionNumber.startsWith("8.") || versionNumber.startsWith("9.")
              logger.info(s"Connected to Elasticsearch version: $versionNumber")
            }
          }
        }
      } finally {
        response.close()
      }
    } catch {
      case e: Exception =>
        logger.warn(s"Unable to determine Elasticsearch version: ${e.getMessage}")
    } finally {
      checkedEsVersion = true
    }
  }

  @throws[IOException]
  def addDocumentWithIndex(document: String, indexName: String, identifier: String): Unit = {
    try {
      val baseUrl = getNextBaseUrl()
      val endpoint = s"$baseUrl/$indexName/_doc/$identifier"
      
      val httpPut = new HttpPut(endpoint)
      httpPut.setEntity(new StringEntity(document, ContentType.APPLICATION_JSON))
      
      // For ES 8.x, we need to include "Content-Type: application/json" header
      httpPut.setHeader("Content-Type", "application/json")
      
      val response = httpClient.execute(httpPut)
      try {
        val statusCode = response.getStatusLine.getStatusCode
        
        if (statusCode >= 200 && statusCode < 300) {
          val responseEntity = response.getEntity
          if (responseEntity != null) {
            val responseBody = EntityUtils.toString(responseEntity)
            val responseMap = mapper.readValue(responseBody, new TypeReference[util.Map[String, AnyRef]]() {})
            logger.info(s"Added ${responseMap.get("_id")} to index ${responseMap.get("_index")}")
          }
        } else {
          val errorMsg = s"Failed to add document, status code: $statusCode"
          if (response.getEntity != null) {
            val responseBody = EntityUtils.toString(response.getEntity)
            logger.error(s"$errorMsg, response: $responseBody")
          } else {
            logger.error(errorMsg)
          }
          throw new IOException(errorMsg)
        }
      } finally {
        response.close()
      }
    } catch {
      case e: IOException =>
        logger.error(s"ElasticSearchUtil:: Error while adding document to index : $indexName : " + e.getMessage)
        e.printStackTrace()
        throw e
    }
  }

  def addIndex(settings: String, mappings: String, indexName: String, alias: String): Unit = {
    try {
      if (!isIndexExists(indexName)) {
        val baseUrl = getNextBaseUrl()
        val endpoint = s"$baseUrl/$indexName"
        
        val httpPut = new HttpPut(endpoint)
        httpPut.setHeader("Content-Type", "application/json")
        
        // Create index request body
        val requestBodyMap = new util.HashMap[String, AnyRef]()
        
        // Add settings if provided
        if (StringUtils.isNotBlank(settings)) {
          val settingsMap = mapper.readValue(settings, new TypeReference[util.Map[String, AnyRef]]() {})
          requestBodyMap.put("settings", settingsMap)
        }
        
        // Add mappings if provided
        if (StringUtils.isNotBlank(mappings)) {
          // For ES 8.x, mappings structure changed slightly
          if (isVersion8orHigher) {
            val mappingsMap = mapper.readValue(mappings, new TypeReference[util.Map[String, AnyRef]]() {})
            requestBodyMap.put("mappings", mappingsMap)
          } else {
            val mappingsMap = mapper.readValue(mappings, new TypeReference[util.Map[String, AnyRef]]() {})
            requestBodyMap.put("mappings", mappingsMap)
          }
        }
        
        // Add alias if provided
        if (StringUtils.isNotBlank(alias)) {
          val aliasMap = new util.HashMap[String, util.Map[String, AnyRef]]()
          aliasMap.put(alias, new util.HashMap[String, AnyRef]())
          requestBodyMap.put("aliases", aliasMap)
        }
        
        val requestBody = mapper.writeValueAsString(requestBodyMap)
        httpPut.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON))
        
        val response = httpClient.execute(httpPut)
        try {
          val statusCode = response.getStatusLine.getStatusCode
          
          if (!(statusCode >= 200 && statusCode < 300)) {
            val errorMsg = s"Failed to create index $indexName, status code: $statusCode"
            if (response.getEntity != null) {
              val responseBody = EntityUtils.toString(response.getEntity)
              logger.error(s"$errorMsg, response: $responseBody")
            } else {
              logger.error(errorMsg)
            }
            throw new IOException(errorMsg)
          } else {
            logger.info(s"Successfully created index: $indexName with alias: $alias")
          }
        } finally {
          response.close()
        }
      } else {
        logger.info(s"Index $indexName already exists, skipping creation")
      }
    } catch {
      case e: Exception =>
        logger.error(s"ElasticSearchUtil:: Error while creating index : $indexName : " + e.getMessage)
        e.printStackTrace()
        throw e
    }
  }

  def isIndexExists(indexName: String): Boolean = {
    try {
      val baseUrl = getNextBaseUrl()
      val endpoint = s"$baseUrl/$indexName"
      
      val httpHead = new HttpHead(endpoint)
      val response = httpClient.execute(httpHead)
      try {
        val statusCode = response.getStatusLine.getStatusCode
        statusCode == 200
      } finally {
        response.close()
      }
    } catch {
      case e: IOException => {
        logger.error("ElasticSearchUtil:: Failed to check if Index is Present or not. Exception : ", e)
        false
      }
    }
  }

  def close(): Unit = {
    if (null != httpClient) {
      try {
        httpClient.close()
      } catch {
        case e: IOException => 
          logger.error("Error closing HTTP client", e)
          e.printStackTrace()
      }
    }
  }
}