package org.swasth.dp.core.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpHost
import org.elasticsearch.client.{Request, Response, RestClient, RestClientBuilder}
import org.elasticsearch.xcontent.XContentFactory
import org.slf4j.LoggerFactory

import java.io.IOException
import scala.collection.JavaConverters.asJavaIterableConverter

class ElasticSearchUtil(connectionInfo: String, indexName: String, batchSize: Int = 1000) extends Serializable {

  private val resultLimit = 100
  private val esClient: RestClient = createClient(connectionInfo)
  private val mapper = new ObjectMapper

  private[this] val logger = LoggerFactory.getLogger(classOf[ElasticSearchUtil])

  System.setProperty("es.set.netty.runtime.available.processors", "false")

  private def createClient(connectionInfo: String): RestClient = {
    val httpHosts: List[HttpHost] = connectionInfo.split(",").map(info => {
      val host = info.split(":")(0)
      val port = info.split(":")(1).toInt
      new HttpHost(host, port)
    }).toList

    val builder: RestClientBuilder = RestClient.builder(httpHosts: _*)
      .setRequestConfigCallback { requestConfigBuilder =>
        requestConfigBuilder.setConnectionRequestTimeout(-1)
      }
    builder.build()
  }

  @throws[IOException]
  def addDocumentWithIndex(document: String, indexName: String, identifier: String): Unit = {
    try {
      val doc: Map[String, AnyRef] = mapper.readValue(document, new TypeReference[Map[String, AnyRef]]() {})
      val endpoint = s"/$indexName/_doc/$identifier"
      val jsonDoc: String = mapper.writeValueAsString(doc.asJava)
      val request = new Request("PUT", endpoint)
      request.setJsonEntity(jsonDoc)
      val response: Response = esClient.performRequest(request)
      println(response.getHost)
      println(s"Added $identifier to index $indexName")
    } catch {
      case e: IOException =>
        println(s"ElasticSearchUtil:: Error while adding document to index : $indexName : " + e.getMessage)
        throw e
    }
  }

//  def addIndex(settings: String, mappings: String, indexName: String, alias: String): Unit = {
//    val client = esClient
//    try {
//        if(!isIndexExists(indexName)) {
//          val createRequest = new CreateIndexRequest(indexName)
//          if (StringUtils.isNotBlank(alias)) createRequest.alias(new Alias(alias))
//          if (StringUtils.isNotBlank(settings)) createRequest.settings(Settings.builder.loadFromSource(settings, XContentType.JSON))
//          if (StringUtils.isNotBlank(mappings)) createRequest.mapping(mappings, XContentType.JSON)
//          client.indices().create(createRequest, RequestOptions.DEFAULT)
//        }
//    } catch {
//      case e: Exception =>
//        logger.error(s"ElasticSearchUtil:: Error while creating index : $indexName : " + e.getMessage)
//        e.printStackTrace()
//        throw e
//    }
//  }

  def addIndex(settings: String, mappings: String, indexName: String, alias: String): Unit = {
    try {
      if (!isIndexExists(indexName)) {
        val createRequest = new Request("PUT", "/" + indexName)
        val jsonBuilder = XContentFactory.jsonBuilder()
        if (StringUtils.isNotBlank(alias)) jsonBuilder.startObject("aliases").startObject(alias).endObject()
        if (StringUtils.isNotBlank(settings)) jsonBuilder.startObject("settings").startObject().field("index", settings).endObject()
        if (StringUtils.isNotBlank(mappings)) jsonBuilder.startObject("mappings").field("properties", mappings).endObject()
//        createRequest.setJsonEntity(jsonBuilde)
        val response: Response = esClient.performRequest(createRequest)
        if (response.getStatusLine.getStatusCode != 200) {
          throw new IOException("Failed to create index: " + indexName)
        }
      }
    } catch {
      case e: Exception =>
        logger.error(s"ElasticsearchUtil:: Error while creating index : $indexName : " + e.getMessage)
        e.printStackTrace()
        throw e
    }
  }

//  def isIndexExists(indexName: String): Boolean = {
//    try {
//      esClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)
//    } catch {
//      case e: IOException => {
//        logger.error("ElasticSearchUtil:: Failed to check Index if Present or not. Exception : ", e)
//        false
//      }
//    }
//  }
  def isIndexExists(indexName: String): Boolean = {
    val request = new Request("HEAD", "/" + indexName)
    try {
      val response = esClient.performRequest(request)
      val statusCode = response.getStatusLine.getStatusCode
      statusCode == 200
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

