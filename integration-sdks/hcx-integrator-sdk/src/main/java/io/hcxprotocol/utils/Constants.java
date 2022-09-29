package io.hcxprotocol.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String HCX_SENDER_CODE = "x-hcx-sender_code";
    public static final String HCX_RECIPIENT_CODE = "x-hcx-recipient_code";
    public static final String HCX_API_CALL_ID = "x-hcx-api_call_id";
    public static final String HCX_CORRELATION_ID = "x-hcx-correlation_id";
    public static final String WORKFLOW_ID = "x-hcx-workflow_id";
    public static final String HCX_TIMESTAMP = "x-hcx-timestamp";
    public static final String STATUS = "x-hcx-status";
    public static final String ALG = "alg";
    public static final String ENC = "enc";
    public static final String ERROR = "error";
    public static final String A256GCM = "A256GCM";
    public static final String RSA_OAEP = "RSA-OAEP";
    public static final String AUTHORIZATION = "Authorization";
    public static final String ENCRYPTION_CERT = "encryption_cert";
    public static final String PAYLOAD = "payload";
    public static final String FHIR_PAYLOAD = "fhirPayload";
    public static final String HEADERS = "headers";
    public static final String RESPONSE_OBJ = "responseObj";
    public static final String TIMESTAMP = "timestamp";
    public static final String API_CALL_ID = "api_call_id";
    public static final String CORRELATION_ID = "correlation_id";

    public static final String DEBUG_FLAG = "x-hcx-debug_flag";
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String REDIRECT_STATUS = "response.redirect";
    public static final String COMPLETE_STATUS = "response.complete";
    public static final String PARTIAL_STATUS = "response.partial";

    public static final int PROTOCOL_PAYLOAD_LENGTH = 5;
    public static final String REDIRECT_TO = "x-hcx-redirect_to";
    public static final List<String> DEBUG_FLAG_VALUES = Arrays.asList("Error","Info","Debug");
    public static final List<String> REQUEST_STATUS_VALUES = Arrays.asList("request.queued", "request.dispatched");
    public static final List<String> ERROR_DETAILS_VALUES = Arrays.asList("code","message","trace");
    public static final String ERROR_RESPONSE = "response.error";
    public static final List<String> RECIPIENT_ERROR_VALUES = Arrays.asList("ERR_INVALID_ENCRYPTION", "ERR_INVALID_PAYLOAD", "ERR_WRONG_DOMAIN_PAYLOAD", "ERR_INVALID_DOMAIN_PAYLOAD", "ERR_SENDER_NOT_SUPPORTED", "ERR_SERVICE_UNAVAILABLE", "ERR_DOMAIN_PROCESSING","ERR_MANDATORY_HEADER_MISSING",
            "ERR_INVALID_API_CALL_ID",
            "ERR_INVALID_CORRELATION_ID",
            "ERR_INVALID_TIMESTAMP",
            "ERR_INVALID_REDIRECT_TO",
            "ERR_INVALID_STATUS",
            "ERR_INVALID_DEBUG_FLAG",
            "ERR_INVALID_ERROR_DETAILS",
            "ERR_INVALID_DEBUG_DETAILS",
            "ERR_INVALID_WORKFLOW_ID");
    public static final List<String> RESPONSE_STATUS_VALUES = Arrays.asList(COMPLETE_STATUS, PARTIAL_STATUS, ERROR_RESPONSE, REDIRECT_STATUS);

    public static final String INVALID_PAYLOAD_LENGTH_ERR_MSG = "Mandatory elements of JWE token are missing.Should have all 5 elements";
    public static final String INVALID_PAYLOAD_VALUES_ERR_MSG = "Payload contains null or empty values";
    public static final String INVALID_API_CALL_ID_ERR_MSG = "API call id should be a valid UUID";
    public static final String INVALID_CORRELATION_ID_ERR_MSG = "Correlation id should be a valid UUID";
    public static final String INVALID_WORKFLOW_ID_ERR_MSG = "Workflow id should be a valid UUID";
    public static final String INVALID_TIMESTAMP_ERR_MSG = "Timestamp cannot be in future or cross more than {0} hours in past";
    public static final String INVALID_DEBUG_FLAG_ERR_MSG = "Debug flag cannot be null, empty and other than 'String'";
    public static final String INVALID_DEBUG_FLAG_RANGE_ERR_MSG = "Debug flag cannot be other than {0}";
    public static final String INVALID_ERROR_DETAILS_ERR_MSG = "Error details cannot be null, empty and other than 'JSON Object' with mandatory fields code or message";
    public static final String INVALID_ERROR_DETAILS_RANGE_ERR_MSG = "Error details should contain only: {0}";
    public static final String INVALID_ERROR_DETAILS_CODE_ERR_MSG ="Invalid Error Code";
    public static final String INVALID_DEBUG_DETAILS_ERR_MSG = "Debug details cannot be null, empty and other than 'JSON Object' with mandatory fields code or message";
    public static final String INVALID_DEBUG_DETAILS_RANGE_ERR_MSG = "Debug details should contain only: {0}";
    public static final String INVALID_STATUS_ERR_MSG = "Status cannot be null, empty and other than 'String'";
    public static final String INVALID_STATUS_ACTION_RANGE_ERR_MSG = "Status value for action API calls can be only: {0}";
    public static final String INVALID_STATUS_ON_ACTION_RANGE_ERR_MSG = "Status value for on_action API calls can be only: {0}";
    public static final int TIMESTAMP_RANGE = 10000;
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String INVALID_MANDATORY_ERR_MSG = "Mandatory headers are missing: {0}";
    public static final String INVALID_REDIRECT_ERR_MSG = "Redirect requests must have valid participant code for field {0}";
    public static final String INVALID_REDIRECT_SELF_ERR_MSG = "Sender can not redirect request to self";
    public static final String INVALID_JSON_REQUEST_BODY_ERR_MSG = "Request body should be a proper JWE object for action API calls";
}
