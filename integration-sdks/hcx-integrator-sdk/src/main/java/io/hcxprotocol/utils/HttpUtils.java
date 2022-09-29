package io.hcxprotocol.utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Map;

/**
 * The REST API Util using Unirest library.
 */

public class HttpUtils {


    public static io.hcxprotocol.dto.HttpResponse post(String url, Map<String,String> headers, String requestBody){
        headers.put("Content-Type","application/json");
        HttpResponse<String> response = Unirest.post(url).headers(headers).body(requestBody).asString();
        return new io.hcxprotocol.dto.HttpResponse(response.getStatus(), response.getBody());
    }

    public static io.hcxprotocol.dto.HttpResponse post(String url, Map<String,String> headers, Map<String,Object> fields) {
        HttpResponse<String> response =  Unirest.post(url).headers(headers).fields(fields).asString();
        return new io.hcxprotocol.dto.HttpResponse(response.getStatus(), response.getBody());
    }

}
