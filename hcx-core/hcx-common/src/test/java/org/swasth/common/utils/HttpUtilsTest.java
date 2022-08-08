package org.swasth.common.utils;

import kong.unirest.UnirestException;
import org.junit.Test;

public class HttpUtilsTest {

    @Test(expected = UnirestException.class)
    public void test_post() {
        HttpUtils.post("localhost:8081", "{}");
    }
}
