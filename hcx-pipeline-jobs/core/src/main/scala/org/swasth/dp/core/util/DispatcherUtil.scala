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
        val token = jwtUtil.generateHCXGatewayToken().asInstanceOf[String]
        Console.println("HCX Token: " + token)
        httpPost.setHeader("Authorization", "Bearer "+ token)
        response = httpClient.execute(httpPost);
        val statusCode = response.getStatusLine().getStatusCode();
        val responseBody = EntityUtils.toString(response.getEntity, StandardCharsets.UTF_8)
        Console.println("Status code: " + statusCode + " :: Response body: " + responseBody);
        if (config.successCodes.contains(statusCode)) {
          DispatcherResult(true, statusCode, null, false)
        } else if (config.errorCodes.contains(statusCode)) {
          val errorResponse: ErrorResponse = errorMessageProcess(responseBody)
          DispatcherResult(false, statusCode, Option(errorResponse), false)
        } else {
          val errorResponse: ErrorResponse = errorMessageProcess(responseBody)
          DispatcherResult(false, statusCode, Option(errorResponse), true)
        }
      } else  //As url is null, no need to retry
        DispatcherResult(false, 0, null, false)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        val errorResponse: ErrorResponse = ErrorResponse(Option(""), Option(ex.getMessage), Option(""))
        DispatcherResult(false, 0, Option(errorResponse), true)
    } finally {
      if (response != null)
        response.close()
    }
  }

  private def errorMessageProcess(responseBody: String) = {
    val responseMap = JSONUtil.deserialize[util.Map[String, AnyRef]](responseBody)
    val error = responseMap.getOrDefault(Constants.ERROR, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    val errorResponse = ErrorResponse(Option(error.getOrDefault(Constants.CODE, "").asInstanceOf[String]), Option(error.getOrDefault(Constants.MESSAGE, responseBody).asInstanceOf[String]), Option(error.getOrDefault(Constants.TRACE, "").asInstanceOf[String]))
    errorResponse
  }

}
