package org.swasth.common.utils;

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

    String HEALTHY = "healthy";
    String NAME = "name";
    String KAFKA = "kafka";
    String POSTGRESQL = "postgreSQL";
    String CHECKS = "checks";

    //event generator props
    String MID = "mid";
    String PAYLOAD = "payload";
    String ETS = "ets";
    String ACTION = "action";
    String HEADERS = "headers";
    String JOSE="jose";
    String PROTOCOL = "protocol";
    String DOMAIN = "domain";
    String LOG_DETAILS = "log_details";
    String CODE = "code";
    String MESSAGE = "message";
    String TRACE = "trace";

    //Request props
    String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    String JOSE_HEADERS = "headers.jose";
    String DOMAIN_HEADERS = "headers.domain";
    String SENDER_CODE = "x-hcx-sender_code";
    String RECIPIENT_CODE = "x-hcx-recipient_code";
    String API_CALL_ID = "x-hcx-api_call_id";
    String CORRELATION_ID = "x-hcx-correlation_id";
    String WORKFLOW_ID = "x-hcx-workflow_id";
    String TIMESTAMP = "x-hcx-timestamp";
    String DEBUG_FLAG = "x-hcx-debug_flag";
    String STATUS = "x-hcx-status";
    String ERROR_DETAILS = "x-hcx-error_details";
    String DEBUG_DETAILS = "x-hcx-debug_details";
    String STATUS_FILTERS = "x-hcx-status_filters";
    String STATUS_RESPONSE = "x-hcx-status_response";
    List<String> STATUS_SEARCH_ALLOWED_ENTITIES = Arrays.asList("coverageeligibility", "preauth", "claim","predetermination");
    String SERVICE_MODE = "service.mode";
    String GATEWAY = "gateway";
    String KAFKA_TOPIC_PAYLOAD = "kafka.topic.payload";

    String HEADER_AUDIT = "hcx_audit";
    String PAYOR = "payor";
    String PROVIDER = "provider";
    String ROLES = "roles";
    String SCHEME_CODE = "scheme_code";
    String PARTICIPANT_CODE = "participant_code";
    String OSID = "osid";
    String AUTHORIZATION = "Authorization";
    String FILTERS = "filters";

    String SEARCH_REQ ="x-hcx-search";
    String SEARCH_RESP = "x-hcx-search_response";
    List<String> SEARCH_REQ_KEYS = Arrays.asList("filters","time_period");
    String SEARCH_FILTERS = "filters";
    List<String>  SEARCH_FILTER_KEYS = Arrays.asList("senders","receivers","entity_types","workflow_ids","case_ids","entity_status");
    String SEARCH_FILTERS_RECEIVER = "receivers";
    List<String> SEARCH_RES_KEYS = Arrays.asList("count","entity_counts");
    String HCX_REGISTRY_CODE = "hcx-registry-code";

}
