package io.hcxprotocol.dto;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class HCXIntegrator {

    private static HCXIntegrator hcxIntegrator = null;

    private HCXIntegrator() {
    }

    public static HCXIntegrator getInstance() {
        if (hcxIntegrator == null)
            hcxIntegrator = new HCXIntegrator();
        return hcxIntegrator;
    }

    Config config = ConfigFactory.load();

    public String getHCXBaseUrl() {
        return config.getString("hcx.base.url");
    }

    public String getParticipantCode() {
        return config.getString("participant.code");
    }

    public String getKeycloakUrl() {
        return config.getString("keycloak.url");
    }

    public String getKeycloakUsername() {
        return config.getString("keycloak.username");
    }

    public String getKeycloakPassword() {
        return config.getString("keycloak.password");
    }

    public String getPrivateKeyUrl() {
        return config.getString("private.key.url");
    }

    public String getIGUrl() {
        return config.getString("ig.url");
    }

    public enum ERROR_CODES {
        ERR_INVALID_PAYLOAD,
        ERR_INVALID_ENCRYPTION,
        ERR_WRONG_DOMAIN_PAYLOAD,
        ERR_INVALID_DOMAIN_PAYLOAD,
        ERR_SENDER_NOT_SUPPORTED,
        ERR_MANDATORY_HEADER_MISSING,
        ERR_INVALID_API_CALL_ID,
        ERR_INVALID_CORRELATION_ID,
        ERR_INVALID_TIMESTAMP,
        ERR_INVALID_REDIRECT_TO,
        ERR_INVALID_STATUS,
        ERR_INVALID_DEBUG_FLAG,
        ERR_INVALID_ERROR_DETAILS,
        ERR_INVALID_DEBUG_DETAILS,
        ERR_INVALID_WORKFLOW_ID,
        ERR_SERVICE_UNAVAILABLE,
        ERR_DOMAIN_PROCESSING
    }

    public enum OPERATIONS {
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

        OPERATIONS(final String operation, final String fhirResourceType) {
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

}
