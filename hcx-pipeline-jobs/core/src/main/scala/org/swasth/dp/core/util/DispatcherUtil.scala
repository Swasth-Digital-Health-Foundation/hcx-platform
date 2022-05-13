package org.swasth.dp.core.util

import org.apache.commons.lang3.StringUtils
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.swasth.dp.core.function.{DispatcherResult, ErrorResponse}
import org.swasth.dp.core.job.BaseJobConfig

import java.nio.charset.StandardCharsets
import java.util

class DispatcherUtil(config: BaseJobConfig) extends Serializable {

  val jwtUtil = new JWTUtil(config)

  val httpClient: CloseableHttpClient = new HttpUtil().getHttpClient()

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
        httpPost.setHeader("Authorization", "Bearer "+ jwtUtil.generateToken())
        response = httpClient.execute(httpPost);
        val statusCode = response.getStatusLine().getStatusCode();
        Console.println("statusCode", statusCode);
        if (config.successCodes.contains(statusCode)) {
          DispatcherResult(true, statusCode, null, false)
        } else if (config.errorCodes.contains(statusCode)) {
          val errorResponse: ErrorResponse = errorMessageProcess(response)
          DispatcherResult(false, statusCode, Option(errorResponse), false)
        } else {
          val errorResponse: ErrorResponse = errorMessageProcess(response)
          DispatcherResult(false, statusCode, Option(errorResponse), true)
        }
      } else  //As url is null, no need to retry
        DispatcherResult(false, 0, null, false)
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

}
