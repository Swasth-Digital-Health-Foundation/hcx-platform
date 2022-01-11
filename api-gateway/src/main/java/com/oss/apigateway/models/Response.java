package com.oss.apigateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Response {

    private long timestamp = System.currentTimeMillis();
    @JsonProperty("workflow_id")
    private String workflowId;
    @JsonProperty("request_id")
    private String requestId;
    private ResponseError error;

    public Response(ResponseError error) {
        this.error = error;
    }

    public Response(String workflowId, String requestId, ResponseError error) {
        this.workflowId = workflowId;
        this.requestId = requestId;
        this.error = error;
    }

}
