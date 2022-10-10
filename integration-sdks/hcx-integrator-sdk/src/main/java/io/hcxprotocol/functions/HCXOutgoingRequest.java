package io.hcxprotocol.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hcxprotocol.dto.HCXIntegrator;
import io.hcxprotocol.dto.HttpResponse;
import io.hcxprotocol.exception.ErrorCodes;
import io.hcxprotocol.helper.FhirPayload;
import io.hcxprotocol.interfaces.OutgoingRequest;
import io.hcxprotocol.utils.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.swasth.jose.jwe.JweRequest;
import org.swasth.jose.jwe.key.PublicKeyLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The <b>HCX Outgoing Request</b> class provide the methods to help in creating the JWE Payload and send the request to the sender system from HCX Gateway.
 *
 * <ul>
 *     <li><b>generate</b> is the main method to use by the integrator(s) to generate JWE Payload.
 *     <ul>
 *         <li>This method handles two types of requests. There are two implementations of <b>generate</b> method to handle action, on_action API calls.
 *         <ol>
 *            <li>
 *              Sending an initial request of HCX workflow action.
 *            </li>
 *            <li>Sending a response to the incoming HCX workflow action.
 *              <ul>
 *                  <li>The input request JEW should be used as <i>actionJwe</i>.</li>
 *              </ul>
 *            </li>
 *        </ol></li>
 *     </ul></li>
 *     <li>
 *         <b>validatePayload, createHeader, encryptPayload, initializeHCXCall</b> methods are used by <b>generate</b> method to compose the JWE payload generation functionality.
 *         These methods are available for the integrator(s) to use them based on different scenario(s) or use cases.
 *     </li>
 * </ul>
 *
 */
public class HCXOutgoingRequest extends FhirPayload implements OutgoingRequest {

    private final HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance();

    @Override
    public boolean generate(String fhirPayload, Operations operation, String recipientCode, Map<String,Object> output){
        return process(fhirPayload, operation, recipientCode, "", "", output);
    }

    @Override
    public boolean generate(String fhirPayload, Operations operation, String actionJwe, String onActionStatus, Map<String,Object> output){
        return process(fhirPayload, operation, "", actionJwe, onActionStatus, output);
    }

    private boolean process(String fhirPayload, Operations operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output){
        boolean result = false;
        try {
            Map<String, Object> error = new HashMap<>();
            Map<String, Object> headers = new HashMap<>();
            Map<String, Object> response = new HashMap<>();
            if (!validatePayload(fhirPayload, operation, error)) {
                output.putAll(error);
            } else if (!createHeader(recipientCode, actionJwe, onActionStatus, headers)) {
                output.putAll(error);
            } else if (!encryptPayload(headers, fhirPayload, output)) {
                output.putAll(error);
            } else {
                result = initializeHCXCall(JSONUtils.serialize(output), operation, response);
                output.putAll(response);
            }
            return result;
        } catch (JsonProcessingException ex) {
            // TODO: JsonProcessingException is handled as domain processing error, we will be enhancing in next version.
            output.put(ErrorCodes.ERR_DOMAIN_PROCESSING.toString(), ex.getMessage());
            return result;
        }
    }

    @Override
    public boolean createHeader(String recipientCode, String actionJwe, String onActionStatus, Map<String, Object> headers) {
        try {
            headers.put(Constants.ALG, Constants.A256GCM);
            headers.put(Constants.ENC, Constants.RSA_OAEP);
            headers.put(Constants.HCX_API_CALL_ID, UUID.randomUUID().toString());
            headers.put(Constants.HCX_TIMESTAMP,  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
            if (!StringUtils.isEmpty(recipientCode)) {
                headers.put(Constants.HCX_SENDER_CODE, hcxIntegrator.getParticipantCode());
                headers.put(Constants.HCX_RECIPIENT_CODE, recipientCode);
                headers.put(Constants.HCX_CORRELATION_ID, UUID.randomUUID().toString());
            } else {
                Map<String,Object> actionHeaders = JSONUtils.decodeBase64String(actionJwe.split("\\.")[0], Map.class);
                headers.put(Constants.HCX_SENDER_CODE,  actionHeaders.get(Constants.HCX_RECIPIENT_CODE));
                headers.put(Constants.HCX_RECIPIENT_CODE, actionHeaders.get(Constants.HCX_SENDER_CODE));
                headers.put(Constants.HCX_CORRELATION_ID, actionHeaders.get(Constants.HCX_CORRELATION_ID));
                headers.put(Constants.STATUS, onActionStatus);
                if(headers.containsKey(Constants.WORKFLOW_ID))
                    headers.put(Constants.WORKFLOW_ID, actionHeaders.get(Constants.WORKFLOW_ID));
            }
            return true;
        } catch (Exception e) {
            headers.put(Constants.ERROR, "Error while creating headers: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean encryptPayload(Map<String,Object> headers, String fhirPayload, Map<String,Object> output) {
        try {
            String publicKeyUrl = (String) Utils.searchRegistry(headers.get(Constants.HCX_RECIPIENT_CODE)).get(Constants.ENCRYPTION_CERT);
            String certificate = IOUtils.toString(new URL(publicKeyUrl), StandardCharsets.UTF_8.toString());
            InputStream stream = new ByteArrayInputStream(certificate.getBytes());
            Reader fileReader = new InputStreamReader(stream);
            RSAPublicKey rsaPublicKey = PublicKeyLoader.loadPublicKeyFromX509Certificate(fileReader);
            JweRequest jweRequest = new JweRequest(headers, JSONUtils.deserialize(fhirPayload, Map.class));
            jweRequest.encryptRequest(rsaPublicKey);
            output.putAll(jweRequest.getEncryptedObject());
            return true;
        } catch (Exception e) {
            output.put(Constants.ERROR, e.getMessage());
            return false;
        }
    }

    // we are handling the JsonProcessingException in processFunction method
    @Override
    public boolean initializeHCXCall(String jwePayload, Operations operation, Map<String,Object> response) throws JsonProcessingException {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, "Bearer " + Utils.generateToken());
        HttpResponse hcxResponse = HttpUtils.post(hcxIntegrator.getHCXProtocolBasePath() + operation.getOperation(), headers, jwePayload);
        response.put(Constants.RESPONSE_OBJ, JSONUtils.deserialize(hcxResponse.getBody(), Map.class));
        return hcxResponse.getStatus() == 202;
    }
}
