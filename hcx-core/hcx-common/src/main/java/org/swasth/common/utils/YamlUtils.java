package org.swasth.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

public class YamlUtils {

    static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static <T> T convertYaml(InputStream input, Class<T> clazz) throws IOException {
        return mapper.readValue(input, clazz);
    }
}
