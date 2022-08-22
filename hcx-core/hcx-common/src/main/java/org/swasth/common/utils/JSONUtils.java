package org.swasth.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

@UtilityClass
public class JSONUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String encodeBase64String(String input){
        return  Base64.getEncoder().encodeToString(input.getBytes());
    }

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

    public static <T> T convert(Object obj, Class<T> clazz) {
        return mapper.convertValue(obj, clazz);
    }

    public static <T> T convertJson(InputStream input, Class<T> clazz) throws IOException {
        return mapper.readValue(input, clazz);
    }

    public static byte[] convertToByte(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsBytes(obj);
    }

}
