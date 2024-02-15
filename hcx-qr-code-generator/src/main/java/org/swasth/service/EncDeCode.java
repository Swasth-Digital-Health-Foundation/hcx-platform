package org.swasth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.swasth.common.utils.JSONUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import static org.swasth.common.utils.JSONUtils.deserialize;
import static org.swasth.common.utils.JSONUtils.serialize;

public class EncDeCode {

    public static String encodePayload(Map<String, Object> payload) throws JsonProcessingException {
        String base64EncodedSignature = Base64.getEncoder().encodeToString(serialize(payload).getBytes());
        return base64EncodedSignature;
    }

    public static Map<String, Object> decodePayload(String payload) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(payload);
        String decodedString = new String(decodedBytes);
        Map<String, Object> decode = deserialize(decodedString, Map.class);
        return decode;
    }

}
