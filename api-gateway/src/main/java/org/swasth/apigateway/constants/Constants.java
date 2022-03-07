package org.swasth.apigateway.constants;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String VERSION_PREFIX = "/v1";

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK = VERSION_PREFIX + "/coverageeligibility/check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK = VERSION_PREFIX + "/coverageeligibility/on_check";

    // Claims APIs
    public static final String PRE_AUTH_SUBMIT = VERSION_PREFIX + "/preauth/submit";
    public static final String PRE_AUTH_ONSUBMIT = VERSION_PREFIX + "/preauth/on_submit";
    public static final String CLAIM_SUBMIT = VERSION_PREFIX + "/claim/submit";
    public static final String CLAIM_ONSUBMIT = VERSION_PREFIX + "/claim/on_submit";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST = VERSION_PREFIX + "/paymentnotice/request";
    public static final String PAYMENT_NOTICE_ONREQUEST = VERSION_PREFIX + "/paymentnotice/on_request";

    //Status Search APIs
    public static final String HCX_STATUS = VERSION_PREFIX + "/hcx/status";
    public static final String HCX_ONSTATUS = VERSION_PREFIX + "/hcx/on_status";

    // Search APIs
    public static final String HCX_SEARCH = VERSION_PREFIX + "/hcx/search";
    public static final String HCX_ON_SEARCH = VERSION_PREFIX + "/hcx/on_search";

    //Communication APIs
    public static final String COMMUNICATION_REQUEST = VERSION_PREFIX + "/communication/request";
    public static final String COMMUNICATION_ONREQUEST = VERSION_PREFIX + "/communication/on_request";

    //Predetermination APIs
    public static final String PREDETERMINATION_SUBMIT = VERSION_PREFIX + "/predetermination/submit";
    public static final String PREDETERMINATION_ONSUBMIT = VERSION_PREFIX + "/predetermination/on_submit";

    public static final List<String> ACTION_APIS = Arrays.asList(COVERAGE_ELIGIBILITY_CHECK, PRE_AUTH_SUBMIT, CLAIM_SUBMIT, PAYMENT_NOTICE_REQUEST, HCX_STATUS, HCX_SEARCH, COMMUNICATION_REQUEST, PREDETERMINATION_SUBMIT);
    public static final List<String> ON_ACTION_APIS = Arrays.asList(COVERAGE_ELIGIBILITY_ONCHECK, PRE_AUTH_ONSUBMIT, CLAIM_ONSUBMIT, PAYMENT_NOTICE_ONREQUEST, HCX_ONSTATUS, HCX_ON_SEARCH, COMMUNICATION_ONREQUEST, PREDETERMINATION_ONSUBMIT);

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
    public static final String ROLES = "roles";
    public static final String ACTION = "action";
    public static final String SENDER_ROLE = "senderRole";
    public static final String RECIPIENT_ROLE = "recipientRole";
    public static final String PROVIDER = "provider";
    public static final List<String> OPERATIONAL_ENTITIES = Arrays.asList("notification", "communication", "status");
    public static final String REDIRECT_STATUS = "response.redirect";
    public static final String COMPLETE_STATUS = "response.complete";
    public static final List<String> EXCLUDE_ENTITIES = Arrays.asList("communication", "payment","notification");

}
