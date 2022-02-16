package org.swasth.apigateway.constants;

import java.util.Arrays;
import java.util.List;

public interface Constants {

    String VERSION_PREFIX = "/v1";

    // Coverage Eligibility APIs
    String COVERAGE_ELIGIBILITY_CHECK = VERSION_PREFIX + "/coverageeligibility/check";
    String COVERAGE_ELIGIBILITY_ONCHECK = VERSION_PREFIX + "/coverageeligibility/on_check";

    // Claims APIs
    String PRE_AUTH_SUBMIT = VERSION_PREFIX + "/preauth/submit";
    String PRE_AUTH_ONSUBMIT = VERSION_PREFIX + "/preauth/on_submit";
    String CLAIM_SUBMIT = VERSION_PREFIX + "/claim/submit";
    String CLAIM_ONSUBMIT = VERSION_PREFIX + "/claim/on_submit";

    //Payment Notice APIs
    String PAYMENT_NOTICE_REQUEST = VERSION_PREFIX + "/paymentnotice/request";
    String PAYMENT_NOTICE_ONREQUEST = VERSION_PREFIX + "/paymentnotice/on_request";

    //Status Search APIs
    String HCX_STATUS = VERSION_PREFIX + "/hcx/status";
    String HCX_ONSTATUS = VERSION_PREFIX + "/hcx/on_status";

    // Search APIs
    String HCX_SEARCH = VERSION_PREFIX + "/hcx/search";
    String HCX_ON_SEARCH = VERSION_PREFIX + "/hcx/on_search";

    List<String> ON_ACTION_APIS = Arrays.asList(COVERAGE_ELIGIBILITY_ONCHECK, PRE_AUTH_ONSUBMIT, CLAIM_ONSUBMIT, PAYMENT_NOTICE_ONREQUEST, HCX_ONSTATUS, HCX_ON_SEARCH);

    int PAYLOAD_LENGTH = 5;
    String AUTH_REQUIRED = "AUTH_REQUIRED";
    String X_JWT_SUB_HEADER = "X-jwt-sub";
    String CORRELATION_ID = "x-hcx-correlation_id";
    String WORKFLOW_ID = "x-hcx-workflow_id";
    String API_CALL_ID = "x-hcx-api_call_id";
    String AUTHORIZATION = "Authorization";
    String PAYLOAD = "payload";
    String SENDER_CODE = "x-hcx-sender_code";
    String RECIPIENT_CODE = "x-hcx-recipient_code";
    String TIMESTAMP = "x-hcx-timestamp";
    String DEBUG_FLAG = "x-hcx-debug_flag";
    List<String> DEBUG_FLAG_VALUES = Arrays.asList("Error","Info","Debug");
    String STATUS = "x-hcx-status";
    List<String> STATUS_VALUES = Arrays.asList("request.queued", "request.dispatched", "response.complete", "response.partial", "response.error", "response.redirect");
    String ERROR_DETAILS = "x-hcx-error_details";
    List<String> ERROR_DETAILS_VALUES = Arrays.asList("code","message","trace");
    String DEBUG_DETAILS = "x-hcx-debug_details";
    String BLOCKED = "Blocked";
    String INACTIVE = "Inactive";
    String OS_OWNER = "osOwner";

}
