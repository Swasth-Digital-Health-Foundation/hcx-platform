package io.hcxprotocol.helper;

import io.hcxprotocol.exception.ErrorCodes;
import io.hcxprotocol.dto.JSONRequest;
import io.hcxprotocol.dto.JWERequest;
import io.hcxprotocol.utils.JSONUtils;
import io.hcxprotocol.utils.Operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.hcxprotocol.utils.Constants.*;
import static io.hcxprotocol.utils.ResponseMessage.INVALID_JSON_REQUEST_BODY_ERR_MSG;

/**
 * This is to validate the incoming request protocol headers.
 */
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
     *
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
    public boolean validateRequest(String payload, Operations operation, Map<String, Object> error) {
        try {
            // Convert the input string into a Map
            Map<String, Object> requestBody = JSONUtils.deserialize(payload, HashMap.class);
            if (requestBody.containsKey(PAYLOAD)) {
                if (validateJWERequest(operation, error, requestBody)) return false;
            } else {
                if (!operation.toString().contains("on_")) {
                    error.put(ErrorCodes.ERR_INVALID_PAYLOAD.toString(), INVALID_JSON_REQUEST_BODY_ERR_MSG);
                    return false;
                }
                if (validateJsonRequest(operation, error, requestBody)) return false;
            }
        } catch (Exception e) {
            error.put(ErrorCodes.ERR_INVALID_PAYLOAD.toString(), e.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateJWERequest(Operations operation, Map<String, Object> error, Map<String, Object> requestBody) throws Exception {
        // Fetch the value of the only key(payload) from the map
        JWERequest jweRequest = new JWERequest(requestBody);
        // Split the extracted above value into an array using . as delimiter
        String[] payloadArr = jweRequest.getPayloadValues();
        if (jweRequest.validateJwePayload(error, payloadArr)) return true;
        // Validate the headers and if there are any failures add the corresponding error message to the error Map
        // protocol_mandatory_headers:x-hcx-sender_code, x-hcx-recipient_code, x-hcx-api_call_id, x-hcx-timestamp, x-hcx-correlation_id
        return jweRequest.validateHeadersData(List.of(ALG,ENC,HCX_SENDER_CODE,HCX_RECIPIENT_CODE,HCX_API_CALL_ID,HCX_TIMESTAMP,HCX_CORRELATION_ID), operation, error);
    }

    private boolean validateJsonRequest(Operations operation, Map<String, Object> error, Map<String, Object> requestBody) throws Exception {
        JSONRequest jsonRequest = new JSONRequest(requestBody);
        if (ERROR_RESPONSE.equalsIgnoreCase(jsonRequest.getStatus())) {
            //error_mandatory_headers:x-hcx-status, x-hcx-sender_code, x-hcx-recipient_code, x-hcx-error_details, x-hcx-correlation_id, x-hcx-api_call_id, x-hcx-timestamp
            return jsonRequest.validateHeadersData(List.of(STATUS,HCX_SENDER_CODE,HCX_RECIPIENT_CODE,ERROR_DETAILS,HCX_CORRELATION_ID,HCX_API_CALL_ID,HCX_TIMESTAMP), operation, error);
        } else {
            //redirect_mandatory_headers:x-hcx-sender_code, x-hcx-recipient_code, x-hcx-api_call_id, x-hcx-timestamp, x-hcx-correlation_id, x-hcx-status, x-hcx-redirect_to
            if (jsonRequest.validateHeadersData(List.of(HCX_SENDER_CODE,HCX_RECIPIENT_CODE,HCX_API_CALL_ID,HCX_TIMESTAMP,HCX_CORRELATION_ID,STATUS,REDIRECT_TO), operation, error))
                return true;
            return jsonRequest.validateRedirect(error);
        }
    }

}
