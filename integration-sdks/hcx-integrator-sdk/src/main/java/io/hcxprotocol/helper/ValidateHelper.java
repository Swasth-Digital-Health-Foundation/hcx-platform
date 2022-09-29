package io.hcxprotocol.helper;

import io.hcxprotocol.dto.HCXIntegrator;
import io.hcxprotocol.model.JSONRequest;
import io.hcxprotocol.model.JWERequest;
import io.hcxprotocol.utils.HelperUtils;
import io.hcxprotocol.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import static io.hcxprotocol.utils.Constants.*;


public class ValidateHelper {

    private static ValidateHelper validateHelper = null;

    private ValidateHelper() {
    }

    public static ValidateHelper getInstance() {
        if (validateHelper == null)
            validateHelper = new ValidateHelper();
        return validateHelper;
    }

    /**
     * Validates the incoming payload by verifying the structure and contents of the headers inside the payload
     * <p>
     * * ERR_INVALID_PAYLOAD:
     * * 1. Request body is not a valid JWE token (as defined in RFC 7516)
     * * 2. Any mandatory elements of JWE token are missing
     * * 3. Any elements of the JWE token are in invalid format
     * *
     * * ERR_SENDER_NOT_SUPPORTED:
     * * Sender is not allowed to send this API request to this recipient.
     * *
     * * HCX Protocol errors:
     * * ERR_MANDATORY_HEADER_MISSING
     * * ERR_INVALID_API_CALL_ID
     * * ERR_INVALID_CORRELATION_ID: check only for the format correctness
     * * ERR_INVALID_TIMESTAMP
     * * ERR_INVALID_REDIRECT_TO
     * * ERR_INVALID_STATUS
     * * ERR_INVALID_DEBUG_FLAG
     * * ERR_INVALID_ERROR_DETAILS
     * * ERR_INVALID_DEBUG_DETAILS
     * * ERR_INVALID_WORKFLOW_ID: check only for the format correctness
     * *
     *
     * @param payload   json string with payload
     * @param operation which operation is being processed
     * @param error     holds any validation errors
     * @return true if it is valid request otherwise returns false along with proper error message in the error map
     */
    public boolean validateRequest(String payload, HCXIntegrator.OPERATIONS operation, Map<String, Object> error) {
        try {
            // Convert the input string into a Map
            Map<String, Object> requestBody = JSONUtils.deserialize(payload, HashMap.class);
            if (requestBody.containsKey(PAYLOAD)) {
                if (validateJWERequest(operation, error, requestBody)) return false;
            } else {
                if (!operation.toString().contains("on_")) {
                    error.put(HCXIntegrator.ERROR_CODES.ERR_INVALID_PAYLOAD.toString(), INVALID_JSON_REQUEST_BODY_ERR_MSG);
                    return false;
                }
                if (validateJsonRequest(operation, error, requestBody)) return false;
            }
        } catch (Exception e) {
            error.put(HCXIntegrator.ERROR_CODES.ERR_INVALID_PAYLOAD.toString(), e.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateJWERequest(HCXIntegrator.OPERATIONS operation, Map<String, Object> error, Map<String, Object> requestBody) throws Exception {
        // Fetch the value of the only key(payload) from the map
        JWERequest jweRequest = new JWERequest(requestBody);
        // Split the extracted above value into an array using . as delimiter
        String[] payloadArr = jweRequest.getPayloadValues();
        if (jweRequest.validateJwePayload(error, payloadArr)) return true;
        // Validate the headers and if there are any failures add the corresponding error message to the error Map
        return jweRequest.validateHeadersData(HelperUtils.getMandatoryHeaders(), operation, error);
    }

    private boolean validateJsonRequest(HCXIntegrator.OPERATIONS operation, Map<String, Object> error, Map<String, Object> requestBody) throws Exception {
        JSONRequest jsonRequest = new JSONRequest(requestBody);
        if (ERROR_RESPONSE.equalsIgnoreCase(jsonRequest.getStatus())) {
            return jsonRequest.validateHeadersData(HelperUtils.getErrorMandatoryHeaders(), operation, error);
        } else {
            if (jsonRequest.validateHeadersData(HelperUtils.getRedirectMandatoryHeaders(), operation, error))
                return true;
            return jsonRequest.validateRedirect(error);
        }
    }


}
