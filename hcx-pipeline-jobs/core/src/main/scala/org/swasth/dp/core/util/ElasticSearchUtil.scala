package org.swasth.dp.core.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.indices.{CreateIndexRequest, ExistsRequest}
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.endpoints.BooleanResponse
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.elasticsearch.client.{RestClient, RestClientBuilder}

import java.io.{ByteArrayInputStream, IOException}
import java.util

class ElasticSearchUtil(connectionInfo: String, indexName: String, batchSize: Int = 1000) extends Serializable {
  private val resultLimit = 100
  private val esClient: ElasticsearchClient = createClient(connectionInfo)
  private val mapper = new ObjectMapper
  private[this] val logger = LoggerFactory.getLogger(classOf[ElasticSearchUtil])

  System.setProperty("es.set.netty.runtime.available.processors", "false")

  private def createClient(connectionInfo: String): ElasticsearchClient = {
    val httpHosts: List[HttpHost] = connectionInfo.split(",").map(info => {
      val host = info.split(":")(0)
      val port = info.split(":")(1).toInt
      new HttpHost(host, port)
    }).toList

    val builder: RestClientBuilder = RestClient.builder(httpHosts: _*).setRequestConfigCallback(
      new RestClientBuilder.RequestConfigCallback() {
        override def customizeRequestConfig(requestConfigBuilder: RequestConfig.Builder): RequestConfig.Builder = {
          requestConfigBuilder.setConnectionRequestTimeout(-1)
        }
      }
    )

    val restClient = builder.build()
    val transport = new RestClientTransport(restClient, new JacksonJsonpMapper())
    new ElasticsearchClient(transport)
  }

  @throws[IOException]
  def addDocumentWithIndex(document: String, indexName: String, identifier: String): Unit = {
    try {
      val doc = mapper.readValue(document, new TypeReference[util.Map[String, AnyRef]]() {})
      
      val request = IndexRequest.of[util.Map[String, AnyRef]](
        builder => builder
          .index(indexName)
          .id(identifier)
          .document(doc)
      )

      val response = esClient.index(request)
      logger.info(s"Added ${response.id()} to index ${response.index()}")
    } catch {
      case e: IOException =>
        logger.error(s"ElasticSearchUtil:: Error while adding document to index : $indexName : " + e.getMessage)
        e.printStackTrace()
        throw e
    }
  }

  def addIndex(settings: String, mappings: String, indexName: String, alias: String): Unit = {
    try {
      if(!isIndexExists(indexName)) {
        var createRequestBuilder = new CreateIndexRequest.Builder()
          .index(indexName)
        
        if (StringUtils.isNotBlank(settings)) {
          createRequestBuilder = createRequestBuilder.settings(
            s => s.withJson(new ByteArrayInputStream(settings.getBytes()))
          )
        }
        
        if (StringUtils.isNotBlank(mappings)) {
          createRequestBuilder = createRequestBuilder.mappings(
            m => m.withJson(new ByteArrayInputStream(mappings.getBytes()))
          )
        }
        
        if (StringUtils.isNotBlank(alias)) {
          createRequestBuilder = createRequestBuilder.aliases(alias, a => a.isWriteIndex(true))
        }
        
        val createRequest = createRequestBuilder.build()
        esClient.indices().create(createRequest)
        logger.info(s"Created index: $indexName with alias: $alias")
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
      val request = new ExistsRequest.Builder().index(indexName).build()
      val response: BooleanResponse = esClient.indices().exists(request)
      response.value()
    } catch {
      case e: IOException => {
        logger.error("ElasticSearchUtil:: Failed to check Index if Present or not. Exception: ", e)
        false
      }
    }
  }

  def close(): Unit = {
    if (esClient != null) {
      try {
        esClient._transport().close()
      } catch {
        case e: IOException => 
          logger.error("Error closing ElasticSearch client", e)
          e.printStackTrace()
      }
    }
  }
}