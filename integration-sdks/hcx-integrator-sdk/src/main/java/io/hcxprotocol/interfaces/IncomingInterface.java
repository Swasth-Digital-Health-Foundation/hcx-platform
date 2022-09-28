package io.hcxprotocol.interfaces;

import io.hcxprotocol.dto.HCXIntegrator;

import java.util.Map;

public interface IncomingInterface {

    boolean processFunction(String jwePayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> output) throws Exception;
    boolean validateRequest(String jwePayload, Map<String,Object> error);
    boolean decryptPayload(String jwePayload, Map<String,Object> output);
    boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error);
    boolean sendResponse(Map<String,Object> error, Map<String,Object> output) throws Exception;

}
