package org.swasth.dp.core.function

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.exception.PipelineException
import org.swasth.dp.core.job.{BaseJobConfig, BaseProcessFunction, Metrics}
import org.swasth.dp.core.service.AuditService
import org.swasth.dp.core.util._

import java.util
import java.util.{Calendar, UUID}

case class Response(timestamp: Long, correlation_id: String, error: Option[ErrorResponse])

case class ErrorResponse(code: Option[String], message: Option[String], trace: Option[String]);

case class ValidationResult(status: Boolean, error: Option[ErrorResponse])

case class DispatcherResult(success: Boolean, statusCode: Int, error: Option[ErrorResponse], retry: Boolean)

abstract class BaseDispatcherFunction(config: BaseJobConfig)
  extends BaseProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]](config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[BaseDispatcherFunction])

  var postgresConnect: PostgresConnect = _
  var auditService: AuditService = _
  var payload: util.Map[String, AnyRef] = _


  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    postgresConnect = new PostgresConnect(PostgresConnectionConfig(config.postgresUser, config.postgresPassword, config.postgresDb, config.postgresHost, config.postgresPort, config.postgresMaxConnections))
    auditService = new AuditService(config)
  }

  override def close(): Unit = {
    super.close()
    postgresConnect.closeConnection()
  }

  def validate(event: util.Map[String, AnyRef]): ValidationResult

  @throws(classOf[Exception])
  def getPayload(payloadRefId: String): util.Map[String, AnyRef] = {
    Console.println("Fetching payload from postgres for mid: " + payloadRefId)
    logger.info("Fetching payload from postgres for mid: " + payloadRefId)
    val postgresQuery = String.format("SELECT data FROM %s WHERE mid = '%s'", config.postgresTable, payloadRefId);
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {
      val resultSet = preparedStatement.executeQuery()
      if (resultSet.next()) {
        val payload = resultSet.getString(1)
        JSONUtil.deserialize[util.Map[String, AnyRef]](payload)
      } else {
        throw new Exception("Payload not found for the given reference id: " + payloadRefId)
      }
    } catch {
      case ex: Exception => throw ex
    } finally {
      preparedStatement.close()
    }

  }

  @throws(classOf[Exception])
  def audit(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    auditService.indexAudit(createAuditRecord(event))
    context.output(config.auditOutputTag, JSONUtil.serialize(createAuditLog(event)))
    metrics.incCounter(config.auditEventsCount)
  }

  def createErrorMap(error: Option[ErrorResponse]): util.Map[String, AnyRef] = {
    val errorMap: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]
    errorMap.put("code", error.get.code.get)
    errorMap.put("message", error.get.message.get)
    errorMap.put("trace", error.get.trace.get)
    errorMap
  }

  def dispatchErrorResponse(event: util.Map[String, AnyRef], error: Option[ErrorResponse], correlationId: String, payloadRefId: String, senderCtx: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val protectedMap = new util.HashMap[String, AnyRef]
    //Update sender code
    protectedMap.put(Constants.HCX_SENDER_CODE, config.hcxRegistryCode)
    //Update recipient code
    protectedMap.put(Constants.HCX_RECIPIENT_CODE, getProtocolStringValue(event, Constants.HCX_SENDER_CODE))
    //Keep same correlationId
    protectedMap.put(Constants.HCX_CORRELATION_ID, getProtocolStringValue(event, Constants.HCX_CORRELATION_ID))
    //Generate new UUID for each request processed by HCX Gateway
    protectedMap.put(Constants.API_CALL_ID, UUID.randomUUID())
    //Keep same work flow id if it exists in the incoming event
    if (!getProtocolStringValue(event, Constants.WORKFLOW_ID).isEmpty)
      protectedMap.put(Constants.WORKFLOW_ID, getProtocolStringValue(event, Constants.WORKFLOW_ID))
    //Update error details
    protectedMap.put(Constants.ERROR_DETAILS, createErrorMap(error))
    //Update status
    protectedMap.put(Constants.HCX_STATUS, Constants.ERROR_STATUS)
    Console.println("Payload: " + protectedMap)
    val result = dispatcherUtil.dispatch(senderCtx, JSONUtil.serialize(protectedMap))
    if (result.retry) {
      logger.info("Error while dispatching error response: " + result.error.get.message.get)
      metrics.incCounter(metric = config.dispatcherRetryCount)
    }
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    //TODO Make changes here for handling redirect requests with flat JSON objects
    val correlationId = getProtocolStringValue(event, Constants.HCX_CORRELATION_ID)
    val payloadRefId = event.get(Constants.MID).asInstanceOf[String]
    // TODO change cdata to context after discussion.
    val senderCtx = event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.SENDER, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    val recipientCtx = event.getOrDefault(Constants.CDATA, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]].getOrDefault(Constants.RECIPIENT, new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
    try {
      if (MapUtils.isEmpty(senderCtx)) {
        Console.println("sender context is empty for mid: " + payloadRefId)
        logger.warn("sender context is empty for mid: " + payloadRefId)
        //Audit the record if sender context is empty
        audit(event, context, metrics)
      } else if (MapUtils.isEmpty(recipientCtx)) {
        Console.println("recipient context is empty for mid: " + payloadRefId)
        logger.warn("recipient context is empty for mid: " + payloadRefId)
        //Send on_action request back to sender when recipient context is missing
        val errorResponse = ErrorResponse(Option(Constants.RECIPIENT_ERROR_CODE), Option(Constants.RECIPIENT_ERROR_MESSAGE), Option(Constants.RECIPIENT_ERROR_LOG))
        dispatchErrorResponse(event, ValidationResult(true, Option(errorResponse)).error, correlationId, payloadRefId, senderCtx, context, metrics)
      } else {
        Console.println("sender and recipient available for mid: " + payloadRefId)
        logger.info("sender and recipient available for mid: " + payloadRefId)
        val validationResult = validate(event)
        if (!validationResult.status) {
          metrics.incCounter(metric = config.dispatcherValidationFailedCount)
          audit(event, context, metrics);
          dispatchErrorResponse(event, validationResult.error, correlationId, payloadRefId, senderCtx, context, metrics)
        }

        if (validationResult.status) {
          metrics.incCounter(metric = config.dispatcherValidationSuccessCount)
          payload = getPayload(payloadRefId);
          val payloadJSON = JSONUtil.serialize(payload);
          val result = dispatcherUtil.dispatch(recipientCtx, payloadJSON)
          logger.info("result::" + result)
          //Adding updatedTimestamp for auditing
          event.put(Constants.UPDATED_TIME, Calendar.getInstance().getTime())
          if (result.success) {
            updateDBStatus(payloadRefId, Constants.DISPATCH_STATUS)
            setStatus(event, Constants.DISPATCH_STATUS)
            metrics.incCounter(metric = config.dispatcherSuccessCount)
          }
          if (result.retry) {
            var retryCount: Int = 0
            if (event.containsKey(Constants.RETRY_INDEX))
              retryCount = event.get(Constants.RETRY_INDEX).asInstanceOf[Int]
            if (!config.allowedEntitiesForRetry.contains(getEntity(event.get(Constants.ACTION).asInstanceOf[String])) || retryCount == config.maxRetry) {
              dispatchError(payloadRefId, event, result, correlationId, senderCtx, context, metrics)
            } else if (retryCount < config.maxRetry) {
              updateDBStatus(payloadRefId, Constants.REQ_RETRY)
              setStatus(event, Constants.QUEUED_STATUS)
              metrics.incCounter(metric = config.dispatcherRetryCount)
              Console.println("Event is updated for retrying..")
            }
          }
          if (!result.retry && !result.success) {
            dispatchError(payloadRefId, event, result, correlationId, senderCtx, context, metrics)
          }
          audit(event, context, metrics)
        }
      }
    } catch {
      case ex: PipelineException =>
        val errorResponse = ErrorResponse(Option(ex.code), Option(ex.message), Option(ex.trace))
        dispatchErrorResponse(event, ValidationResult(true, Option(errorResponse)).error, correlationId, payloadRefId, senderCtx, context, metrics)
    }
  }

  private def getEntity(path: String): String = {
    if (path.contains("status")) "status"
    else if (path.contains("on_search")) "searchresponse"
    else if (path.contains("search")) "search"
    else {
      val str = path.split("/")
      str(str.length - 2)
    }
  }

  private def dispatchError(payloadRefId: String, event: util.Map[String, AnyRef], result: DispatcherResult, correlationId: String, senderCtx: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    updateDBStatus(payloadRefId, Constants.ERROR_STATUS)
    setStatus(event, Constants.ERROR_STATUS)
    metrics.incCounter(metric = config.dispatcherFailedCount)
    dispatchErrorResponse(event, result.error, correlationId, payloadRefId, senderCtx, context, metrics)
  }

  private def executeDBQuery(query: String): Boolean = {
    val preparedStatement = postgresConnect.getConnection.prepareStatement(query)
    try {
      preparedStatement.execute()
    } catch {
      case ex: Exception => throw ex
    } finally {
      preparedStatement.close()
    }
  }

  private def updateDBStatus(payloadRefId: String, status: String): Unit = {
    val query = "UPDATE %s SET status = '%s', lastUpdatedOn = %d WHERE mid = '%s';".format(config.postgresTable, status, System.currentTimeMillis(), payloadRefId)
    executeDBQuery(query)
  }

  override def metricsList(): List[String] = {
    List(config.dispatcherSuccessCount, config.dispatcherFailedCount, config.dispatcherRetryCount, config.dispatcherValidationFailedCount, config.dispatcherValidationSuccessCount, config.auditEventsCount)
  }

  def createAuditRecord(event: util.Map[String, AnyRef]): util.Map[String, AnyRef] = {
    val audit = new util.HashMap[String, AnyRef]();
    audit.put(Constants.EID, Constants.AUDIT)
    audit.put(Constants.HCX_RECIPIENT_CODE, getProtocolStringValue(event, Constants.HCX_RECIPIENT_CODE))
    audit.put(Constants.HCX_SENDER_CODE, getProtocolStringValue(event, Constants.HCX_SENDER_CODE))
    audit.put(Constants.API_CALL_ID, getProtocolStringValue(event, Constants.API_CALL_ID))
    audit.put(Constants.HCX_CORRELATION_ID, getProtocolStringValue(event, Constants.HCX_CORRELATION_ID))
    audit.put(Constants.WORKFLOW_ID, getProtocolStringValue(event, Constants.WORKFLOW_ID))
    audit.put(Constants.HCX_TIMESTAMP, getProtocolStringValue(event, Constants.HCX_TIMESTAMP))
    audit.put(Constants.ERROR_DETAILS, getProtocolMapValue(event, Constants.ERROR_DETAILS))
    audit.put(Constants.DEBUG_DETAILS, getProtocolMapValue(event, Constants.DEBUG_DETAILS))
    audit.put(Constants.MID, event.get(Constants.MID).asInstanceOf[String])
    audit.put(Constants.ACTION, event.get(Constants.ACTION).asInstanceOf[String])
    audit.put(Constants.HCX_STATUS, getProtocolStringValue(event, Constants.HCX_STATUS))
    audit.put(Constants.REQUESTED_TIME, event.get(Constants.ETS))
    audit.put(Constants.UPDATED_TIME, event.getOrDefault(Constants.UPDATED_TIME, Calendar.getInstance().getTime()))
    audit.put(Constants.ETS, Calendar.getInstance().getTime())
    audit.put(Constants.SENDER_ROLE, getCDataListValue(event, Constants.SENDER, Constants.ROLES))
    audit.put(Constants.RECIPIENT_ROLE, getCDataListValue(event, Constants.RECIPIENT, Constants.ROLES))
    audit.put(Constants.SENDER_NAME, getCDataStringValue(event, Constants.SENDER, Constants.PARTICIPANT_NAME))
    audit.put(Constants.RECIPIENT_NAME, getCDataStringValue(event, Constants.RECIPIENT, Constants.PARTICIPANT_NAME))
    audit.put(Constants.SENDER_PRIMARY_EMAIL, getCDataStringValue(event, Constants.SENDER, Constants.PRIMARY_EMAIL))
    audit.put(Constants.RECIPIENT_PRIMARY_EMAIL, getCDataStringValue(event, Constants.RECIPIENT, Constants.PRIMARY_EMAIL))
    audit.put(Constants.PAYLOAD, removeSensitiveData(payload))
    getTag(event,audit)
    audit
  }

  def createAuditLog(event: util.Map[String, AnyRef]): util.Map[String, AnyRef] = {
    val audit = new util.HashMap[String, AnyRef]()
    audit.put(Constants.EID, Constants.AUDIT)
    audit.put(Constants.ETS, Calendar.getInstance().getTime)
    audit.put(Constants.MID, event.get(Constants.MID).asInstanceOf[String])
    audit.put(Constants.OBJECT, new util.HashMap[String, AnyRef]() {
      {
        put(Constants.ID, getProtocolStringValue(event, Constants.HCX_CORRELATION_ID))
        put(Constants.TYPE, getEntity(event.get(Constants.ACTION).asInstanceOf[String]))
      }
    })
    audit.put(Constants.CDATA, new util.HashMap[String, AnyRef]() {
      {
        put(Constants.ACTION, event.get(Constants.ACTION).asInstanceOf[String])
        putAll(event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]])
      }
    })
    audit.put(Constants.EDATA, new util.HashMap[String, AnyRef]() {
      {
        put(Constants.STATUS, getProtocolStringValue(event, Constants.HCX_STATUS))
      }
    })
    audit
  }

  def removeSensitiveData(payload: util.Map[String, AnyRef]): String = {
    if (payload.containsKey(Constants.PAYLOAD)) {
      val modifiedPayload = payload.get(Constants.PAYLOAD).asInstanceOf[String].split("\\.").toBuffer
      // remove encryption key
      modifiedPayload.remove(1)
      // remove ciphertext
      modifiedPayload.remove(2)
      val payloadValues: Array[String] = modifiedPayload.toArray
      val sb = new StringBuffer()
      for (value <- payloadValues) {
        sb.append(value).append(".")
      }
      sb.deleteCharAt(sb.length() - 1).toString
    } else {
      JSONUtil.serialize(payload)
    }
  }

  def dispatchRecipient(baseSenderCode: String, action: String, parsedPayload: util.Map[String, AnyRef]) = {
    val recipientDetails = fetchDetails(baseSenderCode)
    val recipientContext = createRecipientContext(recipientDetails, action)
    val updatedPayload = new util.HashMap[String, AnyRef]()
    //TODO Remove this and use the utility for modifying the ciphertext
    updatedPayload.put(Constants.PAYLOAD, JSONUtil.createPayloadByValues(parsedPayload));
    dispatcherUtil.dispatch(recipientContext, JSONUtil.serialize(updatedPayload))
  }

  def getEmptyCipherText: String = {
    //TODO write logic here for fetching ciphertext value, as of now sending base 64 encoded string of an empty string
    val emptyCiphertext: String = "IiI="
    emptyCiphertext
  }


  def getTag(event : util.Map[String, AnyRef],audit: util.HashMap[String, AnyRef]): Unit = {
    var tagSet: Set[String] = Set()
    tagSet += getCDataListValue(event, Constants.SENDER, Constants.TAGS).toString
    tagSet += getCDataListValue(event, Constants.RECIPIENT, Constants.TAGS).toString
    if (!StringUtils.isEmpty(config.tag)) {
      tagSet += config.tag
    }
    val tag = tagSet.toString.replace("[", "").replace("]", "").replace(" ", "")
    if (tag.nonEmpty) {
      audit.put(Constants.TAGS, tag)
    }
  }
}
