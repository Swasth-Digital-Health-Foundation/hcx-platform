package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.{DispatcherUtil, JSONUtil, Constants}

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
  def getPayload(payloadRefId: String): util.Map[String, AnyRef]

  @throws(classOf[Exception])
  def audit(event: util.Map[String, AnyRef], status: Boolean, context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit

  def dispatchErrorResponse(event: util.Map[String, AnyRef],error: Option[ErrorResponse], correlationId: String, payloadRefId: String, senderCtx: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val response = Response(System.currentTimeMillis(), correlationId, error)
    val responseJSON = JSONUtil.serialize(response);
    Console.println("Error message dispatch:"+responseJSON)
    val payload = getPayload(payloadRefId);
    val payloadJSON = JSONUtil.serialize(payload);
    //TODO Decode the payload, add error response to the payload, encode the updated payload
    //TODO As of now sending the same payload sent by the recipient incase of failure
    val result = DispatcherUtil.dispatch(senderCtx, payloadJSON)
    if(result.retry) {
      metrics.incCounter(metric = config.dispatcherRetryCount)
      context.output(config.retryOutputTag, JSONUtil.serialize(event))
    }
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {

    val correlationId = getProtocolHeaderValue(event,Constants.CORRELATION_ID)
    val payloadRefId = event.get(Constants.MID).asInstanceOf[String]
    // TODO change cdata to context after discussion.
    val senderCtx = event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.SENDER, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    val recipientCtx = event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.RECIPIENT, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
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
      val errorResponse = ErrorResponse(Option("Error"), Option("CLIENT_ERR_RECIPIENT_ENDPOINT_NOT_AVAILABLE"), Option("Please provide correct recipient details"))
      dispatchErrorResponse(event,ValidationResult(true, Option(errorResponse)).error, correlationId, payloadRefId, senderCtx, context, metrics)
    } else {
      Console.println("sender and recipient available: " + payloadRefId)
      logger.info("sender and recipient available: " + payloadRefId)
      val validationResult = validate(event)
      if(!validationResult.status) {
        metrics.incCounter(metric = config.dispatcherValidationFailedCount)
        audit(event, validationResult.status, context, metrics);
        dispatchErrorResponse(event,validationResult.error, correlationId, payloadRefId, senderCtx, context, metrics)
      }

      if(validationResult.status) {
        metrics.incCounter(metric = config.dispatcherValidationSuccessCount)
        val payload = getPayload(payloadRefId);
        val payloadJSON = JSONUtil.serialize(payload);
        val result = DispatcherUtil.dispatch(recipientCtx, payloadJSON)
        //Adding updatedTimestamp for auditing
        audit(event, result.success, context, metrics);
        if(result.success) {
          metrics.incCounter(metric = config.dispatcherSuccessCount)
        }
        if(result.retry) {
          metrics.incCounter(metric = config.dispatcherRetryCount)
          //For retry place the incoming event into retry topic
          context.output(config.retryOutputTag, JSONUtil.serialize(event))
        }
        if(!result.retry && !result.success) {
          metrics.incCounter(metric = config.dispatcherFailedCount)
          dispatchErrorResponse(event,result.error, correlationId, payloadRefId, senderCtx, context, metrics)
        }
      }
    }
  }

  override def metricsList(): List[String] = {
    List(config.dispatcherSuccessCount, config.dispatcherFailedCount, config.dispatcherRetryCount, config.dispatcherValidationFailedCount, config.dispatcherValidationSuccessCount, config.auditEventsCount)
  }

  def createAuditRecord(event: util.Map[String, AnyRef], auditName: String): util.Map[String, AnyRef] = {
    val audit = new util.HashMap[String, AnyRef]();
    audit.put(Constants.AUDIT_ID, auditName)
    event.forEach((k,v) => audit.put(k, v))
    audit.put(Constants.AUDIT_TIMESTAMP, Calendar.getInstance().getTime())
    audit
  }

  def dispatchRecipient(baseSenderCode: String, action: String, parsedPayload: util.Map[String, AnyRef]) = {
    val recipientDetails = fetchDetails(baseSenderCode)
    val recipientContext = createRecipientContext(recipientDetails, action)
    val updatedPayload = new util.HashMap[String,AnyRef]()
    updatedPayload.put(Constants.PAYLOAD,JSONUtil.createPayloadByValues(parsedPayload));
    DispatcherUtil.dispatch(recipientContext, JSONUtil.serialize(updatedPayload))
  }

}
