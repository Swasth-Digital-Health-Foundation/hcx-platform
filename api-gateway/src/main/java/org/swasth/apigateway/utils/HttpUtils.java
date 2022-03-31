package org.swasth.apigateway.utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Collections;

public class HttpUtils {

    public static HttpResponse post(String url, String requestBody){
        return Unirest.post(url).headers(Collections.singletonMap("Content-Type","application/json")).body(requestBody).asString();
    }

}
