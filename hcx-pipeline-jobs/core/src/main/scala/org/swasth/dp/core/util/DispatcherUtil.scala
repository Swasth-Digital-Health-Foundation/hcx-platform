package org.swasth.dp.core.util

import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HttpContext
import org.swasth.dp.core.function.{DispatcherResult, Response}
import org.apache.http.util.EntityUtils
import java.nio.charset.StandardCharsets

import java.io.IOException
import java.util

object DispatcherUtil {

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
    val httpPost = new HttpPost(url);
    headers.map(f => httpPost.addHeader(f._1, f._2));
    httpPost.setEntity(new StringEntity(payload))
    httpPost.setHeader("Accept", "application/json")
    httpPost.setHeader("Content-type", "application/json")
    try {
      val response = httpClient.execute(httpPost);
      val statusCode = response.getStatusLine().getStatusCode();
      Console.println("statusCode", statusCode);
      response.close()
      if(successCodes.contains(statusCode)) {
        DispatcherResult(true, statusCode, None, false)
      } else if(errorCodes.contains(statusCode)) {
        val responseBody = EntityUtils.toString(response.getEntity, StandardCharsets.UTF_8);
        val responseJSON: Response = JSONUtil.deserialize[Response](responseBody);
        Console.println("responseJSON", responseJSON);
        DispatcherResult(false, statusCode, responseJSON.error, false)
      } else {
        DispatcherResult(false, statusCode, None, true)
      }
    } catch {
      case ex:Exception => {
        ex.printStackTrace()
        DispatcherResult(false, 0, None, true)
      }
    }
  }

}
