package org.swasth.apigateway.constants;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final int PAYLOAD_LENGTH = 5;
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String X_JWT_SUB_HEADER = "X-jwt-sub";
    public static final String CORRELATION_ID = "x-hcx-correlation_id";
    public static final String WORKFLOW_ID = "x-hcx-workflow_id";
    public static final String API_CALL_ID = "x-hcx-api_call_id";
    public static final String AUTHORIZATION = "Authorization";
    public static final String PAYLOAD = "payload";
    public static final String SENDER_CODE = "x-hcx-sender_code";
    public static final String RECIPIENT_CODE = "x-hcx-recipient_code";
    public static final String TIMESTAMP = "x-hcx-timestamp";
    public static final String DEBUG_FLAG = "x-hcx-debug_flag";
    public static final List<String> DEBUG_FLAG_VALUES = Arrays.asList("Error","Info","Debug");
    public static final String STATUS = "x-hcx-status";
    public static final List<String> STATUS_VALUES = Arrays.asList("request.queued", "request.dispatched", "response.complete", "response.partial", "response.error", "response.redirect");
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final List<String> ERROR_DETAILS_VALUES = Arrays.asList("code","message","trace");
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String BLOCKED = "Blocked";
    public static final String INACTIVE = "Inactive";
    public static final String OS_OWNER = "osOwner";
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


}
