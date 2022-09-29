package io.hcxprotocol.interfaces;

import io.hcxprotocol.dto.HCXIntegrator;

import java.util.Map;

/**
 * The <b>Incoming</b> Interface provide the methods to help in processing the JWE Payload and extract FHIR Object.
 * The implementation of this interface process the JWE Payload, to extract the FHIR Object and validate it using HCX FHIR IG.
 */
public interface IncomingInterface {
    /**
     * The incoming request to the participant system processed to extract and validate the FHIR object.
     * It has the implementation of below steps.
     * <ol>
     *     <li>Validating HCX Protocol headers</li>
     *     <li>Decryption of the payload and extracting FHIR object</li>
     *     <li>Validating the FHIR object using HCX FHIR IG.</li>
     * </ol>
     * @param jwePayload The JWE payload from the incoming API request body.
     * @param operation The HCX operation name.
     * @param output A wrapper map to collect the outcome (errors or response) of the JWE Payload processing.
     * <ol>
     *    <li>output -
     *    <pre>
     *    {@code {
     *       "headers":{}, - protocol headers
     *       "fhirPayload":{}, - fhir object
     *       "responseObj":{} - success/error response object
     *    }}</pre>
     *    </li>
     *    <li>success response object -
     *    <pre>
     *    {@code {
     *       "timestamp": , - unix timestamp
     *       "correlation_id": "", - fetched from incoming request
     *       "api_call_id": "" - fetched from incoming request
     *    }}</pre>
     *    </li>
     *    <li>error response object -
     *    <pre>
     *    {@code {
     *       "timestamp": , - unix timestamp
     *       "error": {
     *           "code" : "", - error code
     *           "message": "", - error message
     *           "trace":"" - error trace
     *        }
     *    }}</pre>
     *    </li>
     *  </ol>
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
     * @param jwePayload The JWE payload from the incoming API request body.
     * @param operation The HCX operation or action defined by specs to understand the functional behaviour.
     * @param error A wrapper map to collect the errors from the JWE Payload.
     * <pre>
     *    {@code {
     *       "error_code": "error_message"
     *    }}</pre>
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
     * @param jwePayload The JWE payload from the incoming API request body.
     * @param output A wrapper map to collect the outcome (errors or response) of the JWE Payload after decryption.
     * <ol>
     *    <li>success output -
     *    <pre>
     *    {@code {
     *       "headers":{}, - protocol headers
     *       "fhirPayload":{} - fhir object
     *    }}</pre>
     *    </li>
     *    <li>error output -
     *    <pre>
     *    {@code {
     *       "error_code": "error_message"
     *    }}</pre>
     *    </li>
     *  </ol>
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
     * <pre>
     *    {@code {
     *       "error_code": "error_message"
     *    }}</pre>
     * @return It is a boolean value to understand the validation status of FHIR Payload.
     * <ol>
     *     <li>true - Validation is successful.</li>
     *     <li>false - Validation is failure.</li>
     * </ol>
     */
    boolean validatePayload(String fhirPayload, HCXIntegrator.OPERATIONS operation, Map<String,Object> error);

    /**
     * Here the final output object to return to the HCX Gateway crated using the error and output maps.
     * This method has the logic to generate the output of the incoming request from HCX Gateway. It is used internally.
     *
     * @param error A wrapper map to collect the errors from the JWE or FHIR Payload validations.
     * @param output A wrapper map to collect the response of the JWE Payload processing.
     * <ol>
     *    <li>success output -
     *    <pre>
     *    {@code {
     *       "responseObj": {
     *       "timestamp": , - unix timestamp
     *       "correlation_id": "", - fetched from incoming request
     *       "api_call_id": "" - fetched from incoming request
     *      }
     *    }}</pre>
     *    </li>
     *    <li>error output -
     *    <pre>
     *    {@code {
     *      "responseObj":{
     *       "timestamp": , - unix timestamp
     *       "error": {
     *           "code" : "", - error code
     *           "message": "", - error message
     *           "trace":"" - error trace
     *        }
     *      }
     *    }}</pre>
     *    </li>
     *  </ol>
     * @return It is a boolean value to understand the final status is successful or failure.
     */
    boolean sendResponse(Map<String,Object> error, Map<String,Object> output);

}
