package com.oss.apigateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {

    private long timestamp = System.currentTimeMillis();
    @JsonProperty("correlation_id")
    private String correlationId;
    private ResponseError error;


    public Response() {}

    public Response(String correlationId) {
        this.correlationId = correlationId;
    }

    public Response(String correlationId, ResponseError error) {
        this.correlationId = correlationId;
        this.error = error;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }

}
