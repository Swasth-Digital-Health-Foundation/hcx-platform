package org.swasth.common.utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Collections;
import java.util.Map;

public class HttpUtils {

    public static HttpResponse get(String url) {
        return Unirest.get(url).headers(Collections.singletonMap("Content-Type","application/json")).asString();
    }

    public static HttpResponse post(String url, String requestBody, Map<String,String> headers){
        headers.put("Content-Type","application/json");
        return Unirest.post(url).headers(headers).body(requestBody).asString();
    }

    public static HttpResponse put(String url, String requestBody, Map<String,String> headers){
        headers.put("Content-Type","application/json");
        return Unirest.put(url).headers(headers).body(requestBody).asString();
    }

}
