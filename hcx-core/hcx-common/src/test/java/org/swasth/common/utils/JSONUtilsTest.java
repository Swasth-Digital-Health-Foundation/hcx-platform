package org.swasth.common.utils;

import org.junit.Test;
import org.swasth.common.dto.User;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
public class JSONUtilsTest {


    @Test
    public void testDeserialize() {
        Object value = Map.of("mobile","1234567890","email","test@gmail.com");
        User result = JSONUtils.deserialize(value, User.class);
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("1234567890",result.getMobile());
    }

}
