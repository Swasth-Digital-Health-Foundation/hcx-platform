package org.swasth.dp.core.util

import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.protocol.HttpContext

import java.io.IOException

class HttpUtil {

  val requestConfig: RequestConfig = RequestConfig.custom()
    .setCookieSpec(CookieSpecs.STANDARD)
    .setConnectTimeout(5 * 1000) // TODO: load from config
    .setConnectionRequestTimeout(5 * 1000) // TODO: load from config
    .setSocketTimeout(60 * 1000) // TODO: load from config
    .build()

  val retryHandler: HttpRequestRetryHandler = new HttpRequestRetryHandler {
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

  val httpClient: CloseableHttpClient = HttpClientBuilder
    .create()
    .setRetryHandler(retryHandler)
    .setDefaultRequestConfig(requestConfig)
    .build()

  def getHttpClient(): CloseableHttpClient = {
    httpClient
  }

}
