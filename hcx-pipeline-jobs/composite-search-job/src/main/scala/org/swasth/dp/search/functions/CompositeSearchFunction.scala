package org.swasth.dp.search.functions

import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.slf4j.LoggerFactory
import org.swasth.dp.core.function.{BaseDispatcherFunction, ValidationResult}
import org.swasth.dp.core.job.Metrics
import org.swasth.dp.core.util.{Constants, JSONUtil, PostgresConnect, PostgresConnectionConfig}
import org.swasth.dp.search.task.SearchConfig

import java.sql.Timestamp
import java.util
import java.util.{Calendar, UUID}
import scala.collection.JavaConverters._
import scala.collection.mutable

class CompositeSearchFunction(config: SearchConfig)(implicit val stringTypeInfo: TypeInformation[String])
  extends BaseDispatcherFunction(config) {

  private[this] val logger = LoggerFactory.getLogger(classOf[CompositeSearchFunction])

  override def validate(event: util.Map[String, AnyRef]): ValidationResult = {
    // TODO: Add domain specific validations
    ValidationResult(status = true, None)
  }

  override def processElement(event: util.Map[String, AnyRef], context: ProcessFunction[util.Map[String, AnyRef], util.Map[String, AnyRef]]#Context, metrics: Metrics): Unit = {
    /**
     * TODO below logic.
     *  1. Modify the sender(HCX) and recipient(recipient id from search filters) in the payload
     *     2. Modify the x-hcx-api_call_id with the new generated api_call id
     *     3. Modify the search filters to have only one recipient while dispatching the apicall
     *     4. Dispatch api_call to the recipient by encoding the updated payload
     *          a. Successful Dispatch, Insert child record with status as Open
     *             b. In case of dispatch failure to the recipient, Insert child record with status as Fail
     */
    //Pay load from database
    val payloadRefId = event.get(Constants.MID).asInstanceOf[String]
    val payloadMap = getPayload(payloadRefId)
    val encodedPayload = payloadMap.get(Constants.PAYLOAD).asInstanceOf[String]
    //Place the updated protected header values into this map and encode the values
    val parsedPayload = JSONUtil.parsePayload(encodedPayload)
    val protectString = parsedPayload.get(Constants.PROTECTED).asInstanceOf[String]
    //Decode protected String
    val protectedMap = JSONUtil.decodeBase64String(protectString,classOf[util.HashMap[String, AnyRef]])
    //Creating mutable map to update the protected headers
    val mutableMap: mutable.Map[String, AnyRef] = new mutable.HashMap[String, AnyRef]
    //Add protected map values to mutable map
    protectedMap.forEach((k, v) => mutableMap.put(k, v))

    val protectedHeaders = event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].get(Constants.PROTOCOL).asInstanceOf[util.Map[String, AnyRef]]
    val recipientList = protectedHeaders.get(Constants.SEARCH_REQUEST).asInstanceOf[util.Map[String, AnyRef]].
      get(Constants.SEARCH_FILTERS).asInstanceOf[util.Map[String, AnyRef]].
      get(Constants.SEARCH_FILTERS_RECEIVER).asInstanceOf[util.List[String]]

    val correlationId = getProtocolStringValue(event,Constants.CORRELATION_ID)
    val originalApiCallId = getProtocolStringValue(event,Constants.API_CALL_ID)
    val senderCode = getProtocolStringValue(event,Constants.SENDER_CODE)
    val action = event.get(Constants.ACTION).asInstanceOf[String]
    //Insert base record
    insertSearchRecord(correlationId, originalApiCallId, senderCode, null, Constants.OPEN_STATUS, "{}")
    mutableMap.asJava.put(Constants.SENDER_CODE, config.hcxRegistryCode)
    //Iterate through all the recipients and dispatch apicalls with updated payload
    for (recipientCode <- recipientList.asScala) {
      mutableMap.asJava.put(Constants.RECIPIENT_CODE, recipientCode)
      val apiCallId = UUID.randomUUID().toString
      mutableMap.asJava.put(Constants.API_CALL_ID, apiCallId)
      val filterList = new util.ArrayList[String]
      filterList.add(recipientCode)
      mutableMap.asJava.get(Constants.SEARCH_REQUEST).asInstanceOf[util.Map[String, AnyRef]].
        get(Constants.SEARCH_FILTERS).asInstanceOf[util.Map[String, AnyRef]].put(Constants.SEARCH_FILTERS_RECEIVER, filterList)
      //Update the protected headers for each apicall being sent to recipient
      event.get(Constants.HEADERS).asInstanceOf[util.Map[String, AnyRef]].put(Constants.PROTOCOL, mutableMap.asJava)
      //place the updated protected headers in the parsed payload map
      parsedPayload.put(Constants.PROTECTED, JSONUtil.encodeBase64Object(mutableMap.asJava))
      val dispatchResult = dispatchRecipient(recipientCode, action, parsedPayload)
      if (!dispatchResult.success) {
        insertSearchRecord(correlationId, apiCallId, config.hcxRegistryCode, recipientCode, Constants.RETRY_STATUS, "{}")
        //Update the protocol headers with the updated values
        context.output(config.retryOutputTag, JSONUtil.serialize(event))
      } else
        insertSearchRecord(correlationId, apiCallId, config.hcxRegistryCode, recipientCode, Constants.OPEN_STATUS, "{}")
      //Audit the each child record after dispatching the api_call with the updated protected headers
      Console.println("Writing audit log for child record with apiCallId:" + apiCallId)
      event.put(Constants.UPDATED_TIME, Calendar.getInstance().getTime())
      audit(event, context, metrics)
    }
    Console.println("Writing into audit log for base record with correlationId:" + correlationId)
    //Audit the incoming event
    event.put(Constants.UPDATED_TIME, Calendar.getInstance().getTime())
    audit(event, context, metrics)
  }


  @throws[Exception]
  def insertSearchRecord(correlationId: String, apiCallId: String, senderCode: String, recipientCode: String, status: String, searchResponse: String): Unit = {
    val query: String = String.format("INSERT INTO  %s (correlation_id,apicall_id,sender_code,recipient_code,response_status,response_data,apicall_time) " + "VALUES (?,?,?,?,?,?::JSON,?) ON CONFLICT ON CONSTRAINT composite_search_pkey DO NOTHING", config.searchTable)
    val preStatement = postgresConnect.getConnection.prepareStatement(query)
    try {
      postgresConnect.connection.setAutoCommit(false)
      preStatement.setString(1, correlationId)
      preStatement.setString(2, apiCallId)
      preStatement.setString(3, senderCode)
      if (StringUtils.isNotEmpty(recipientCode))
        preStatement.setString(4, recipientCode)
      else
        preStatement.setNull(4, java.sql.Types.VARCHAR)
      preStatement.setString(5, status)
      preStatement.setObject(6, searchResponse)
      val apicallTime: Timestamp = new Timestamp(System.currentTimeMillis)
      preStatement.setTimestamp(7, apicallTime)
      preStatement.executeUpdate
      System.out.println("Insert completed successfully for correlationId:" + correlationId + " and apiCallId:" + apiCallId)
      postgresConnect.connection.commit()
    } catch {
      case e: Exception =>
        throw e
    } finally {
      if (preStatement != null) {
        preStatement.close()
      }
    }
  }
}






