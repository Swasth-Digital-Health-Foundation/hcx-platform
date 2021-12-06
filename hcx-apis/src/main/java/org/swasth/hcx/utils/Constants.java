package org.swasth.hcx.utils;

public  class Constants {

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK = "/v1/coverageeligibility/check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK = "/v1/coverageeligibility/oncheck";

    // Claims APIs
    public static final String PRE_AUTH_SUBMIT = "/v1/preauth/submit";
    public static final String PRE_AUTH_ONSUBMIT = "/v1/preauth/onsubmit";
    public static final String PRE_AUTH_SEARCH = "/v1/preauth/search";
    public static final String PRE_AUTH_ONSEARCH = "/v1/preauth/onsearch";
    public static final String CLAIM_SUBMIT = "/v1/claim/submit";
    public static final String CLAIM_ONSUBMIT = "/v1/claim/onsubmit";
    public static final String CLAIM_SEARCH = "/v1/claim/search";
    public static final String CLAIM_ONSEARCH = "/v1/claim/onsearch";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST = "/v1/paymentnotice/request";
    public static final String PAYMENT_NOTICE_ONREQUEST = "/v1/paymentnotice/onrequest";
    public static final String PAYMENT_NOTICE_SEARCH = "/v1/paymentnotice/search";
    public static final String PAYMENT_NOTICE_ONSEARCH = "/v1/paymentnotice/onsearch";

    public static final String HEALTHY = "healthy";
    public static final String NAME = "name";
    public static final String KAFKA = "kafka";
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
    public static final String STATUS = "status";
    public static final String SUBMITTED = "submitted";

    //Request props
    public static final String PAYLOAD_MANDATORY_PROPERTIES = "payload.mandatory.properties";
    public static final String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    public static final String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    public static final String JOSE_HEADERS = "headers.jose";
    public static final String DOMAIN_HEADERS = "headers.domain";
    public static final String PROTECTED = "protected";
    public static final String SENDER_CODE = "x-hcx-sender_code";
    public static final String CORRELATION_ID = "x-hcx-correlation_id";
    public static final String SERVICE_MODE = "service.mode";
    public static final String GATEWAY = "gateway";
    public static final String KAFKA_TOPIC_PAYLOAD = "kafka.topic.payload";

}
