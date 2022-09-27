package io.hcxprotocol.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface IncomingInterface {

    boolean processFunction(String jwePayload, String operation, Map<String,Object> output) throws JsonProcessingException, Exception;
    boolean validateRequest(String jwePayload, Map<String,Object> error);
    boolean decryptPayload(String jwePayload, Map<String,Object> output);
    boolean validatePayload(String fhirPayload, String operation, Map<String,Object> error);
    void sendResponse(Map<String,Object> error, Map<String,Object> output) throws Exception;

}
