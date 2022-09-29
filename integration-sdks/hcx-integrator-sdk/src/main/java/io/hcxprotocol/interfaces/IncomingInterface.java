package io.hcxprotocol.interfaces;

import io.hcxprotocol.dto.HCXIntegrator;

import java.util.Map;

public interface IncomingInterface {
    /**
     * This will help in processing the incoming request to the participant system by executing below steps.
     * <ol>
     *     <li>Validating HCX Protocol headers</li>
     *     <li>Decryption of the payload and extracting FHIR object</li>
     *     <li>Validating the FHIR object using HCX FHIR IG.</li>
     * </ol>
     * @param jwePayload The JWE from the incoming API request body.
     * @param operation The HCX operation name.
     * @param output A wrapper map to collect the outcome (errors or response) of the JEW Payload processing.
     * @return It is a boolean value to understand the incoming request processing is successful or not.
     *  <ol>
     *      <li>true - It is successful.</li>
     *      <li>false - It is failure.</li>
     *  </ol>
     *
     */
    boolean processFunction(String jwePayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> output);

    /**
     *
     * @param jwePayload
     * @param error
     * @return
     */
    boolean validateRequest(String jwePayload, Map<String,Object> error);

    /**
     *
     * @param jwePayload
     * @param output
     * @return
     */
    boolean decryptPayload(String jwePayload, Map<String,Object> output);

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
     * @param error
     * @param output
     * @return
     */
    boolean sendResponse(Map<String,Object> error, Map<String,Object> output);

}
