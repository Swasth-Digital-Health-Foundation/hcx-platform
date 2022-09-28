package io.hcxprotocol.interfaces;

import io.hcxprotocol.dto.HCXIntegrator;

import java.util.Map;

public interface OutgoingInterface {

    boolean processFunction(String fhirPayload, HCXIntegrator.OPERATIONS operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output) throws Exception;
    boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error) throws Exception;
    boolean createHeader(String recipient_key, String actionJwe, String onActionStatus, Map<String,Object> headers) throws Exception;
    boolean encryptPayload(Map<String,Object> Headers, Object Payload, Map<String,Object> output);
    boolean initializeHCXCall(String JWSPayload, Map<String,Object> response) throws Exception;

}
