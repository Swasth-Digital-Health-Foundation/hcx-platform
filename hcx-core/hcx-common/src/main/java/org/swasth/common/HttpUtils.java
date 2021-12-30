package org.swasth.common;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.net.http.HttpHeaders;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static HttpResponse get(String url) {
        return Unirest.get(url).headers(Collections.singletonMap("Content-Type","application/json")).asString();
    }

    public static HttpResponse post(String url, String requestBody){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        headers.put("Authorization","bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE2NDEwNDIxMzUsImlhdCI6MTY0MDE3ODEzNSwianRpIjoiMmUxN2IyMTYtZTQ1ZC00NTRhLTgzNDctYTViNDVkZmMwMDZjIiwiaXNzIjoiaHR0cDovL2FlZjgxMDFjNDMyZDA0YTY1OWU2MzE3YjNlNTAzMWNmLTE2NzQ1ODYwNjguYXAtc291dGgtMS5lbGIuYW1hem9uYXdzLmNvbTo4MDgwL2F1dGgvcmVhbG1zL3N1bmJpcmQtcmMiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNjExNDZlYTctNTdhZS00MDQyLThhM2MtOTA3YjU2ZTlhNTYxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiZDE0Yzc0ZDYtYzZmNi00MjUxLWI1OTgtNjBiYjQxYTdmMjYwIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2xvY2FsaG9zdDo0MjAyIiwiaHR0cDovL2xvY2FsaG9zdDo0MjAyIiwiaHR0cHM6Ly9sb2NhbGhvc3Q6NDIwMCIsImh0dHBzOi8vbmRlYXIueGl2LmluIiwiaHR0cDovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cDovL25kZWFyLnhpdi5pbiIsImh0dHA6Ly8yMC4xOTguNjQuMTI4Il0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW5kZWFyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbl91c2VyIn0.botyuHXIg62lchvSrVZGTmxiMdun7pLs5WklB-tS_EgprvkAKDU_hknpQO-V5clF2YBUxVywWFfmvtQsWeUk88V3MosgUa27JD8Tcvqc6XSgmjb7AZRj-vPDKnZeXm_pw5eP-Qu4gG_RdBPHGjm_TgLqTgAGuTgU7m4JJScfYx4sMMxCYDYfo5mPrfd1hs3htOl5P49fonxPQtAcdr7qp5BwuHpHFq_M-DkF89xRsfuH5ATOcdgiXuGJp6kczex2VAAvCTG49YJSoFTMQQFW3bqdzp17XWRuReTyl_e0duzvxoeleza4h6ufYy15LjKYcJR4Wtql_OK3CrhgN69OEQ");
        return Unirest.post(url).headers(headers).body(requestBody).asString();
    }

    public static HttpResponse put(String url, String requestBody, Map<String,String> headers){
        headers.put("Content-Type","application/json");
        return Unirest.put(url).headers(headers).body(requestBody).asString();
    }

}
