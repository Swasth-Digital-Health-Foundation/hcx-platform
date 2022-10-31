package io.hcxprotocol.helper;

import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import io.hcxprotocol.exception.ErrorCodes;
import io.hcxprotocol.utils.JSONUtils;
import io.hcxprotocol.utils.Operations;
import io.hcxprotocol.validator.HCXFHIRValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of FHIR validation using HCX FHIR IG.
 */
public abstract class FhirPayload {

    public boolean validatePayload(String fhirPayload, Operations operation, Map<String,Object> error) {
        boolean returnBool = true;
        try {
            FhirValidator validator = HCXFHIRValidator.getValidator();
            ValidationResult result = validator.validateWithResult(fhirPayload);
            List<SingleValidationMessage> messages = result.getMessages();
            Map<String, Object> map = JSONUtils.deserialize(fhirPayload, Map.class);
            if (!StringUtils.equalsIgnoreCase((String) map.get("resourceType"), operation.getFhirResourceType())) {
                error.put(String.valueOf(ErrorCodes.ERR_WRONG_DOMAIN_PAYLOAD), "Incorrect eObject is sent as the domain payload");
                return false;
            }
            List<String> errMessages = new ArrayList<>();
            for (SingleValidationMessage message : messages) {
                if (message.getSeverity() == ResultSeverityEnum.ERROR) {
                    errMessages.add(message.getMessage());
                    error.put(String.valueOf(ErrorCodes.ERR_INVALID_DOMAIN_PAYLOAD), errMessages);
                    returnBool = false;
                }
            }
        }catch (Exception e){
            error.put(String.valueOf(ErrorCodes.ERR_INVALID_DOMAIN_PAYLOAD),e.getMessage());
        }
        return returnBool;
    }
}
