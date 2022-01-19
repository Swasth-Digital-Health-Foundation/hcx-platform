package org.swasth.dp.search.functions

import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}
import org.swasth.dp.core.job.Metrics
import org.swasth.dp.core.util.{JSONUtil, PostgresConnect, PostgresConnectionConfig}
import org.swasth.dp.search.task.SearchConfig

import java.sql.Timestamp
import java.util
import java.util.UUID
import scala.collection.JavaConverters._
import scala.collection.mutable

class CompositeSearchFunction(config: SearchConfig, @transient var postgresConnect: PostgresConnect = null)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[CompositeSearchFunction])

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    if (postgresConnect == null) {
      postgresConnect = new PostgresConnect(PostgresConnectionConfig(
        user = config.postgresUser,
        password = config.postgresPassword,
        database = config.postgresDb,
        host = config.postgresHost,
        port = config.postgresPort,
        maxConnections = config.postgresMaxConnections
      ))
    }
  }

  override def close(): Unit = {
    super.close()
    postgresConnect.closeConnection()
  }

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(status = true, None)
  }

  override def getPayload(event: util.Map[String, AnyRef]): util.Map[String, AnyRef] = {
    val payloadRefId = getPayloadRefId(event)
    Console.println("Fetching payload from postgres for mid: " + payloadRefId)
    logger.info("Fetching payload from postgres for mid: " + payloadRefId)
    val postgresQuery = String.format("SELECT data FROM %s WHERE mid = '%s'", config.payloadTable, payloadRefId)
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {

      val resultSet = preparedStatement.executeQuery()
      if(resultSet.next()) {
        val payload = resultSet.getString(1)
        JSONUtil.deserialize[util.Map[String, AnyRef]](payload)
      } else {
        throw new Exception("Payload not found for the given reference mid: " + payloadRefId)
      }
    } catch {
      case ex: Exception => throw ex
    } finally {
      preparedStatement.close()
    }

  }

  override def audit(event: util.Map[String, AnyRef], status: Boolean, context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    val audit = createAuditRecord(event,"AUDIT")
    context.output(config.auditOutputTag, JSONUtil.serialize(audit))
    metrics.incCounter(config.auditEventsCount)
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    /**
     * TODO below logic.
     *  1. Modify the sender(HCX) and recipient(recipient id from search filters) in the payload
     *     2. Modify the x-hcx-request_id with the new generated request id
     *     3. Modify the search filters to have only one recipient while dispatching the request
     *     4. Dispatch request to the recipient by encoding the updated payload
     *          a. Successful Dispatch, Insert child record with status as Open
     *             b. In case of dispatch failure to the recipient, Insert child record with status as Fail
     */
    //Pay load from database
    val payload = getPayload(event)
    val encodedPayload = payload.get("payload").asInstanceOf[String]
    //Place the updated protected header values into this map and encode the values
    val parsedPayload: util.HashMap[String, AnyRef] = JSONUtil.parsePayload(encodedPayload)
    val protectString = parsedPayload.get("protected").asInstanceOf[String]
    //Decode protected String
    val protectedMap = JSONUtil.decodeBase64String(protectString,classOf[util.HashMap[String,AnyRef]])
    //Creating mutable map to update the protocted headers
    val mutableMap: mutable.Map[String,Object] = new mutable.HashMap[String,Object]
    //Add protected map values to mutable map
    protectedMap.forEach((k,v) => mutableMap.put(k, v))

    val protectedHeaders = event.get("headers").asInstanceOf[util.Map[String, AnyRef]].get("protocol").asInstanceOf[util.Map[String, AnyRef]]
    val recipientList = protectedHeaders.get("x-hcx-search").asInstanceOf[util.Map[String, AnyRef]].
          get("filters").asInstanceOf[util.Map[String, AnyRef]].
          get("receivers").asInstanceOf[util.List[String]]

    val workFlowId = getWorkflowId(event)
    val originalRequestId = getRequestId(event)
    val senderCode = getSenderCode(event)
    val action = getAction(event)
    //Insert base record
    insertSearchRecord(workFlowId, originalRequestId, senderCode, null, "OPEN", "{}")
    mutableMap.asJava.put("x-hcx-sender_code",config.hcxRegistryCode)
    //Iterate through all the recipients and dispatch requests with updated payload
    for (recipientCode <- recipientList.asScala) {
      mutableMap.asJava.put("x-hcx-recipient_code",recipientCode)
      val requestId = UUID.randomUUID().toString
      mutableMap.asJava.put("x-hcx-request_id",requestId)
      val filterList = new util.ArrayList[String]
      filterList.add(recipientCode)
      mutableMap.asJava.get("x-hcx-search").asInstanceOf[util.Map[String, AnyRef]].
        get("filters").asInstanceOf[util.Map[String, AnyRef]].put("receivers",filterList)
      //Update the protected headers for each request being sent to recipient
      event.get("headers").asInstanceOf[util.Map[String, AnyRef]].put("protocol",mutableMap.asJava)
      //place the updated protected headers in the parsed payload map
      parsedPayload.put("protected",JSONUtil.encodeBase64Object(mutableMap.asJava))
      val dispatchResult = dispatchRecipient(recipientCode,action,parsedPayload)
      if (!dispatchResult.success) {
        insertSearchRecord(workFlowId, requestId, config.hcxRegistryCode, recipientCode, "RETRY", "{}")
        //Update the protocol headers with the updated values
        context.output(config.retryOutputTag,JSONUtil.serialize(event))
      } else
        insertSearchRecord(workFlowId, requestId, config.hcxRegistryCode, recipientCode, "OPEN", "{}")
      //Audit the each child record after dispatching the request with the updated protected headers
      Console.println("Writing audit log for child record")

      audit(event,status = true,context,metrics)
    }
    Console.println("Writing into audit log for base record with workflowId:"+workFlowId)
    //Audit the incoming event
    audit(event,status = true,context,metrics)
  }


  @throws[Exception]
  def insertSearchRecord(workFlowId:String, requestId: String, senderCode: String, recipientCode: String, status: String, searchResponse: String): Unit = {
    val query: String = String.format ("INSERT INTO  %s (workflow_id,request_id,sender_code,recipient_code,response_status,response_data,request_time) " + "VALUES (?,?,?,?,?,?::JSON,?) ON CONFLICT ON CONSTRAINT composite_search_pkey DO NOTHING", config.searchTable)
    val preStatement = postgresConnect.getConnection.prepareStatement(query)
    try {
      postgresConnect.connection.setAutoCommit (false)
      preStatement.setString (1, workFlowId)
      preStatement.setString (2, requestId)
      preStatement.setString (3, senderCode)
      if (StringUtils.isNotEmpty(recipientCode))
        preStatement.setString (4, recipientCode)
      else
        preStatement.setNull (4, java.sql.Types.VARCHAR)
      preStatement.setString (5, status)
      preStatement.setObject (6, searchResponse)
      val requestTime: Timestamp = new Timestamp (System.currentTimeMillis)
      preStatement.setTimestamp (7, requestTime)
      preStatement.executeUpdate
      System.out.println ("Insert completed successfully for workflowId:" + workFlowId +" and requestId" +requestId)
      postgresConnect.connection.commit ()
    } catch {
      case e: Exception =>
        throw e
    } finally {
      if (preStatement != null) {
        preStatement.close ()
      }
    }
  }
}






