package io.hcxprotocol.interfaces;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.hcxprotocol.dto.HCXIntegrator;

import java.util.Map;

public interface OutgoingInterface {

    /**
     *
     * @param fhirPayload
     * @param operation
     * @param recipientCode
     * @param actionJwe
     * @param onActionStatus
     * @param output
     * @return
     */
    boolean processFunction(String fhirPayload, HCXIntegrator.OPERATIONS operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output);

    /**
     *
     * @param fhirPayload
     * @param operation
     * @param error
     * @return
     */
    boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error);

    /**
     *
     * @param recipientCode
     * @param actionJwe
     * @param onActionStatus
     * @param headers
     * @return
     */
    boolean createHeader(String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> headers);

    /**
     *
     * @param headers
     * @param fhirPayload
     * @param output
     * @return
     */
    boolean encryptPayload(Map<String,Object> headers, String fhirPayload, Map<String,Object> output);

    /**
     *
     * @param jwePayload
     * @param operation
     * @param response
     * @return
     * @throws JsonProcessingException
     */
    boolean initializeHCXCall(String jwePayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> response) throws JsonProcessingException;

}
