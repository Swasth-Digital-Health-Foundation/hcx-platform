package org.swasth.dp.core.util

object Constants {

  val CDATA = "cdata"
  val SENDER = "sender"
  val RECIPIENT = "recipient"
  val PROTECTED = "protected"
  val ENCRYPTED_KEY = "encrypted_key"
  val IV = "iv"
  val CIPHERTEXT = "ciphertext"
  val TAG = "tag"
  val PAYLOAD_LENGTH = 5
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
  val SENDER_ROLE = "senderRole"
  val RECIPIENT_ROLE = "recipientRole"
  val ROLES = "roles"
  val RETRY_INDEX= "retryIndex"

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
  val HCX_ERROR_STATUS = "response.error"
  val HCX_DISPATCH_STATUS = "request.dispatched"
  val HCX_QUEUED_STATUS = "request.queued"

  //Search Fields
  val SEARCH_REQUEST = "x-hcx-search"
  val SEARCH_RESPONSE = "x-hcx-search_response"
  val SEARCH_FILTERS = "filters"
  val SEARCH_FILTERS_RECEIVER = "receivers"
  val SEARCH_COUNT = "count"
  val SEARCH_ENTITY_COUNT = "entity_counts"
  val PARTIAL_RESPONSE = "response.partial"
  val COMPLETE_RESPONSE = "response.complete"


  val OPEN_STATUS = "OPEN"
  val RETRY_STATUS = "RETRY"
  val CLOSE_STATUS = "CLOSE"
  val PARTIAL_STATUS = "PARTIAL"
  val FAIL_STATUS = "FAIL"
  val DISPATCH_STATUS = "request.dispatch"
  val ERROR_STATUS = "response.error"

  val RECIPIENT_ERROR_CODE = "ERR_RECIPIENT_NOT_AVAILABLE"
  val RECIPIENT_ERROR_MESSAGE = "Please provide correct recipient code"
  val RECIPIENT_ERROR_LOG = "Recipient endpoint url is empty"

  // JWT token properties
  val JTI = "jti"
  val ISS = "iss"
  val SUB = "sub"
  val IAT = "iat"
  val EXP = "exp"

}
