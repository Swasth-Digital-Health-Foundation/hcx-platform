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
     * @param output A wrapper map to collect the outcome (errors or response) of the JWE Payload processing.
     *               <br> TODO: Define the format of the output map for success and failure.
     *      <pre>{@code
     *      {
     *          "status": "failure",
     *          "message": "The Registry API is not functional."
     *      }
     *     }</pre>
     *
     * @return It is a boolean value to understand the incoming request processing is successful or not.
     *  <ol>
     *      <li>true - It is successful.</li>
     *      <li>false - It is failure.</li>
     *  </ol>
     *
     */
    boolean processFunction(String jwePayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> output);

    /**
     * The JWE Payload of the incoming request to the participant system requires HCX Protocol headers validation.
     * This method is used by {@index IncomingInterface.processFunction} to validate the headers.
     * @param jwePayload The JWE from the incoming API request body.
     * @param error A wrapper map to collect the errors from the JWE Payload validation.
     * @return It is a boolean value to understand the validation status of JWE Payload.
     * <ol>
     *     <li>true - Validation is successful.</li>
     *     <li>false - Validation is failure.</li>
     * </ol>
     *
     */
    boolean validateRequest(String jwePayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error);

    /**
     * The JWE Payload decrypted using the participant system encryption private key to extract the FHIR object.
     *
     * @param jwePayload The JWE from the incoming API request body.
     * @param output A wrapper map to collect the outcome (errors or response) of the JWE Payload after decryption.
     * @return It is a boolean value to understand the decryption status of JWE Payload.
     * <ol>
     *     <li>true - Decryption is successful.</li>
     *     <li>false - Decryption is failure.</li>
     * </ol>
     */
    boolean decryptPayload(String jwePayload, Map<String,Object> output);

    /**
     * The FHIR object resource type validation based on the operation executed here.
     * Also, it used the HCX FHIR IG to validate the format, structure and minimum required attributes.
     *
     * @param fhirPayload The FHIR object extracted from the incoming JWE Payload.
     * @param operation The HCX operation or action defined by specs to understand the functional behaviour.
     * @param error A wrapper map to collect the errors from the FHIR Payload validation.
     * @return It is a boolean value to understand the validation status of FHIR Payload.
     * <ol>
     *     <li>true - Validation is successful.</li>
     *     <li>false - Validation is failure.</li>
     * </ol>
     */
    boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error);

    /**
     * Here the final output object to return to the HCX Gateway crated using the error and output maps created by validations and decryption process.
     * This method has the logic to generate the output of the incoming request from HCX Gateway. It is used internally.
     *
     * @param error A wrapper map containing the errors from the JWE or FHIR Payload validations.
     * @param output A wrapper map containing the response of the JWE Payload processing.
     * @return It is a boolean value to understand the final status is successful or failure.
     */
    boolean sendResponse(Map<String,Object> error, Map<String,Object> output);

}
