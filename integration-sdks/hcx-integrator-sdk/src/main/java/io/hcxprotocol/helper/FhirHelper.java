package io.hcxprotocol.helper;

import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import io.hcxprotocol.dto.HCXIntegrator;
import io.hcxprotocol.utils.JSONUtils;
import io.hcxprotocol.validator.HCXFHIRValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FhirHelper {

    public static boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error) {
        boolean returnBool = true;
        try{
        FhirValidator validator = HCXFHIRValidator.getValidator();
        ValidationResult result = validator.validateWithResult(fhirPayload);
        List<SingleValidationMessage> messages = result.getMessages();
        Map<String, Object> map = JSONUtils.deserialize(fhirPayload, Map.class);
        if(map.get("resourceType") != operation.getFhirResourceType()){
            error.put(String.valueOf(HCXIntegrator.ERROR_CODES.ERR_WRONG_DOMAIN_PAYLOAD),"Incorrect eObject is sent as the domain payload");
            return false;
        }
        ArrayList<String> errMessages = null;
        for(SingleValidationMessage message: messages){
            if(message.getSeverity() == ResultSeverityEnum.ERROR){
                error.put(String.valueOf(HCXIntegrator.ERROR_CODES.ERR_INVALID_DOMAIN_PAYLOAD),errMessages.add(message.getMessage()));
                returnBool = false;
            }
        }
        }catch (Exception e){
            error.put(String.valueOf(HCXIntegrator.ERROR_CODES.ERR_INVALID_DOMAIN_PAYLOAD),e.getMessage());
        }
        return returnBool;
    }
}
