package org.swasth.apigateway.models;


import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.apigateway.constants.Constants;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.swasth.apigateway.constants.Constants.*;

public class ErrorRequest {

    private Map<String, Object> errorRequest;

    public ErrorRequest(Map<String, Object> body) throws ClientException {
        try {
            this.errorRequest = body;
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid payload");
        }
    }

    public void validate(List<String> mandatoryHeaders,Map<String, Object> senderDetails, Map<String, Object> recipientDetails, String subject) throws ClientException {
        List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !errorRequest.containsKey(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            throw new ClientException(ErrorCodes.ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + missingHeaders);
        }


        String correlationId = (String) errorRequest.get(Constants.CORRELATION_ID);
        validateCondition(!Utils.isUUID(correlationId), ErrorCodes.ERR_INVALID_CORRELATION_ID, "Correlation id should be a valid UUID");

        String status = (String) errorRequest.get(Constants.STATUS);
        validateCondition(!status.equals(Constants.ERROR_RESPONSE), ErrorCodes.ERR_INVALID_STATUS, "Invalid Status");

        Map<String, Object> errorDetails = (Map) errorRequest.get(Constants.ERROR_DETAILS);

        validateDetails(errorDetails, ErrorCodes.ERR_INVALID_ERROR_DETAILS, "Error details cannot be null, empty and other than 'JSON Object'", ERROR_DETAILS_VALUES, "Error details should contain only: ");
        validateParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, "sender");
        validateParticipant(recipientDetails, ErrorCodes.ERR_INVALID_RECIPIENT, "recipient");
        validateCondition(!StringUtils.equals(((ArrayList) senderDetails.get(OS_OWNER)).get(0).toString(), subject), ErrorCodes.ERR_ACCESS_DENIED, "Caller id and sender code is not matched");
    }

    private void validateCondition(Boolean condition, ErrorCodes errorcode, String msg) throws ClientException {
        if(condition){
            throw new ClientException(errorcode, msg);
        }
    }

    private void validateParticipant(Map<String,Object> details, ErrorCodes code, String participant) throws ClientException {
        if(details.isEmpty()){
            throw new ClientException(code, participant + " is not exist in registry");
        } else if(StringUtils.equals((String) details.get("status"), BLOCKED) || StringUtils.equals((String) details.get("status"), INACTIVE)){
            throw new ClientException(code, participant + "  is blocked or inactive as per the registry");
        }
    }

    private void validateDetails(Map<String, Object> inputMap, ErrorCodes errorCode, String msg,List<String> rangeValues, String rangeMsg) throws ClientException {
        if (MapUtils.isEmpty(inputMap)) {
            throw new ClientException(errorCode, msg);
        }else if (!inputMap.containsKey("code") || !inputMap.containsKey("message")) {
            throw new ClientException(errorCode, "Mandatory fields code or message is missing");
        } else if (!RECIPIENT_ERROR_VALUES.contains(inputMap.get("code"))){
            throw new ClientException(errorCode, "Invalid Recipient Error Code");
        }
        for(String key: inputMap.keySet()){
            if(!rangeValues.contains(key)){
                throw new ClientException(errorCode, rangeMsg + rangeValues);
            }
        }
    }
}
