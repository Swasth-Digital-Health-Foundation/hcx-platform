package org.swasth.common.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String VERSION_PREFIX = "/v0.7";

    // Health APIs
    public static final String HEALTH = "/health";
    public static final String SERVICE_HEALTH = "/service/health";

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

    // Notification APIs
    public static final String NOTIFICATION_SUBSCRIBE = VERSION_PREFIX + "/notification/subscribe";
    public static final String NOTIFICATION_UNSUBSCRIBE = VERSION_PREFIX + "/notification/unsubscribe";
    public static final String NOTIFICATION_SUBSCRIPTION_LIST = VERSION_PREFIX + "/notification/subscription/list";
    public static final String NOTIFICATION_LIST = VERSION_PREFIX + "/notification/list";
    public static final String NOTIFICATION_REQUEST = VERSION_PREFIX + "/notification/notify";

    // Audit APIs
    public static final String AUDIT_SEARCH = VERSION_PREFIX + "/audit/search";

    // Registry Participant APIs
    public static final String PARTICIPANT_CREATE = VERSION_PREFIX + "/participant/create";
    public static final String PARTICIPANT_SEARCH = VERSION_PREFIX + "/participant/search";
    public static final String PARTICIPANT_UPDATE = VERSION_PREFIX + "/participant/update";

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
    public static final String RETRY_COUNT = "retryCount";
    public static final String RETRY_INDEX = "retryIndex";
    public static final String EID = "eid";
    public static final String AUDIT = "AUDIT";
    public static final String SENDER_ROLE = "senderRole";
    public static final String RECIPIENT_ROLE = "recipientRole";
    public static final String AUDIT_TIMESTAMP = "auditTimeStamp";
    public static final String UPDATED_TIME = "updatedTimestamp";
    public static final String REQUEST_TIME = "requestTimeStamp";
    public static  final String TRIGGER_TYPE = "triggerType";

    //Request props
    public static final String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    public static final String ERROR_HEADERS_MANDATORY = "plainrequest.headers.mandatory";
    public static final String ERROR_HEADERS_OPTIONAL = "plainrequest.headers.optional";
    public static final String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    public static final String REDIRECT_HEADERS_MANDATORY = "redirect.headers.mandatory";
    public static final String REDIRECT_HEADERS_OPTIONAL = "redirect.headers.optional";
    public static final String JOSE_HEADERS = "headers.jose";
    public static final String DOMAIN_HEADERS = "headers.domain";
    public static final String SENDER_CODE = "x-hcx-sender_code";
    public static final String RECIPIENT_CODE = "x-hcx-recipient_code";
    public static final String API_CALL_ID = "x-hcx-api_call_id";
    public static final String CORRELATION_ID = "x-hcx-correlation_id";
    public static final String WORKFLOW_ID = "x-hcx-workflow_id";
    public static final String TIMESTAMP = "x-hcx-timestamp";
    public static final String DEBUG_FLAG = "x-hcx-debug_flag";
    public static final String STATUS = "x-hcx-status";
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String SERVICE_MODE = "service.mode";
    public static final String GATEWAY = "gateway";
    public static final String KAFKA_TOPIC_PAYLOAD = "kafka.topic.payload";
    public static final String ENDPOINT_URL = "endpoint_url";
    public static final String HCX_NOT_ALLOWED_URLS = "hcx.urls.notallowed";

    public static final String HEADER_AUDIT = "hcx_audit";
    public static final String PAYOR = "payor";
    public static final String PROVIDER = "provider";
    public static final String ROLES = "roles";
    public static final String SCHEME_CODE = "scheme_code";
    public static final String PARTICIPANT_CODE = "participant_code";
    public static final String PARTICIPANT_NAME = "participant_name";
    public static final String PRIMARY_EMAIL = "primary_email";
    public static final String EQUAL_OPERATION = "eq";
    public static final String OSID = "osid";
    public static final String AUTHORIZATION = "Authorization";
    public static final String FILTERS = "filters";

    public static final String SEARCH_REQ ="x-hcx-search";
    public static final String SEARCH_RESP = "x-hcx-search_response";
    public static final List<String> SEARCH_REQ_KEYS = Arrays.asList("filters","time_period");
    public static final String SEARCH_FILTERS = "filters";
    public static final List<String>  SEARCH_FILTER_KEYS = Arrays.asList("senders","receivers","entity_types","workflow_ids","case_ids","entity_status");
    public static final String SEARCH_FILTERS_RECEIVER = "receivers";
    public static final List<String> SEARCH_RES_KEYS = Arrays.asList("count","entity_counts");
    public static final String HCX_REGISTRY_CODE = "hcx-registry-code";
    public static final String ERROR_STATUS = "response.error";
    public static final String REDIRECT_STATUS = "response.redirect";
    public static final String COMPLETE_STATUS = "response.complete";
    public static final String QUEUED_STATUS = "request.queued";
    public static final String DISPATCHED_STATUS = "request.dispatched";
    public static final String RETRY_STATUS = "request.retry";
    public static final String RETRY_PROCESSING_STATUS = "request.retry.processing";

    //Notification constants
    public static final String NOTIFICATION_ID = "x-hcx-notification_id";
    public static final String ACTIVE = "active";
    public static final String IN_ACTIVE = "inactive";
    public static final int ACTIVE_CODE = 1;
    public static final int INACTIVE_CODE = 0;
    public static final String SUBSCRIPTION_ID = "x-hcx-subscription_id";
    public static final String NOTIFICATION_HEADERS_MANDATORY = "notification.headers.mandatory";
    public static final String NOTIFICATION_HEADERS_OPTIONAL = "notification.headers.optional";
    public static final String NOTIFICATION_DATA = "x-hcx-notification_data";
    public static final String TRIGGER_VALUE = "API";


}
