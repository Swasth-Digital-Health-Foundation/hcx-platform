package io.hcxprotocol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

/**
 * The JSON Utils to convert a Java object to JSON string and vise versa.
 */

public class JSONUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T decodeBase64String(String encodedString, Class<T> clazz) throws JsonProcessingException {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return deserialize(decodedString, clazz);
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String value, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(value, clazz);
    }

}


