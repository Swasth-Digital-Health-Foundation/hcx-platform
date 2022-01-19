package org.swasth.dp.searchresponse.functions

import com.google.gson.Gson
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, DispatcherResult, ValidationResult}
import org.swasth.dp.core.job.Metrics
import org.swasth.dp.core.util.{JSONUtil, PostgresConnect, PostgresConnectionConfig}
import org.swasth.dp.searchresponse.task.SearchResponseConfig

import java.util
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

class SearchResponseFunction(config: SearchResponseConfig, @transient var postgresConnect: PostgresConnect = null)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[SearchResponseFunction])

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
        throw new Exception("Payload not found for the given reference id: " + payloadRefId)
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

  def getEmptyCipherText: String = {
    //TODO write logic here for fetching ciphertext value, as of now sending base 64 encoded string of an empty string
    val emptyCiphertext:String = "IiI="
    emptyCiphertext
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    /**
     * TODO below logic.
     *  1. Fetch the baseRecord based on workflowId,
     *     if status is complete, do nothing
     *     else proceed with next step
     *     2. Modify the sender(HCX) and recipient(original sender from base record) in the payload
     *     3. Modify the x-hcx-request_id with the requestId from the base record
     *     4. Modify the x-hcx-status as response.partial for each request and response.complete for final record
     *     4. Dispatch request to the sender
     *     5.      Success: Update child record, with status as Close and response_data with the search response from the event
     *     Update base record with status as PARTIAL
     *     FAIL: Update the child record, with status as RETRY and response_data with the search response from the event
     */
    val workflowId = getWorkflowId(event)
    val action = getAction(event)
    val requestId = getRequestId(event)
    val baseRecord = getBaseRecord(workflowId)
    if(!"CLOSE".equals(baseRecord.responseStatus)){
      val payload = getPayload(event)
      val encodedPayload = payload.get("payload").asInstanceOf[String]
      //Place the updated protected header values into this map and encode the values
      val parsedPayload: util.HashMap[String, AnyRef] = JSONUtil.parsePayload(encodedPayload)
      val protectString = parsedPayload.get("protected").asInstanceOf[String]
      //Decode protected String
      val protectedMap = JSONUtil.decodeBase64String(protectString,classOf[util.HashMap[String,AnyRef]])
      val mutableMap: mutable.Map[String,Object] = new mutable.HashMap[String,Object]
      //Add protected payload values to mutable map
      protectedMap.forEach((k,v) => mutableMap.put(k, v))

      mutableMap.asJava.put("x-hcx-sender_code", config.hcxRegistryCode)
      mutableMap.asJava.put("x-hcx-recipient_code", baseRecord.senderCode)
      mutableMap.asJava.put("x-hcx-request_id", baseRecord.requestId)
      mutableMap.asJava.put("x-hcx-status", "response.partial")

      val searchResponse = event.get("headers").asInstanceOf[util.Map[String, AnyRef]]
        .get("protocol").asInstanceOf[util.Map[String, AnyRef]].get("x-hcx-search_response").asInstanceOf[util.Map[String, AnyRef]]
      //As we got response back from recipient, update the status as close irrespective of dispatch status
      updateSearchRecord(workflowId, requestId,"CLOSE",searchResponse)

      //place the updated protected headers in the payload
      parsedPayload.put("protected",JSONUtil.encodeBase64String(mutableMap.asJava))

      val dispatchResult: DispatcherResult = dispatchRecipient(baseRecord.senderCode,action,parsedPayload)
      if (!dispatchResult.success) {
        // Try to resend the response back to sender with the retry job by adding the event to retry topic
        //Update the protocol headers in the event, with the updated values and write to retry topic
        event.get("headers").asInstanceOf[util.Map[String, AnyRef]].put("protocol",mutableMap.asJava)
        context.output(config.retryOutputTag,JSONUtil.serialize(event))
      } else {
        //Update Base record
        updateSearchRecord(workflowId, baseRecord.requestId,"PARTIAL",new util.HashMap[String,Object]())
      }

      Console.println("Writing recipient response to audit log")
      //Update the protocol headers in the event
      event.get("headers").asInstanceOf[util.Map[String, AnyRef]].put("protocol",mutableMap.asJava)
      audit(event,status = true,context,metrics)
      /**
       * 6. Check whether all the child record status were in CLOSE/FAIL, then mark it as last recipient response received
       * 7. If it is the last recipient response, do the following
       * 8. Fetch all the child record details based on the workflowId
       * 9. Update the consolidated search response from all the recipients
       * 10. Dispatch request to the recipient(original sender) by encoding the updated payload
       *          a. Successful Dispatch, update base record with status as CLOSE
       *             b. In case of dispatch failure to the recipient, update base record status as RETRY
       */
      val searchList: util.List[CompositeSearch]  = fetchAllSearchRecords(workflowId)
      Console.println("Child records size:"+searchList.size())
      var hasNoPendingRecords:Boolean = false
      breakable {
        for (searchRecord <- searchList.asScala) {
           if("OPEN".equals(searchRecord.responseStatus) || "RETRY".equals(searchRecord.responseStatus)) {
             hasNoPendingRecords = true
             break
           }
      } }
      if(!hasNoPendingRecords && searchList.size() > 0){
        Console.println("Processing consolidated response")
        val consolidatedResponse = createConsolidatedResponse(baseRecord,searchList)
        val gson = new Gson
        mutableMap.asJava.put("x-hcx-search_response", gson.toJson(consolidatedResponse))
        mutableMap.asJava.put("x-hcx-status", "response.complete")
        parsedPayload.put("protected",JSONUtil.encodeBase64String(mutableMap.asJava))
        parsedPayload.put("ciphertext", getEmptyCipherText)

        val dispatcherResult = dispatchRecipient(baseRecord.senderCode, action, parsedPayload)

        //If we are unable to dispatch the consolidated response, then update the base record with RETRY status
        if (dispatcherResult.success)
          updateSearchRecord(baseRecord.workFlowId, baseRecord.requestId, "CLOSE", JSONUtil.deserialize[util.Map[String,AnyRef]](gson.toJson(consolidatedResponse.summary)))
        else
          updateSearchRecord(baseRecord.workFlowId, baseRecord.requestId, "RETRY", JSONUtil.deserialize[util.Map[String,AnyRef]](gson.toJson(consolidatedResponse.summary)))

        Console.println("Writing consolidated response to audit log")
        //Update the protocol headers in the event
        event.get("headers").asInstanceOf[util.Map[String, AnyRef]].put("protocol",mutableMap.asJava)
        audit(event,status = true,context,metrics)
      }else{
        Console.println("Pending records, waiting for responses from recipients")
      }

    }else{
      //TODO Got response after the expiry time, do nothing for now
    }
  }

  private def getFinalSearchResponse(entityResultMap: util.Map[String, Int]): SearchResponse = {
    var entityTotalCounts: Int = 0
    for (key <- entityResultMap.asScala.keySet) {
      entityTotalCounts += entityResultMap.get(key)
    }
    SearchResponse(entityTotalCounts,entityResultMap)
  }



  @throws[Exception]
  private def createConsolidatedResponse(baseRecord: CompositeSearch, searchList: util.List[CompositeSearch]): ConsolidatedResponse = {
    var resp_index = 0
    val entityResultMap = new util.HashMap[String, Int]
    for (searchRecord <- searchList.asScala) {
      if (!"CLOSE".equals(searchRecord.responseStatus)) resp_index += 1
      val searchResponse = searchRecord.responseData
      updateEntityCounts(entityResultMap, searchResponse)
    }
    val responseSummary = getFinalSearchResponse(entityResultMap)
    val consolidatedResponse = ConsolidatedResponse(searchList.size,resp_index,baseRecord.senderCode,responseSummary)
    consolidatedResponse
  }

  private def updateEntityCounts(entityResultMap: util.Map[String, Int], entityCountsMap: util.Map[String,AnyRef]): Unit = {
    val entityMap = entityCountsMap.getOrDefault("entity_counts",new util.HashMap[String,AnyRef]).asInstanceOf[util.HashMap[String,AnyRef]]
    var entityCount: Int = 0
    for (key <- entityMap.asScala.keySet) {
      entityCount = entityMap.get(key).asInstanceOf[Int]
     if (entityResultMap.containsKey(key)) {
        var oldCounts = entityResultMap.get(key)
        oldCounts += entityCount
        entityResultMap.put(key, oldCounts)
      }
      else
      entityResultMap.put(key, entityCount)
   }
  }

  @throws[Exception]
  def updateSearchRecord(workFlowId: String, requestId: String, status: String, searchResponse: util.Map[String, AnyRef]): Unit = {
    val query = String.format("UPDATE %s SET response_status ='%s',response_data=?::JSON WHERE workflow_id='%s' AND request_id='%s'", config.searchTable, status, workFlowId, requestId)
    val preStatement = postgresConnect.connection.prepareStatement(query)
    try {
      postgresConnect.connection.setAutoCommit(false)
      preStatement.setObject(1,  JSONUtil.serialize(searchResponse))
      preStatement.executeUpdate
      System.out.println("Update operation completed successfully for workFlowId"+workFlowId +" and requestId:"+requestId)
      postgresConnect.connection.commit()
    } catch {
      case e: Exception =>
        throw e
    } finally if (preStatement != null) preStatement.close()
  }

  @throws[Exception]
  def getBaseRecord(workflowId: String): CompositeSearch = {
    System.out.println("Fetching base record from postgres for workflowId: " + workflowId)
    val postgresQuery = String.format("SELECT request_id,sender_code,response_status FROM %s WHERE recipient_code IS NULL and workflow_id = '%s'", config.searchTable, workflowId)
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {
      val resultSet = preparedStatement.executeQuery
      if (resultSet.next) {
        CompositeSearch(workflowId,resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),null)
      }
      else throw new Exception
    } catch {
      case e: Exception =>
        throw e
    } finally if (preparedStatement != null) preparedStatement.close()
  }


  @throws[Exception]
  def fetchAllSearchRecords(workflowId: String): util.List[CompositeSearch] = {
    System.out.println("Fetching all records from postgres for workflowId: " + workflowId)
    val postgresQuery = String.format("SELECT request_id,sender_code,response_status,response_data FROM %s WHERE recipient_code IS NOT NULL AND workflow_id = '%s'", config.searchTable, workflowId)
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {
      val resultSet = preparedStatement.executeQuery
      val searchList = new util.ArrayList[CompositeSearch]
      while (resultSet.next) {
       val compositeSearch = CompositeSearch(workFlowId = workflowId,resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),JSONUtil.deserialize[util.Map[String, AnyRef]](resultSet.getString(4)))
        searchList.add(compositeSearch)
      }
      searchList
    } catch {
      case e: Exception =>
        throw e
    } finally if (preparedStatement != null) preparedStatement.close()
  }
}

case class CompositeSearch(workFlowId:String,requestId:String,senderCode:String,responseStatus:String,responseData:util.Map[String,AnyRef])

case class ConsolidatedResponse(total_responses:Int,response_index:Int,sender_code:String,summary:SearchResponse)

case class SearchResponse(count: Int,entity_counts: util.Map[String, Int])










