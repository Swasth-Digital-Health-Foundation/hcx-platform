package org.swasth.dp.search.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public class BaseUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T decodeBase64String(String encodedString, Class<T> clazz) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return deserialize(decodedString, clazz);
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String value, Class<T> clazz) throws Exception {
        return mapper.readValue(value, clazz);
    }

    public static String encodeBase64String(Object decodedObj) throws Exception {
        String encodedString = Base64.getEncoder().encodeToString(serialize(decodedObj).getBytes());
        return encodedString;
    }


}