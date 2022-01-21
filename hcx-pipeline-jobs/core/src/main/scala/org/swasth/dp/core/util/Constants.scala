package org.swasth.dp.core.util

object Constants {

  val CDATA = "cdata"
  val SENDER = "sender"
  val RECIPIENT = "recipient"
  val PROTECTED = "protected"
  val PAYLOAD_LENGTH = 6
  val MID = "mid"
  val PAYLOAD = "payload"
  val ETS = "ets"
  val ACTION = "action"
  val HEADERS = "headers"
  val JOSE = "jose"
  val PROTOCOL = "protocol"
  val DOMAIN = "domain"
  val LOG_DETAILS = "log_details"
  val ERROR_DETAILS = "x-hcx-error_details"
  val DEBUG_DETAILS = "x-hcx-debug_details"
  val CODE = "code"
  val MESSAGE = "message"
  val TRACE = "trace"
  val SUBMITTED = "submitted"
  val AUDIT_TIMESTAMP = "auditTimeStamp"
  val AUDIT_ID = "eid"
  val END_POINT = "endpoint_url"
  val STATUS = "status"
  val REQUESTED_TIME = "requestTimeStamp"
  val UPDATED_TIME = "updatedTimestamp"

  //Event Fields
  val SENDER_CODE = "x-hcx-sender_code"
  val RECIPIENT_CODE = "x-hcx-recipient_code"
  val API_CALL_ID = "x-hcx-api_call_id"
  val CORRELATION_ID = "x-hcx-correlation_id"
  //val CASE_ID = "x-hcx-case_id"
  val WORKFLOW_ID = "x-hcx-workflow_id"
  val TIMESTAMP = "x-hcx-timestamp"
  val DEBUG_FLAG = "x-hcx-debug_flag"
  val HCX_STATUS = "x-hcx-status"

  //Search Fields
  val SEARCH_REQUEST: String = "x-hcx-search"
  val SEARCH_RESPONSE: String = "x-hcx-search_response"
  val SEARCH_FILTERS = "filters"
  val SEARCH_FILTERS_RECEIVER: String = "receivers"
  val SEARCH_COUNT: String = "count"
  val SEARCH_ENTITY_COUNT: String = "entity_counts"
  val PARTIAL_RESPONSE: String = "response.partial"
  val COMPLETE_RESPONSE: String = "response.complete"
  val CIPHERTEXT: String = "ciphertext"

  val OPEN_STATUS = "OPEN"
  val RETRY_STATUS = "RETRY"
  val CLOSE_STATUS = "CLOSE"
  val PARTIAL_STATUS = "PARTIAL"
  val FAIL_STATUS = "FAIL"


}
