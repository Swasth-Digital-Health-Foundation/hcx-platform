package org.swasth.hcx.utils;

public interface Constants {

    // Coverage Eligibility APIs
    String COVERAGE_ELIGIBILITY_CHECK = "coverageeligibility/check";
    String COVERAGE_ELIGIBILITY_ONCHECK = "coverageeligibility/oncheck";

    // Claims APIs
    String PRE_AUTH_SUBMIT = "preauth/submit";
    String PRE_AUTH_ONSUBMIT = "preauth/onsubmit";
    String PRE_AUTH_SEARCH = "preauth/search";
    String PRE_AUTH_ONSEARCH = "preauth/onsearch";
    String CLAIM_SUBMIT = "claim/submit";
    String CLAIM_ONSUBMIT = "claim/onsubmit";
    String CLAIM_SEARCH = "claim/search";
    String CLAIM_ONSEARCH = "claim/onsearch";

    //Payment Notice APIs
    String PAYMENT_NOTICE_REQUEST = "paymentnotice/request";
    String PAYMENT_NOTICE_ONREQUEST = "paymentnotice/onrequest";
    String PAYMENT_NOTICE_SEARCH = "paymentnotice/search";
    String PAYMENT_NOTICE_ONSEARCH = "paymentnotice/onsearch";

    String HEALTHY = "healthy";
    String NAME = "name";
    String KAFKA = "kafka";
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
    String STATUS = "status";
    String SUBMITTED = "submitted";

    //Request props
    String PAYLOAD_MANDATORY_PROPERTIES = "payload.mandatory.properties";
    String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    String JOSE_HEADERS = "headers.jose";
    String DOMAIN_HEADERS = "headers.domain";
    String PROTECTED = "protected";
    String HEADER_CORRELATION = "x-hcx-correlation_id";
    String SERVICE_MODE = "service.mode";
    String KAFKA_TOPIC_PAYLOAD = "kafka.topic.payload";
    String KAFKA_TOPIC_ELIGIBILITY_CHECK = "kafka.topic.eligibilitycheck";

}
