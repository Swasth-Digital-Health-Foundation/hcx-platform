package org.swasth.apigateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Response {

    private long timestamp = System.currentTimeMillis();
    @JsonProperty("correlation_id")
    private String correlationId;
    @JsonProperty("api_call_id")
    private String apiCallId;
    private ResponseError error;

    public Response(String correlationId, String apiCallId, ResponseError error) {
        this.correlationId = correlationId;
        this.apiCallId = apiCallId;
        this.error = error;
    }

}
