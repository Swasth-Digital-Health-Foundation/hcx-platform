package io.hcxprotocol.functions;

import io.hcxprotocol.dto.HCXIntegrator;
import io.hcxprotocol.interfaces.OutgoingInterface;
import io.hcxprotocol.utils.Constants;
import io.hcxprotocol.utils.HttpUtils;
import io.hcxprotocol.utils.JSONUtils;
import io.hcxprotocol.utils.Utils;
import kong.unirest.HttpResponse;
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

public class Outgoing implements OutgoingInterface {

    private final HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance();

    /**
     * This method is to process the outgoing requests.
     *
     * @param fhirPayload
     * @param operation
     * @param recipientCode
     * @param actionJwe
     * @param onActionStatus
     * @param output
     * @return
     * @throws Exception
     */
    @Override
    public boolean processFunction(String fhirPayload, String operation, String recipientCode, String actionJwe, String onActionStatus, Map<String,Object> output) throws Exception {
        Map<String, Object> error = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        boolean result = false;
        if (!validatePayload(fhirPayload, operation, error)) {
            output.putAll(error);
        } else if (!createHeader(recipientCode, actionJwe, onActionStatus, headers)) {
            output.putAll(error);
        } else if (!encryptPayload(headers, fhirPayload, output)) {
            output.putAll(error);
        } else {
            result = initializeHCXCall(JSONUtils.serialize(output), response);
            output.putAll(response);
        }
        return result;
    }

    @Override
    public boolean validatePayload(String fhirPayload, String operation, Map<String, Object> error) {
        return true;
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
    public boolean encryptPayload(Map<String, Object> headers, Object payload, Map<String,Object> output) {
        try {
            String publicKeyUrl = (String) Utils.searchRegistry(headers.get(Constants.HCX_RECIPIENT_CODE)).get(Constants.ENCRYPTION_CERT);
            String certificate = IOUtils.toString(new URL(publicKeyUrl), StandardCharsets.UTF_8.toString());
            InputStream stream = new ByteArrayInputStream(certificate.getBytes());
            Reader fileReader = new InputStreamReader(stream);
            RSAPublicKey rsaPublicKey = PublicKeyLoader.loadPublicKeyFromX509Certificate(fileReader);
            JweRequest jweRequest = new JweRequest(headers, (Map<String, Object>) payload);
            jweRequest.encryptRequest(rsaPublicKey);
            output.put(Constants.PAYLOAD, jweRequest.getEncryptedObject());
            return true;
        } catch (Exception e) {
            output.put(Constants.ERROR, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean initializeHCXCall(String jwePayload, Map<String,Object> response) throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put(Constants.AUTHORIZATION, "Bearer " + Utils.generateToken());
        HttpResponse<String> hcxResponse = HttpUtils.post(hcxIntegrator.getHCXBaseUrl(), headers, jwePayload);
        response.put(Constants.RESPONSE_OBJ, JSONUtils.deserialize(hcxResponse.getBody(), Map.class));
        return hcxResponse.getStatus() == 202;
    }
}
