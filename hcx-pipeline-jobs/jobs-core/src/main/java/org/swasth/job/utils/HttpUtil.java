package org.swasth.job.utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Collections;

public class HttpUtil {

    public static HttpResponse get(String url) {
        return Unirest.get(url).headers(Collections.singletonMap("Content-Type","application/json")).asString();
    }

}
