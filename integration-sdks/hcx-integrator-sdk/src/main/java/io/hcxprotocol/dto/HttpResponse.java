package io.hcxprotocol.dto;

/**
 * The HttpResponse class to capture the REST API status code and response body.
 */
public class HttpResponse {

    public int status;
    public String body;

    public HttpResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus(){
        return this.status;
    }

    public String getBody(){
        return this.body;
    }
}
