package com.oss.apigateway.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private long timestamp = System.currentTimeMillis();
    @JsonProperty("correlation_id")
    private String correlationId;
    private ResponseError error;

    public Response(String correlationId) {
        this.correlationId = correlationId;
    }

    public Response(String correlationId, ResponseError error) {
        this.correlationId = correlationId;
        this.error = error;
    }

}
