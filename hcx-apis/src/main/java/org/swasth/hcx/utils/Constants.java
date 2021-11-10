package org.swasth.hcx.utils;

public interface Constants {
    public static final String APPLICATION_HEALTH = "hcx.health";
    public static final String APPLICATION_SERVICE_HEALTH = "hcx.service.health";

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK ="coverageeligibility.check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK ="coverageeligibility.oncheck";

    // Claims APIs
    public static final String PRE_AUTH_SUBMIT ="preauth.submit";
    public static final String PRE_AUTH_ONSUBMIT ="preauth.onsubmit";
    public static final String PRE_AUTH_SEARCH ="preauth.search";
    public static final String PRE_AUTH_ONSEARCH ="preauth.onsearch";
    public static final String CLAIM_SUBMIT ="claim.submit";
    public static final String CLAIM_ONSUBMIT ="claim.onsubmit";
    public static final String CLAIM_SEARCH ="claim.search";
    public static final String CLAIM_ONSEARCH ="claim.onsearch";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST ="paymentnotice.request";
    public static final String PAYMENT_NOTICE_ONREQUEST ="paymentnotice.onrequest";
    public static final String PAYMENT_NOTICE_SEARCH ="paymentnotice.search";
    public static final String PAYMENT_NOTICE_ONSEARCH ="paymentnotice.onsearch";

}
