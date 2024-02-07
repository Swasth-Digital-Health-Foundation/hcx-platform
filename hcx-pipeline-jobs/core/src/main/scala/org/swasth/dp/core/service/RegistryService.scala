package org.swasth.dp.core.service

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.{Constants, HttpUtil, JSONUtil}

import java.util
import scala.io.Source

class RegistryService(config: BaseJobConfig) {

  val httpClient: CloseableHttpClient = new HttpUtil().getHttpClient()

  def  getParticipantDetails(filters: String): util.ArrayList[util.Map[String, AnyRef]]= {
    // payload for registry search
    val payload = s"""{"entityType":["Organisation"],"filters":$filters}"""
    //Console.println("registry payload", payload)
    val httpPost = new HttpPost(config.hcxApisUrl + Constants.PARTICIPANT_SEARCH)
    httpPost.setEntity(new StringEntity(payload))
    httpPost.setHeader("Accept", "application/json")
    httpPost.setHeader("Content-type", "application/json")
    try {
      val response = httpClient.execute(httpPost)
      val statusCode = response.getStatusLine().getStatusCode()
      //Console.println("registryAPI statusCode", statusCode)
      val entity = response.getEntity
      val inputStream = entity.getContent
      val content = JSONUtil.deserialize[util.Map[String, AnyRef]](Source.fromInputStream(inputStream, "UTF-8").getLines.mkString)
      inputStream.close()
      response.close()
      content.get(Constants.PARTICIPANTS).asInstanceOf[util.ArrayList[util.Map[String, AnyRef]]]
    } catch {
      case ex: Exception => {
        ex.printStackTrace()
        new util.ArrayList
      }
    }
  }

}
