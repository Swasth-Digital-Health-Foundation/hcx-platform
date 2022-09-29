package io.hcxprotocol.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hcxprotocol.dto.HCXIntegrator;

import java.util.Map;

public interface OutgoingInterface {

    boolean processFunction(String fhirPayload, HCXIntegrator.OPERATIONS operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output);
    boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error);
    boolean createHeader(String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> headers);
    boolean encryptPayload(Map<String,Object> headers, String fhirPayload, Map<String,Object> output);
    boolean initializeHCXCall(String jwePayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> response) throws JsonProcessingException;

}
