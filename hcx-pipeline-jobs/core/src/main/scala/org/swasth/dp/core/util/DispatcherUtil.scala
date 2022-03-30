package org.swasth.dp.core.util

import org.apache.commons.lang3.StringUtils
import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import org.swasth.dp.core.function.{DispatcherResult, ErrorResponse}
import org.swasth.dp.core.job.BaseJobConfig

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util
import scala.io.Source

class DispatcherUtil(config: BaseJobConfig) extends Serializable {

  val jwtUtil = new JWTUtil(config)

  val successCodes = Array(200, 202) // TODO: load from config
  val errorCodes = Array(400, 401, 403, 404) // TODO: load from config

  val requestConfig = RequestConfig.custom()
    .setCookieSpec(CookieSpecs.STANDARD)
    .setConnectTimeout(5 * 1000) // TODO: load from config
    .setConnectionRequestTimeout(5 * 1000) // TODO: load from config
    .setSocketTimeout(60 * 1000) // TODO: load from config
    .build()

  val retryHandler = new HttpRequestRetryHandler {
    override def retryRequest(exception: IOException, executionCount: Int, httpContext: HttpContext): Boolean = {
      Console.println("HTTP retry request execution count", executionCount)
      if (executionCount > 3) { // TODO: load from config
        return false
      } else {
        // wait a second before retrying again
        Thread.sleep(1000) // TODO: load from config
        return true
      }
    }
  }

  val httpClient = HttpClientBuilder
    .create()
    .setRetryHandler(retryHandler)
    .setDefaultRequestConfig(requestConfig)
    .build()


  def dispatch(ctx: util.Map[String, AnyRef], payload: String): DispatcherResult = {
    val url = ctx.get("endpoint_url").asInstanceOf[String]
    val headers = ctx.getOrDefault("headers", Map[String, String]()).asInstanceOf[Map[String, String]]
    Console.println("URL", url)
    var response: CloseableHttpResponse = null
    try {
      if (StringUtils.isNotEmpty(url)) {
        val httpPost = new HttpPost(url);
        headers.map(f => httpPost.addHeader(f._1, f._2));
        httpPost.setEntity(new StringEntity(payload))
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-type", "application/json")
        Console.println("Testing " + jwtUtil.generateToken())
        httpPost.setHeader("Authorization", "Bearer "+ jwtUtil.generateToken())
        response = httpClient.execute(httpPost);
        val statusCode = response.getStatusLine().getStatusCode();
        Console.println("statusCode", statusCode);
        if (successCodes.contains(statusCode)) {
          DispatcherResult(true, statusCode, None, false)
        } else if (errorCodes.contains(statusCode)) {
          val errorResponse: ErrorResponse = errorMessageProcess(response)
          DispatcherResult(false, statusCode, Option(errorResponse), false)
        } else {
          val errorResponse: ErrorResponse = errorMessageProcess(response)
          DispatcherResult(false, statusCode, Option(errorResponse), true)
        }
      } else  //As url is null, no need to retry
        DispatcherResult(false, 0, None, false)
    } catch {
      case ex: Exception =>
        val errorResponse: ErrorResponse = ErrorResponse(Option(Constants.RECIPIENT_ERROR_CODE), Option(Constants.RECIPIENT_ERROR_MESSAGE), Option(""))
        DispatcherResult(false, 0, Option(errorResponse), true)
    } finally {
      if (response != null)
        response.close()
    }
  }

  private def errorMessageProcess(response: CloseableHttpResponse) = {
    val responseBody = EntityUtils.toString(response.getEntity, StandardCharsets.UTF_8)
    val errorResponse = ErrorResponse(Option(Constants.RECIPIENT_ERROR_CODE), Option(Constants.RECIPIENT_ERROR_MESSAGE), Option(responseBody))
    errorResponse
  }

  def post(url: String, code: String): String= {
    // payload for registry search
    val payload = s"""{"entityType":["Organisation"],"filters":{"osid":{"eq":"$code"}}}"""
    Console.println("registry payload", payload)
    Console.println("Registry URL", url)
    val httpPost = new HttpPost(url);
    httpPost.setEntity(new StringEntity(payload))
    httpPost.setHeader("Accept", "application/json")
    httpPost.setHeader("Content-type", "application/json")
    try {
      val response = httpClient.execute(httpPost);
      val statusCode = response.getStatusLine().getStatusCode();
      Console.println("registryAPI statusCode", statusCode);
      val entity = response.getEntity
      val inputStream = entity.getContent
      val content = Source.fromInputStream(inputStream, "UTF-8").getLines.mkString
      inputStream.close()
      response.close()
      content
    } catch {
      case ex: Exception => {
        ex.printStackTrace()
        "[{}]"
      }
    }
  }

}
