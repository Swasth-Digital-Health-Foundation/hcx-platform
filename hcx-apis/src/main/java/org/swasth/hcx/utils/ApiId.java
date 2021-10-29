package org.swasth.hcx.utils;

public interface ApiId {
    public static final String APPLICATION_HEALTH = "api.hcx.health";
    public static final String APPLICATION_SERVICE_HEALTH = "api.hcx.service.health";

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK ="api.coverageeligibility.check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK ="api.coverageeligibility.oncheck";

    // Claims APIs
    public static final String PRE_AUTH_SUBMIT ="api.preauth.submit";
    public static final String PRE_AUTH_ONSUBMIT ="api.preauth.onsubmit";
    public static final String PRE_AUTH_SEARCH ="api.preauth.search";
    public static final String PRE_AUTH_ONSEARCH ="api.preauth.onsearch";
    public static final String CLAIM_SUBMIT ="api.claim.submit";
    public static final String CLAIM_ONSUBMIT ="api.claim.onsubmit";
    public static final String CLAIM_SEARCH ="api.claim.search";
    public static final String CLAIM_ONSEARCH ="api.claim.onsearch";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST ="api.paymentnotice.request";
    public static final String PAYMENT_NOTICE_ONREQUEST ="api.paymentnotice.onrequest";
    public static final String PAYMENT_NOTICE_SEARCH ="api.paymentnotice.search";
    public static final String PAYMENT_NOTICE_ONSEARCH ="api.paymentnotice.onsearch";

}
