package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private long timestamp = System.currentTimeMillis();
    @JsonProperty("workflow_id")
    private String workflowId;
    @JsonProperty("request_id")
    private String requestId;
    private ResponseError error;
    private Map<String, Object> result;

    public Response() {}

    public Response(String workflowId, String requestId) {
        this.workflowId = workflowId;
        this.requestId = requestId;
    }

    public Response(String key, Object val) {
        this.result = new HashMap<>();
        this.put(key, val);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Object get(String key) {
        return result.get(key);
    }

    public Response put(String key, Object vo) {
        result.put(key, vo);
        return this;
    }

    public Response putAll(Map<String, Object> resultMap) {
        result.putAll(resultMap);
        return this;
    }

}

