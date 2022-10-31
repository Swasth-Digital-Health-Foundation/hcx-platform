package org.swasth.dp.core.service

import org.slf4j.LoggerFactory
import org.swasth.dp.core.job.BaseJobConfig
import org.swasth.dp.core.util.{Constants, ElasticSearchUtil, JSONUtil}

import java.util
import java.util.{Calendar, Date, TimeZone}

class AuditService(config: BaseJobConfig) {

  private[this] val logger = LoggerFactory.getLogger(classOf[AuditService])
  val esUtil = new ElasticSearchUtil(config.esUrl, config.auditIndex, config.batchSize)

  def indexAudit(auditEvent: util.Map[String, AnyRef]): Unit ={
    try {
      val settings = "{ \"index\": { } }"
      val mappings = "{ \"properties\": { \"eid\": { \"type\": \"text\" }, \"x-hcx-sender_code\": { \"type\": \"keyword\" }, \"x-hcx-recipient_code\": { \"type\": \"keyword\" }, \"x-hcx-api_call_id\": { \"type\": \"keyword\" }, \"x-hcx-correlation_id\": { \"type\": \"keyword\" }, \"x-hcx-workflow_id\": { \"type\": \"keyword\" }, \"x-hcx-timestamp\": { \"type\": \"keyword\" }, \"mid\": { \"type\": \"keyword\" }, \"action\": { \"type\": \"keyword\" }, \"x-hcx-status\": { \"type\": \"keyword\" }, \"auditTimeStamp\": { \"type\": \"keyword\" }, \"requestTimeStamp\": { \"type\": \"keyword\" }, \"updatedTimestamp\": { \"type\": \"keyword\" }, \"x-hcx-error_details\": { \"type\": \"object\" }, \"x-hcx-debug_details\": { \"type\": \"object\" }, \"senderRole\": { \"type\": \"keyword\" }, \"recipientRole\": { \"type\": \"keyword\" }, \"payload\": { \"type\": \"text\" }, \"topic_code\":{\"type\":\"keyword\"}, \"sender_code\":{\"type\":\"keyword\"}, \"recipient_code\":{\"type\":\"keyword\"}, \"notification_request_id\":{\"type\":\"keyword\"}, \"status\":{\"type\":\"keyword\"}, \"subscription_id\":{\"type\":\"keyword\"} } }"
      val cal = Calendar.getInstance(TimeZone.getTimeZone(config.timeZone))
      cal.setTime(auditEvent.get(Constants.ETS).asInstanceOf[Date])
      val indexName = config.auditIndex + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.WEEK_OF_YEAR)
      val mid = auditEvent.get(Constants.MID).asInstanceOf[String]
      esUtil.addIndex(settings, mappings, indexName, config.auditAlias)
      esUtil.addDocumentWithIndex(JSONUtil.serialize(auditEvent), indexName, mid)
      Console.println("Audit document created for mid: " + mid)
    } catch {
      case e: Exception =>
        logger.error("Error while processing event :: " + auditEvent + " :: " + e.getMessage)
        throw e
    }
  }

}
