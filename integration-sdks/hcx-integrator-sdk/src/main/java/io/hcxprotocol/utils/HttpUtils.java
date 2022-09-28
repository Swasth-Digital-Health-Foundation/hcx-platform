package io.hcxprotocol.utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Map;

public class HttpUtils {


    public static HttpResponse<String> post(String url, Map<String,String> headers, String requestBody){
        headers.put("Content-Type","application/json");
        return Unirest.post(url).headers(headers).body(requestBody).asString();
    }

    public static HttpResponse<String> post(String url, Map<String,String> headers, Map<String,Object> fields) {
        return Unirest.post(url).headers(headers).fields(fields).asString();
    }

}
