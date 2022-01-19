package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.{DispatcherUtil, JSONUtil}

import java.util
import java.util.Calendar

case class Response(timestamp: Long, correlation_id: String, error: Option[ErrorResponse])
case class ErrorResponse(code: Option[String], message: Option[String], trace: Option[String]);
case class ValidationResult(status: Boolean, error: Option[ErrorResponse])
case class DispatcherResult(success: Boolean, statusCode: Int, error: Option[ErrorResponse], retry: Boolean)

abstract class BaseDispatcherFunction (config: BaseJobConfig)
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[BaseDispatcherFunction])

  def validate(event: util.Map[String, AnyRef]):ValidationResult

  @throws(classOf[Exception])
  def getPayload(event: util.Map[String, AnyRef]): util.Map[String, AnyRef]

  @throws(classOf[Exception])
  def audit(event: util.Map[String, AnyRef], status: Boolean, context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit

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
    val senderCtx = event.getOrDefault("cdata", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault("sender", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    val recipientCtx = event.getOrDefault("cdata", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault("recipient", new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    //Adding requestTimestamp for auditing
    event.put("requestTimeStamp", Calendar.getInstance().getTime())
    if (MapUtils.isEmpty(senderCtx)) {
      Console.println("sender context is empty: " + payloadRefId)
      logger.warn("sender context is empty: " + payloadRefId)
      //Audit the record if sender context is empty
      audit(event,true,context,metrics)
    } else if (MapUtils.isEmpty(recipientCtx)) {
      Console.println("recipient context is empty: " + payloadRefId)
      logger.warn("recipient context is empty: " + payloadRefId)
      //Send on_action request back to sender when recipient context is missing
      dispatchErrorResponse(ValidationResult(true, None).error, correlationId, payloadRefId, senderCtx, context, metrics)
    } else {
      Console.println("sender and recipient available: " + payloadRefId)
      logger.info("sender and recipient available: " + payloadRefId)
      val validationResult = validate(event)
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
        //Adding updatedTimestamp for auditing
        event.put("updatedTimestamp", Calendar.getInstance().getTime())
        audit(event, result.success, context, metrics);
        if(result.success) {
          metrics.incCounter(metric = config.dispatcherSuccessCount)
        }
        if(result.retry) {
          metrics.incCounter(metric = config.dispatcherRetryCount)
          //For retry place the incoming event into retry topic
          //val retryEvent = new util.HashMap[String, AnyRef]();
          //retryEvent.put("ctx", recipientCtx);
          //retryEvent.put("payloadRefId", event.get("mid"));
          context.output(config.retryOutputTag, JSONUtil.serialize(event))
        }
        if(!result.retry && !result.success) {
          metrics.incCounter(metric = config.dispatcherFailedCount)
          dispatchErrorResponse(result.error, correlationId, payloadRefId, senderCtx, context, metrics)
        }
      }
    }
  }

  override def metricsList(): List[String] = {
    List(config.dispatcherSuccessCount, config.dispatcherFailedCount, config.dispatcherRetryCount, config.dispatcherValidationFailedCount, config.dispatcherValidationSuccessCount, config.auditEventsCount)
  }

  // Audit related functions
  def getRecipientCode(event: util.Map[String, AnyRef]): String = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("protocol").asInstanceOf[util.Map[String, AnyRef]]
      .get("x-hcx-recipient_code").asInstanceOf[String]
  }

  def getSenderCode(event: util.Map[String, AnyRef]): String = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("protocol").asInstanceOf[util.Map[String, AnyRef]]
      .get("x-hcx-sender_code").asInstanceOf[String]
  }

  def getRequestId(event: util.Map[String, AnyRef]): String = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("protocol").asInstanceOf[util.Map[String, AnyRef]]
      .get("x-hcx-request_id").asInstanceOf[String]
  }

  def getHcxTimestamp(event: util.Map[String, AnyRef]): String = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("protocol").asInstanceOf[util.Map[String, AnyRef]]
      .get("x-hcx-timestamp").asInstanceOf[String]
  }

  def getWorkflowId(event: util.Map[String, AnyRef]): String = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("protocol").asInstanceOf[util.Map[String, AnyRef]]
      .get("x-hcx-workflow_id").asInstanceOf[String]
  }

  def getAction(event: util.Map[String, AnyRef]): String = {
    event.get("action").asInstanceOf[String]
  }

  def getLogDetails(event: util.Map[String, AnyRef]): util.Map[String, AnyRef] = {
    event.get("log_details").asInstanceOf[util.Map[String, AnyRef]]
  }

  def getJose(event: util.Map[String, AnyRef]): util.Map[String, AnyRef] = {
    event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
      .get("jose").asInstanceOf[util.Map[String, AnyRef]]
  }

  def getStatus(event: util.Map[String, AnyRef]): String = {
    event.get("status").asInstanceOf[String]
  }

  def createAuditRecord(event: util.Map[String, AnyRef], auditName: String): util.Map[String, AnyRef] = {
    val audit = new util.HashMap[String, AnyRef]();
    audit.put("eid", auditName)
    event.forEach((k,v) => audit.put(k, v))
    audit.put("auditTimeStamp", Calendar.getInstance().getTime())
    audit
  }

  def dispatchRecipient(baseSenderCode: String, action: String, parsedPayload: util.Map[String, AnyRef]) = {
    val recipientDetails = fetchDetails(baseSenderCode)
    val recipientContext = createRecipientContext(recipientDetails, action)
    val updatedPayload = new util.HashMap[String,AnyRef]()
    updatedPayload.put("payload",JSONUtil.createPayloadByValues(parsedPayload));
    DispatcherUtil.dispatch(recipientContext, JSONUtil.serialize(updatedPayload))
  }

}
