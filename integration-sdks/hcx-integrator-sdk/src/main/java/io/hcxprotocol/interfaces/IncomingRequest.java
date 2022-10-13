package io.hcxprotocol.interfaces;

import io.hcxprotocol.utils.Operations;

import java.util.Map;

/**
 * The <b>Incoming Request</b> Interface provide the methods to help in processing the JWE Payload and extract FHIR Object.
 */
public interface IncomingRequest {
    /**
     * Process the JWE Payload based on the Operation to validate and extract the FHIR Object.
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
    boolean process(String jwePayload, Operations operation, Map<String,Object> output);

    /**
     * Validates the HCX Protocol Headers from the JWE Payload.
     * This method is used by {@index IncomingInterface.processFunction} for validate the headers.
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
    boolean validateRequest(String jwePayload, Operations operation, Map<String,Object> error);

    /**
     * Decrypt the JWE Payload using the Participant System Private Key (which is available from the configuration).
     * The JWE Payload follows RFC7516.
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
     * Validates the FHIR Object structure and required attributes using HCX FHIR IG.
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
    boolean validatePayload(String fhirPayload, Operations operation, Map<String,Object> error);

    /**
     * Generates the HCX Protocol API response using validation errors and the output object.
     *
     * @param error A wrapper map to collect the errors from the JWE or FHIR Payload validations.
     * @param output A wrapper map to collect the response of the JWE Payload processing.
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
     * @return It is a boolean value to understand the final status is successful or failure.
     */
    boolean sendResponse(Map<String,Object> error, Map<String,Object> output);

}
