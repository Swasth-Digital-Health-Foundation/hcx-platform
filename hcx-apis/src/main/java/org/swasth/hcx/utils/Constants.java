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

}