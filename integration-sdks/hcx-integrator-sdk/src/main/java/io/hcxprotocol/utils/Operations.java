package io.hcxprotocol.utils;

/**
 * The Operations of HCX Gateway to handle claims processing.
 */
public enum Operations {
    COVERAGE_ELIGIBILITY_CHECK("/coverageeligibility/check", "Bundle"),
    COVERAGE_ELIGIBILITY_ON_CHECK("/coverageeligibility/on_check", "Bundle"),
    PRE_AUTH_SUBMIT("/preauth/submit", "Bundle"),
    PRE_AUTH_ON_SUBMIT("/preauth/on_submit","Bundle"),
    CLAIM_SUBMIT("/claim/submit","Bundle"),
    CLAIM_ON_SUBMIT("/claim/on_submit","Bundle"),
    PAYMENT_NOTICE_REQUEST("/paymentnotice/request","PaymentNotice"),
    PAYMENT_NOTICE_ON_REQUEST("/paymentnotice/on_request","PaymentReconciliation"),
    HCX_STATUS("/hcx/status","StatusRequest"),
    HCX_ON_STATUS("/hcx/on_status","StatusResponse"),
    COMMUNICATION_REQUEST("/communication/request","Communication"),
    COMMUNICATION_ON_REQUEST("/communication/on_request","CommunicationRequest"),
    PREDETERMINATION_SUBMIT("/predetermination/submit","Bundle"),
    PREDETERMINATION_ON_SUBMIT("/predetermination/on_submit","Bundle");

    private final String operation;
    private final String fhirResourceType;

    Operations(final String operation, final String fhirResourceType) {
        this.operation = operation;
        this.fhirResourceType = fhirResourceType;
    }

    public String getOperation() {
        return operation;
    }

    public  String getFhirResourceType() {
        return fhirResourceType;
    }
}
