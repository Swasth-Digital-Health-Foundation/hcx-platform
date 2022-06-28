package org.swasth.apigateway.constants;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class Constants {

    // Entity types
    public static final String COMMUNICATION = "communication";
    public static final String NOTIFICATION = "notification";
    public static final String PAYMENT = "paymentnotice";

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
    public static final String REDIRECT_TO = "x-hcx-redirect_to";
    public static final List<String> DEBUG_FLAG_VALUES = Arrays.asList("Error","Info","Debug");
    public static final String STATUS = "x-hcx-status";
    public static final List<String> REQUEST_STATUS_VALUES = Arrays.asList("request.queued", "request.dispatched");
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final List<String> ERROR_DETAILS_VALUES = Arrays.asList("code","message","trace");
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String EID = "eid";
    public static final String AUDIT = "AUDIT";
    public static final String MID = "mid";
    public static final String ERROR_STATUS = "response.error";
    public static final String REQUESTED_TIME = "requestTimeStamp";
    public static final String UPDATED_TIME = "updatedTimestamp";
    public static final String AUDIT_TIMESTAMP = "auditTimeStamp";
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
    public static final String REGISTRY_STATUS = "status";
    public static final String ROLES = "roles";
    public static final String ACTION = "action";
    public static final String SENDER_ROLE = "senderRole";
    public static final String RECIPIENT_ROLE = "recipientRole";
    public static final String PROVIDER = "provider";
    public static final List<String> OPERATIONAL_ENTITIES = Arrays.asList(NOTIFICATION, COMMUNICATION, REGISTRY_STATUS);
    public static final String REDIRECT_STATUS = "response.redirect";
    public static final String COMPLETE_STATUS = "response.complete";
    public static final List<String> EXCLUDE_ENTITIES = Arrays.asList(COMMUNICATION, PAYMENT, NOTIFICATION);
    public static final List<String> RESPONSE_STATUS_VALUES = Arrays.asList(COMPLETE_STATUS, "response.partial", ERROR_RESPONSE, REDIRECT_STATUS);
    public static final List<String> ALLOWED_ENTITIES_ERROR_AUDIT_CREATION = Arrays.asList("coverageeligibility", "preauth", "claim", PAYMENT, COMMUNICATION, "predetermination", REGISTRY_STATUS, "search");
    public static final String QUEUED_STATUS = "request.queued";
    public static final String DISPATCHED_STATUS = "request.dispatched";

    //Notification Details
    public static final String NOTIFICATION_ID = "x-hcx-notification_id";
    public static final String NOTIFICATION_DATA = "x-hcx-notification_data";

}
