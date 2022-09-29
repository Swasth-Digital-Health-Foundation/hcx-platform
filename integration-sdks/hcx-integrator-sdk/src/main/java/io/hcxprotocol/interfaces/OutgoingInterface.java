package io.hcxprotocol.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface OutgoingInterface {

    boolean processFunction(String fhirPayload, String operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output) throws JsonProcessingException;
    boolean validatePayload(String fhirPayload, String operation, Map<String,Object> error);
    boolean createHeader(String recipient_key, String actionJwe, String onActionStatus, Map<String,Object> headers);
    boolean encryptPayload(Map<String,Object> Headers, Object Payload, Map<String,Object> output);
    boolean initializeHCXCall(String JWSPayload, Map<String,Object> response) throws JsonProcessingException;

}
