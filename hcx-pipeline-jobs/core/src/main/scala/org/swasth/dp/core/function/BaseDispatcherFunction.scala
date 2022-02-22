package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.util.{Constants, DispatcherUtil, JSONUtil}

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

  def createErrorMap(error: Option[ErrorResponse]):util.Map[String, AnyRef] = {
    val errorMap:util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
    errorMap.put("code",error.get.code.get)
    errorMap.put("message",error.get.message.get)
    errorMap.put("trace",error.get.trace.get)
    errorMap
  }

  def dispatchErrorResponse(event: util.Map[String, AnyRef], error: Option[ErrorResponse], correlationId: String, payloadRefId: String, senderCtx: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val protectedMap = new util.HashMap[String, AnyRef]
    //Update sender code
    protectedMap.put(Constants.SENDER_CODE, config.hcxRegistryCode)
    //Update recipient code
    protectedMap.put(Constants.RECIPIENT_CODE, getProtocolStringValue(event,Constants.SENDER_CODE))
    //Keep same correlationId
    protectedMap.put(Constants.CORRELATION_ID, getProtocolStringValue(event,Constants.CORRELATION_ID))
    //Keep same api call id
    protectedMap.put(Constants.API_CALL_ID, getProtocolStringValue(event,Constants.API_CALL_ID))
    //Keep same work flow id if it exists in the incoming event
    if(!getProtocolStringValue(event,Constants.WORKFLOW_ID).isEmpty)
    protectedMap.put(Constants.WORKFLOW_ID, getProtocolStringValue(event,Constants.WORKFLOW_ID))
    //Update error details
    protectedMap.put(Constants.ERROR_DETAILS,createErrorMap(error))
    //Update status
    protectedMap.put(Constants.HCX_STATUS,Constants.HCX_ERROR_STATUS)
    Console.println("Payload: " + protectedMap)
    val result = DispatcherUtil.dispatch(senderCtx, JSONUtil.serialize(protectedMap))
    if(result.retry) {
      metrics.incCounter(metric = config.dispatcherRetryCount)
      context.output(config.retryOutputTag, JSONUtil.serialize(event))
    }
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    //TODO Make changes here for handling redirect requests with flat JSON objects
    val correlationId = getProtocolStringValue(event,Constants.CORRELATION_ID)
    val payloadRefId = event.get(Constants.MID).asInstanceOf[String]
    // TODO change cdata to context after discussion.
    val senderCtx = event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.SENDER, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    val recipientCtx = event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.RECIPIENT, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    if (MapUtils.isEmpty(senderCtx)) {
      Console.println("sender context is empty for mid: " + payloadRefId)
      logger.warn("sender context is empty for mid: " + payloadRefId)
      //Audit the record if sender context is empty
      audit(event,true,context,metrics)
    } else if (MapUtils.isEmpty(recipientCtx)) {
      Console.println("recipient context is empty for mid: " + payloadRefId)
      logger.warn("recipient context is empty for mid: " + payloadRefId)
      //Send on_action request back to sender when recipient context is missing
      val errorResponse = ErrorResponse(Option(Constants.RECIPIENT_ERROR_CODE), Option(Constants.RECIPIENT_ERROR_MESSAGE), Option(Constants.RECIPIENT_ERROR_LOG))
      dispatchErrorResponse(event,ValidationResult(true, Option(errorResponse)).error, correlationId, payloadRefId, senderCtx, context, metrics)
    } else {
      Console.println("sender and recipient available for mid: " + payloadRefId)
      logger.info("sender and recipient available for mid: " + payloadRefId)
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
        event.put(Constants.UPDATED_TIME, Calendar.getInstance().getTime())
        if(result.success) {
          if(!event.get(Constants.ACTION).asInstanceOf[String].contains("on_"))
          event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].put(Constants.HCX_STATUS, Constants.HCX_DISPATCH_STATUS)
          audit(event, result.success, context, metrics);
          metrics.incCounter(metric = config.dispatcherSuccessCount)
        }
        if(result.retry) {
          metrics.incCounter(metric = config.dispatcherRetryCount)
          audit(event, result.success, context, metrics);
          //For retry place the incoming event into retry topic
          context.output(config.retryOutputTag, JSONUtil.serialize(event))
        }
        if(!result.retry && !result.success) {
          metrics.incCounter(metric = config.dispatcherFailedCount)
          audit(event, result.success, context, metrics);
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
    audit.put(Constants.RECIPIENT_CODE,getProtocolStringValue(event,Constants.RECIPIENT_CODE))
    audit.put(Constants.SENDER_CODE,getProtocolStringValue(event,Constants.SENDER_CODE))
    audit.put(Constants.API_CALL_ID,getProtocolStringValue(event,Constants.API_CALL_ID))
    audit.put(Constants.CORRELATION_ID,getProtocolStringValue(event,Constants.CORRELATION_ID))
    audit.put(Constants.WORKFLOW_ID,getProtocolStringValue(event,Constants.WORKFLOW_ID))
    audit.put(Constants.TIMESTAMP,getProtocolStringValue(event,Constants.TIMESTAMP))
    audit.put(Constants.ERROR_DETAILS,getProtocolMapValue(event,Constants.ERROR_DETAILS))
    audit.put(Constants.DEBUG_DETAILS,getProtocolMapValue(event,Constants.DEBUG_DETAILS))
    audit.put(Constants.MID,event.get(Constants.MID).asInstanceOf[String])
    audit.put(Constants.ACTION,event.get(Constants.ACTION).asInstanceOf[String])
    audit.put(Constants.HCX_STATUS,getProtocolStringValue(event,Constants.HCX_STATUS))
    audit.put(Constants.REQUESTED_TIME,event.get(Constants.ETS))
    audit.put(Constants.UPDATED_TIME,event.getOrDefault(Constants.UPDATED_TIME, Calendar.getInstance().getTime()))
    audit.put(Constants.AUDIT_TIMESTAMP, Calendar.getInstance().getTime())
    audit.put(Constants.SENDER_ROLE, getCDataListValue(event, Constants.SENDER, Constants.ROLES))
    audit.put(Constants.RECIPIENT_ROLE, getCDataListValue(event, Constants.RECIPIENT, Constants.ROLES))
    audit
  }

  def dispatchRecipient(baseSenderCode: String, action: String, parsedPayload: util.Map[String, AnyRef]) = {
    val recipientDetails = fetchDetails(baseSenderCode)
    val recipientContext = createRecipientContext(recipientDetails, action)
    val updatedPayload = new util.HashMap[String,AnyRef]()
    //TODO Remove this and use the utility for modifying the ciphertext
    updatedPayload.put(Constants.PAYLOAD,JSONUtil.createPayloadByValues(parsedPayload));
    DispatcherUtil.dispatch(recipientContext, JSONUtil.serialize(updatedPayload))
  }

  def getEmptyCipherText: String = {
    //TODO write logic here for fetching ciphertext value, as of now sending base 64 encoded string of an empty string
    val emptyCiphertext: String = "IiI="
    emptyCiphertext
  }

}
