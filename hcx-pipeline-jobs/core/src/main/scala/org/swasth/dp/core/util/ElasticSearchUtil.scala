package org.swasth.dp.core.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.transport.rest_client.RestClientTransport
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

import java.io.IOException
import java.util
import java.util.function.Function

class ElasticSearchUtil(configPath: String, indexName: String, batchSize: Int = 1000) extends Serializable {

  private val config = ConfigFactory.load(configPath)
  private val esBasePath = config.getString("es.basePath")
  private val host = esBasePath.split(":")(0)
  private val port = esBasePath.split(":")(1).toInt
  private val esClient: ElasticsearchClient = createClient(host, port)
  private val mapper = new ObjectMapper

  private[this] val logger = LoggerFactory.getLogger(classOf[ElasticSearchUtil])

  System.setProperty("es.set.netty.runtime.available.processors", "false")

  private def createClient(host: String, port: Int): ElasticsearchClient = {
    if (host.isEmpty || host.startsWith("//")) {
      throw new IllegalArgumentException(s"Invalid host: $host. Please provide a valid hostname.")
    }

    val validPort = try {
      port
    } catch {
      case _: NumberFormatException =>
        throw new IllegalArgumentException(s"Invalid port number: $port. Please provide a valid integer.")
    }

    val httpHost = new HttpHost(host, validPort)
    val restClient = RestClient.builder(httpHost).build()
    val transport = new RestClientTransport(restClient, new JacksonJsonpMapper())
    new ElasticsearchClient(transport)
  }

  @throws[IOException]
  def addDocumentWithIndex(document: String, indexName: String, identifier: String): Unit = {
    try {
      val doc = mapper.readValue(document, new TypeReference[util.Map[String, AnyRef]]() {})
      val indexRequestFunction: java.util.function.Function[
        co.elastic.clients.elasticsearch.core.IndexRequest.Builder[java.util.Map[String, AnyRef]],
        co.elastic.clients.util.ObjectBuilder[
          co.elastic.clients.elasticsearch.core.IndexRequest[java.util.Map[String, AnyRef]]
        ]
      ] = builder => builder.index(indexName).id(identifier).document(doc)

      val indexRequest = IndexRequest.of(indexRequestFunction)
      esClient.index(indexRequest)
      logger.info(s"Added $identifier to index $indexName")
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
        val createRequest = CreateIndexRequest.of(_.index(indexName)
          .settings(s => s.withJson(new java.io.ByteArrayInputStream(settings.getBytes)))
          .mappings(m => m.withJson(new java.io.ByteArrayInputStream(mappings.getBytes))))
        esClient.indices().create(createRequest)
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
      esClient.indices().exists(_.index(indexName)).value()
    } catch {
      case e: IOException =>
        logger.error("ElasticSearchUtil:: Failed to check Index if Present or not. Exception : ", e)
        false
    }
  }

  def close(): Unit = {
    try esClient._transport().close()
    catch {
      case e: IOException => e.printStackTrace()
    }
  }

}