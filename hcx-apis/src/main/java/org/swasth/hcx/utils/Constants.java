package org.swasth.hcx.utils;

import org.apache.kafka.common.protocol.types.Field;

import java.util.Arrays;
import java.util.List;

public  class Constants {

    public static final String VERSION_PREFIX = "/v1";
    public static final int PAYLOAD_LENGTH = 6;

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK = VERSION_PREFIX + "/coverageeligibility/check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK = VERSION_PREFIX + "/coverageeligibility/on_check";

    // Claims APIs
    public static final String PRE_AUTH_SUBMIT = VERSION_PREFIX + "/preauth/submit";
    public static final String PRE_AUTH_ONSUBMIT = VERSION_PREFIX + "/preauth/on_submit";
    public static final String PRE_AUTH_SEARCH = VERSION_PREFIX + "/preauth/search";
    public static final String PRE_AUTH_ONSEARCH = VERSION_PREFIX + "/preauth/on_search";
    public static final String CLAIM_SUBMIT = VERSION_PREFIX + "/claim/submit";
    public static final String CLAIM_ONSUBMIT = VERSION_PREFIX + "/claim/on_submit";
    public static final String CLAIM_SEARCH = VERSION_PREFIX + "/claim/search";
    public static final String CLAIM_ONSEARCH = VERSION_PREFIX + "/claim/on_search";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST = VERSION_PREFIX + "/paymentnotice/request";
    public static final String PAYMENT_NOTICE_ONREQUEST = VERSION_PREFIX + "/paymentnotice/on_request";
    public static final String PAYMENT_NOTICE_SEARCH = VERSION_PREFIX + "/paymentnotice/search";
    public static final String PAYMENT_NOTICE_ONSEARCH = VERSION_PREFIX + "/paymentnotice/on_search";

    // Search APIs
    public static final String HCX_SEARCH = VERSION_PREFIX + "/hcx/search";
    public static final String HCX_ON_SEARCH = VERSION_PREFIX + "/hcx/on_search";

    public static final String HEALTHY = "healthy";
    public static final String NAME = "name";
    public static final String KAFKA = "kafka";
    public static final String POSTGRESQL = "postgreSQL";
    public static final String CHECKS = "checks";

    //event generator props
    public static final String MID = "mid";
    public static final String PAYLOAD = "payload";
    public static final String ETS = "ets";
    public static final String ACTION = "action";
    public static final String HEADERS = "headers";
    public static final String JOSE="jose";
    public static final String PROTOCOL = "protocol";
    public static final String DOMAIN = "domain";
    public static final String LOG_DETAILS = "log_details";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String TRACE = "trace";
    public static final String SUBMITTED = "submitted";

    //Request props
    public static final String PAYLOAD_MANDATORY_PROPERTIES = "payload.mandatory.properties";
    public static final String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    public static final String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    public static final String JOSE_HEADERS = "headers.jose";
    public static final String DOMAIN_HEADERS = "headers.domain";
    public static final String PROTECTED = "protected";
    public static final String SENDER_CODE = "x-hcx-sender_code";
    public static final String RECIPIENT_CODE = "x-hcx-recipient_code";
    public static final String REQUEST_ID = "x-hcx-request_id";
    public static final String CORRELATION_ID = "x-hcx-correlation_id";
    public static final String CASE_ID = "x-hcx-case_id";
    public static final String WORKFLOW_ID = "x-hcx-workflow_id";
    public static final String TIMESTAMP = "x-hcx-timestamp";
    public static final String DEBUG_FLAG = "x-hcx-debug_flag";
    public static final List<String> DEBUG_FLAG_VALUES = Arrays.asList("Error","Info","Debug");
    public static final String STATUS = "x-hcx-status";
    public static final List<String> STATUS_VALUES = Arrays.asList("request.initiate", "request.retry",
        "response.success", "response.failure", "forward.request.initiate", "forward.response.success",
        "forward.response.fail", "response.redirect", "request.redirect", "response.partial");
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final List<String> ERROR_DETAILS_VALUES = Arrays.asList("error.code","error.message","trace");
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String SERVICE_MODE = "service.mode";
    public static final String GATEWAY = "gateway";
    public static final String KAFKA_TOPIC_PAYLOAD = "kafka.topic.payload";
    public static final String SEARCH_REQ ="x-hcx-search";
    public static final String SEARCH_RESP = "x-hcx-search_response";
    public static final List<String> SEARCH_REQ_KEYS = Arrays.asList("filters","time_period");
    public static final String SEARCH_FILTERS = "filters";
    public static final List<String>  SEARCH_FILTER_KEYS = Arrays.asList("senders","receivers","entity_types","workflow_ids","case_ids","entity_status");
    public static final String SEARCH_FILTERS_RECEIVER = "receivers";
    public static final List<String> SEARCH_RES_KEYS = Arrays.asList("count","entity_counts");

    public static final String HCX_REGISTRY_CODE = "hcx-registry-code";

    public static final String ENCRYPTED_KEY = "encrypted_key";
    public static final String AAD = "aad";
    public static final String IV = "iv";
    public static final String CIPHERTEXT = "ciphertext";
    public static final String TAG = "tag";

    public static final String HEADER_AUDIT = "headeraudit";
    public static final String PAYOR = "payor";
    public static final String PROVIDER = "provider";
    public static final String ROLES = "roles";
    public static final String SCHEME_CODE = "scheme_code";

}
