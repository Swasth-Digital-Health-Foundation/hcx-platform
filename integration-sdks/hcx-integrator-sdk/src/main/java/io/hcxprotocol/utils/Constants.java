package io.hcxprotocol.utils;

import java.util.Arrays;
import java.util.List;

/**
 * All the constant variables used in HCX Integrator SDK.
 */
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
            "ERR_INVALID_API_CALL_ID", "ERR_INVALID_CORRELATION_ID", "ERR_INVALID_TIMESTAMP", "ERR_INVALID_REDIRECT_TO", "ERR_INVALID_STATUS", "ERR_INVALID_DEBUG_FLAG", "ERR_INVALID_ERROR_DETAILS", "ERR_INVALID_DEBUG_DETAILS", "ERR_INVALID_WORKFLOW_ID");
    public static final List<String> RESPONSE_STATUS_VALUES = Arrays.asList(COMPLETE_STATUS, PARTIAL_STATUS, ERROR_RESPONSE, REDIRECT_STATUS);
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String PARTICIPANTS = "participants";

}
