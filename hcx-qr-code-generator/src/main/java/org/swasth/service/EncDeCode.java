package org.swasth.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

import static org.swasth.common.utils.JSONUtils.deserialize;

public class EncDeCode {
    private static final Logger logger = Logger.getLogger(String.valueOf(EncDeCode.class));
    public static String encodePayload(String payload) {
        String base64EncodedSignature = Base64.getEncoder().encodeToString(payload.getBytes());
        logger.info("Encoded Payload");
        return base64EncodedSignature;
    }

    public static Map<String,Object> decodePayload(String payload) throws JsonProcessingException {
        byte[] decodedBytes = Base64.getDecoder().decode(payload);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        return deserialize(decodedString, Map.class);
    }

}
