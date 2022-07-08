package org.swasth.dp.searchresponse.functions

import com.google.gson.Gson
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, DispatcherResult, ValidationResult}
import org.swasth.dp.core.job.Metrics
import org.swasth.dp.core.util.{Constants, JSONUtil, PostgresConnect, PostgresConnectionConfig}
import org.swasth.dp.searchresponse.task.SearchResponseConfig

import java.util
import java.util.Calendar
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

class SearchResponseFunction(config: SearchResponseConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[SearchResponseFunction])

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(status = true, None)
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    /**
     * TODO below logic.
     *  1. Fetch the baseRecord based on correlationId,
     *     if status is complete, do nothing
     *     else proceed with next step
     *     2. Modify the sender(HCX) and recipient(original sender from base record) in the payload
     *     3. Modify the x-hcx-api_call_id with the apiCallId from the base record
     *     4. Modify the x-hcx-status as response.partial for each api_call and response.complete for final record
     *     4. Dispatch api_call to the sender
     *     5.      Success: Update child record, with status as Close and response_data with the search response from the event
     *     Update base record with status as PARTIAL
     *     FAIL: Update the child record, with status as RETRY and response_data with the search response from the event
     */
    //Add request time stamp for audit log
    event.put(Constants.REQUESTED_TIME, Calendar.getInstance().getTime())
    val correlationId = getProtocolStringValue(event,Constants.CORRELATION_ID)
    val action = event.get(Constants.ACTION).asInstanceOf[String]
    val apiCallId = getProtocolStringValue(event,Constants.API_CALL_ID)
    val baseRecord = getBaseRecord(correlationId)
    val senderCode = getProtocolStringValue(event,Constants.HCX_SENDER_CODE) //1-93f908ba-b579-453e-8b2a-56022afad275
    if (!Constants.CLOSE_STATUS.equalsIgnoreCase(baseRecord.responseStatus)) {
      val payloadRefId = event.get(Constants.MID).asInstanceOf[String]
      val payloadMap = getPayload(payloadRefId)
      val encodedPayload = payloadMap.get(Constants.PAYLOAD).asInstanceOf[String]
      //Place the updated protected header values into this map and encode the values
      val parsedPayload = JSONUtil.parsePayload(encodedPayload)
      val protectString = parsedPayload.get(Constants.PROTECTED).asInstanceOf[String]
      //Decode protected String
      val protectedMap = JSONUtil.decodeBase64String(protectString,classOf[util.HashMap[String, AnyRef]])
      val mutableMap: mutable.Map[String, AnyRef] = new mutable.HashMap[String, AnyRef]
      //Add protected payload values to mutable map
      protectedMap.forEach((k, v) => mutableMap.put(k, v))

      mutableMap.asJava.put(Constants.HCX_SENDER_CODE, config.hcxRegistryCode)
      mutableMap.asJava.put(Constants.HCX_RECIPIENT_CODE, baseRecord.senderCode)
      mutableMap.asJava.put(Constants.API_CALL_ID, baseRecord.apiCallId)
      mutableMap.asJava.put(Constants.HCX_STATUS, Constants.PARTIAL_RESPONSE)

      val searchResponse = event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]]
        .get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]].get(Constants.SEARCH_RESPONSE).asInstanceOf[util.Map[String, AnyRef]]
      Console.println("Search Response:"+ JSONUtil.serialize(searchResponse))
      //As we got response back from recipient, update the status as close irrespective of dispatch status
      updateSearchRecord(correlationId, apiCallId, Constants.CLOSE_STATUS, searchResponse)

      //place the updated protected headers in the payload
      parsedPayload.put(Constants.PROTECTED, JSONUtil.encodeBase64Object(mutableMap.asJava))
      //Update the protocol headers in the event, with the updated values and write to retry topic
      event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].put(Constants.PROTOCOL, mutableMap.asJava)

      val dispatchResult: DispatcherResult = dispatchRecipient(baseRecord.senderCode, action, parsedPayload)
      if (!dispatchResult.success) {
        //context.output(config.retryOutputTag, JSONUtil.serialize(event))
      } else {
        //Update Base record
        updateSearchRecord(correlationId, baseRecord.apiCallId, Constants.PARTIAL_RESPONSE, new util.HashMap[String, Object]())
      }

      Console.println("Writing recipient response to audit log with correlationId:" + correlationId)
      event.put(Constants.UPDATED_TIME, Calendar.getInstance().getTime())
      audit(event, context, metrics)
      /**
       * 6. Check whether all the child record status were in CLOSE/FAIL, then mark it as last recipient response received
       * 7. If it is the last recipient response, do the following
       * 8. Fetch all the child record details based on the correlationId
       * 9. Update the consolidated search response from all the recipients
       * 10. Dispatch apicall to the recipient(original sender) by encoding the updated payload
       *          a. Successful Dispatch, update base record with status as CLOSE
       *             b. In case of dispatch failure to the recipient, update base record status as RETRY
       */
      val searchList: util.List[CompositeSearch] = fetchAllSearchRecords(correlationId)
      Console.println("Child records size:" + searchList.size())
      var hasNoPendingRecords: Boolean = false
      breakable {
        for (searchRecord <- searchList.asScala) {
          if (Constants.OPEN_STATUS.equalsIgnoreCase(searchRecord.responseStatus) || Constants.RETRY_STATUS.equalsIgnoreCase(searchRecord.responseStatus)) {
            hasNoPendingRecords = true
            break
          }
        }
      }
      if (!hasNoPendingRecords && searchList.size() > 0) {
        Console.println("Processing consolidated response for correlationId:"+correlationId)
        val consolidatedResponse = createConsolidatedResponse(baseRecord, searchList)
        val gson = new Gson
        mutableMap.asJava.put(Constants.SEARCH_RESPONSE, gson.toJson(consolidatedResponse))
        mutableMap.asJava.put(Constants.HCX_STATUS, Constants.COMPLETE_RESPONSE)
        parsedPayload.put(Constants.PROTECTED, JSONUtil.encodeBase64Object(mutableMap.asJava))
        parsedPayload.put(Constants.CIPHERTEXT, getEmptyCipherText)

        val dispatcherResult = dispatchRecipient(baseRecord.senderCode, action, parsedPayload)

        //If we are unable to dispatch the consolidated response, then update the base record with FAIL status and senders have to check by status API call
        if (dispatcherResult.success)
          updateSearchRecord(baseRecord.correlationId, baseRecord.apiCallId, Constants.CLOSE_STATUS, JSONUtil.deserialize[util.Map[String, AnyRef]](gson.toJson(consolidatedResponse.summary)))
        else {
          updateSearchRecord(baseRecord.correlationId, baseRecord.apiCallId, Constants.FAIL_STATUS, JSONUtil.deserialize[util.Map[String, AnyRef]](gson.toJson(consolidatedResponse.summary)))
        }

        Console.println("Writing consolidated response to audit log for correlationId:" + correlationId)
        //Update the protocol headers in the event
        event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].put(Constants.PROTOCOL, mutableMap.asJava)
        event.put(Constants.UPDATED_TIME, Calendar.getInstance().getTime())
        audit(event, context, metrics)
      } else {
        Console.println("Pending records, waiting for responses from recipients for correlationId:" + correlationId)
      }

    } else {
      //TODO Got response after the expiry time, do nothing for now
      Console.println("Got response after the expiry time, from recipient:" + senderCode + " for correlationId:" + correlationId)
    }
  }

  private def getFinalSearchResponse(entityResultMap: util.Map[String, Int]): SearchResponse = {
    var entityTotalCounts: Int = 0
    for (key <- entityResultMap.asScala.keySet) {
      entityTotalCounts += entityResultMap.get(key)
    }
    SearchResponse(entityTotalCounts, entityResultMap)
  }


  @throws[Exception]
  private def createConsolidatedResponse(baseRecord: CompositeSearch, searchList: util.List[CompositeSearch]): ConsolidatedResponse = {
    var resp_index = 0
    val entityResultMap = new util.HashMap[String, Int]
    for (searchRecord <- searchList.asScala) {
      if (!Constants.CLOSE_STATUS.equalsIgnoreCase(searchRecord.responseStatus)) resp_index += 1
      val searchResponse = searchRecord.responseData
      updateEntityCounts(entityResultMap, searchResponse)
    }
    val responseSummary = getFinalSearchResponse(entityResultMap)
    val consolidatedResponse = ConsolidatedResponse(searchList.size, resp_index, baseRecord.senderCode, responseSummary)
    consolidatedResponse
  }

  private def updateEntityCounts(entityResultMap: util.Map[String, Int], entityCountsMap: util.Map[String, AnyRef]): Unit = {
    val entityMap = entityCountsMap.getOrDefault(Constants.SEARCH_ENTITY_COUNT, new util.HashMap[String, AnyRef]).asInstanceOf[util.HashMap[String, AnyRef]]
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
  def updateSearchRecord(correlationId: String, apiCallId: String, status: String, searchResponse: util.Map[String, AnyRef]): Unit = {
    val query = String.format("UPDATE %s SET response_status ='%s',response_data=?::JSON WHERE correlation_id='%s' AND apicall_id='%s'", config.searchTable, status, correlationId, apiCallId)
    val preStatement = postgresConnect.connection.prepareStatement(query)
    try {
      postgresConnect.connection.setAutoCommit(false)
      preStatement.setObject(1, JSONUtil.serialize(searchResponse))
      preStatement.executeUpdate
      System.out.println("Update operation completed successfully for correlationId:" + correlationId + " and apiCallId:" + apiCallId)
      postgresConnect.connection.commit()
    } catch {
      case e: Exception =>
        throw e
    } finally if (preStatement != null) preStatement.close()
  }

  @throws[Exception]
  def getBaseRecord(correlationId: String): CompositeSearch = {
    System.out.println("Fetching base record from postgres for correlationId: " + correlationId)
    val postgresQuery = String.format("SELECT apicall_id,sender_code,response_status FROM %s WHERE recipient_code IS NULL and correlation_id = '%s'", config.searchTable, correlationId)
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {
      val resultSet = preparedStatement.executeQuery
      if (resultSet.next) {
        CompositeSearch(correlationId, resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), null)
      }
      else throw new Exception
    } catch {
      case e: Exception =>
        throw e
    } finally if (preparedStatement != null) preparedStatement.close()
  }


  @throws[Exception]
  def fetchAllSearchRecords(correlationId: String): util.List[CompositeSearch] = {
    System.out.println("Fetching all records from postgres for correlationId: " + correlationId)
    val postgresQuery = String.format("SELECT apicall_id,sender_code,response_status,response_data FROM %s WHERE recipient_code IS NOT NULL AND correlation_id = '%s'", config.searchTable, correlationId)
    val preparedStatement = postgresConnect.getConnection.prepareStatement(postgresQuery)
    try {
      val resultSet = preparedStatement.executeQuery
      val searchList = new util.ArrayList[CompositeSearch]
      while (resultSet.next) {
        val compositeSearch = CompositeSearch(correlationId, resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), JSONUtil.deserialize[util.Map[String, AnyRef]](resultSet.getString(4)))
        searchList.add(compositeSearch)
      }
      searchList
    } catch {
      case e: Exception =>
        throw e
    } finally if (preparedStatement != null) preparedStatement.close()
  }
}

case class CompositeSearch(correlationId: String, apiCallId: String, senderCode: String, responseStatus: String, responseData: util.Map[String, AnyRef])

case class ConsolidatedResponse(total_responses: Int, response_index: Int, sender_code: String, summary: SearchResponse)

case class SearchResponse(count: Int, entity_counts: util.Map[String, Int])










