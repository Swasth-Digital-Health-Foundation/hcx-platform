package org.swasth.job.utils;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class StringUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String serialize(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String value, Class<T> clazz) throws Exception {
        return mapper.readValue(value, clazz);
    }

}
