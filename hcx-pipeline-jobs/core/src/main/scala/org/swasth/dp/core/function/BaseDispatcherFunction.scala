package org.swasth.dp.core.function

import org.apache.flink.streaming.api.functions.ProcessFunction

import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.{DispatcherUtil, JSONUtil}

import java.util

case class Response(timestamp: Long, correlation_id: String, error: Option[ErrorResponse])
case class ErrorResponse(code: Option[String], message: Option[String], trace: Option[String]);
case class ValidationResult(status: Boolean, error: Option[ErrorResponse])
case class DispatcherResult(success: Boolean, statusCode: Int, error: Option[ErrorResponse], retry: Boolean)

abstract class BaseDispatcherFunction (config: BaseJobConfig)
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {

  def validate(event: util.Map[String, AnyRef]):ValidationResult

  def getPayload(event: util.Map[String, AnyRef]): util.Map[String, AnyRef]

  def audit(event: util.Map[String, AnyRef], status: Boolean, context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics);

  def getCorrelationId(event: util.Map[String, AnyRef]): String = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("protocol").asInstanceOf[util.Map[String, AnyRef]]
      .get("x-hcx-correlation_id").asInstanceOf[String]
  }

  def getPayloadRefId(event: util.Map[String, AnyRef]): String = {
    event.get("mid").asInstanceOf[String]
  }

  def dispatchErrorResponse(error: Option[ErrorResponse], correlationId: String, payloadRefId: String, senderCtx: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val response = Response(System.currentTimeMillis(), correlationId, error)
    val responseJSON = JSONUtil.serialize(response);
    val result = DispatcherUtil.dispatch(senderCtx, responseJSON)
    if(result.retry) {
      metrics.incCounter(metric = config.dispatcherRetryCount)
      val retryEvent = new util.HashMap[String, AnyRef]();
      retryEvent.put("ctx", senderCtx);
      retryEvent.put("payloadRefId", payloadRefId);
      retryEvent.put("payloadData", responseJSON);
      context.output(config.retryOutputTag, retryEvent)
    }
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {

    val correlationId = getCorrelationId(event);
    val payloadRefId = event.get("mid").asInstanceOf[String]
    // TODO change cdata to context after discussion.
    val recipientCtx = event.get("cdata").asInstanceOf[util.Map[String, AnyRef]].get("recipient").asInstanceOf[util.Map[String, AnyRef]]
    val senderCtx = event.get("cdata").asInstanceOf[util.Map[String, AnyRef]].get("sender").asInstanceOf[util.Map[String, AnyRef]]

    val validationResult = validate(event);

    if(!validationResult.status) {
      metrics.incCounter(metric = config.dispatcherValidationFailedCount)
      audit(event, validationResult.status, context, metrics);
      dispatchErrorResponse(validationResult.error, correlationId, payloadRefId, senderCtx, context, metrics)
    }

    if(validationResult.status) {
      metrics.incCounter(metric = config.dispatcherValidationSuccessCount)
      val payload = getPayload(event);
      val payloadJSON = JSONUtil.serialize(payload);
      val result = DispatcherUtil.dispatch(recipientCtx, payloadJSON)
      audit(event, result.success, context, metrics);
      if(result.success) {
        metrics.incCounter(metric = config.dispatcherSuccessCount)
      }
      if(result.retry) {
        metrics.incCounter(metric = config.dispatcherRetryCount)
        val retryEvent = new util.HashMap[String, AnyRef]();
        retryEvent.put("ctx", recipientCtx);
        retryEvent.put("payloadRefId", event.get("mid"));
        context.output(config.retryOutputTag, retryEvent)
      }
      if(!result.retry && !result.success) {
        metrics.incCounter(metric = config.dispatcherFailedCount)
        dispatchErrorResponse(result.error, correlationId, payloadRefId, senderCtx, context, metrics)
      }
    }
  }

  override def metricsList(): List[String] = {
    List(config.dispatcherSuccessCount, config.dispatcherFailedCount, config.dispatcherRetryCount, config.dispatcherValidationFailedCount, config.dispatcherValidationSuccessCount, config.auditEventsCount)
  }
}
