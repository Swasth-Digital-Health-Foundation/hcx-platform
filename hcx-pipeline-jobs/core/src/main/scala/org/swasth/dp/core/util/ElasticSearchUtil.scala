package org.swasth.dp.core.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.elasticsearch.action.admin.indices.alias.Alias
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.indices.{CreateIndexRequest, GetIndexRequest}
import org.elasticsearch.client.{RequestOptions, RestClient, RestClientBuilder, RestHighLevelClient}
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util

class ElasticSearchUtil(connectionInfo: String, indexName: String, indexType: String, batchSize: Int = 1000) extends Serializable {

  private val resultLimit = 100
  private val esClient: RestHighLevelClient = createClient(connectionInfo)
  private val mapper = new ObjectMapper

  private[this] val logger = LoggerFactory.getLogger(classOf[ElasticSearchUtil])

  System.setProperty("es.set.netty.runtime.available.processors", "false")

  private def createClient(connectionInfo: String): RestHighLevelClient = {
    val httpHosts: List[HttpHost] = connectionInfo.split(",").map(info => {
      val host = info.split(":")(0)
      val port = info.split(":")(1).toInt
      new HttpHost(host, port)
    }).toList

    val builder: RestClientBuilder = RestClient.builder(httpHosts: _*).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
      override def customizeRequestConfig(requestConfigBuilder: RequestConfig.Builder): RequestConfig.Builder = {
        requestConfigBuilder.setConnectionRequestTimeout(-1)
      }
    })
    new RestHighLevelClient(builder)
  }

  @throws[IOException]
  def addDocumentWithIndex(document: String, indexName: String, identifier: String): Unit = {
    try {
      val doc = mapper.readValue(document, new TypeReference[util.Map[String, AnyRef]]() {})
      println("executed started")
      val indexRequest = new IndexRequest(indexName)
      indexRequest.id(identifier)
      val response = esClient.index(indexRequest.source(doc), RequestOptions.DEFAULT)
      println("pushed data")
      logger.info(s"Added ${response.getId} to index ${response.getIndex}")
    } catch {
      case e: IOException =>
        logger.error(s"ElasticSearchUtil:: Error while adding document to index : $indexName : " + e.getMessage)
        e.printStackTrace()
        throw e
    }
  }

  def addIndex(settings: String, mappings: String, indexName: String, alias: String): Unit = {
    val client = esClient
    try {
        if(!isIndexExists(indexName)) {
          val createRequest = new CreateIndexRequest(indexName)
          if (StringUtils.isNotBlank(alias)) createRequest.alias(new Alias(alias))
          if (StringUtils.isNotBlank(settings)) createRequest.settings(Settings.builder.loadFromSource(settings, XContentType.JSON))
          if (StringUtils.isNotBlank(indexType) && StringUtils.isNotBlank(mappings)) createRequest.mapping(mappings, XContentType.JSON)
          client.indices().create(createRequest, RequestOptions.DEFAULT)
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
      esClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)
    } catch {
      case e: IOException => {
        logger.error("ElasticSearchUtil:: Failed to check Index if Present or not. Exception : ", e)
        false
      }
    }
  }

  def close(): Unit = {
    if (null != esClient) try esClient.close()
    catch {
      case e: IOException => e.printStackTrace()
    }
  }

}

