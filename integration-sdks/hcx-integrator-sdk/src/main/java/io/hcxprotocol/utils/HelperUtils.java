package io.hcxprotocol.utils;

import java.util.ArrayList;
import java.util.List;
import static io.hcxprotocol.utils.Constants.*;

public class HelperUtils {

    public static List<String> getMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        //Jose Headers alg, enc
        mandatoryHeaders.add(ALG);
        mandatoryHeaders.add(ENC);
        // protocol_mandatory_headers:x-hcx-sender_code, x-hcx-recipient_code, x-hcx-api_call_id, x-hcx-timestamp, x-hcx-correlation_id
        mandatoryHeaders.add(HCX_SENDER_CODE);
        mandatoryHeaders.add(HCX_RECIPIENT_CODE);
        mandatoryHeaders.add(API_CALL_ID);
        mandatoryHeaders.add(TIMESTAMP);
        mandatoryHeaders.add(CORRELATION_ID);
        return mandatoryHeaders;
    }

    public static List<String> getRedirectMandatoryHeaders() {
        List<String> redirectMandatoryHeaders = new ArrayList<>();
        //redirect_mandatory_headers:x-hcx-sender_code, x-hcx-recipient_code, x-hcx-api_call_id, x-hcx-timestamp, x-hcx-correlation_id, x-hcx-status, x-hcx-redirect_to
        redirectMandatoryHeaders.add(HCX_SENDER_CODE);
        redirectMandatoryHeaders.add(HCX_RECIPIENT_CODE);
        redirectMandatoryHeaders.add(API_CALL_ID);
        redirectMandatoryHeaders.add(TIMESTAMP);
        redirectMandatoryHeaders.add(CORRELATION_ID);
        redirectMandatoryHeaders.add(STATUS);
        redirectMandatoryHeaders.add(REDIRECT_TO);
        return redirectMandatoryHeaders;
    }

    public static List<String> getErrorMandatoryHeaders() {
        List<String> errorMandatoryHeaders = new ArrayList<>();
        //error_mandatory_headers:x-hcx-status, x-hcx-sender_code, x-hcx-recipient_code, x-hcx-error_details, x-hcx-correlation_id, x-hcx-api_call_id, x-hcx-timestamp
        errorMandatoryHeaders.add(STATUS);
        errorMandatoryHeaders.add(HCX_SENDER_CODE);
        errorMandatoryHeaders.add(HCX_RECIPIENT_CODE);
        errorMandatoryHeaders.add(ERROR_DETAILS);
        errorMandatoryHeaders.add(CORRELATION_ID);
        errorMandatoryHeaders.add(API_CALL_ID);
        errorMandatoryHeaders.add(TIMESTAMP);
        return errorMandatoryHeaders;
    }
}
