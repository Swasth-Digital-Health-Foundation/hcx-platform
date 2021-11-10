package org.swasth.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.HashMap;

public class StringUtils {

    public static HashMap decodeBase64String(String encodedString) throws JsonProcessingException {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return new ObjectMapper().readValue(decodedString, HashMap.class);
    }
}
