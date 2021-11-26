package org.swasth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private long timestamp;
    @JsonProperty("correlation_id")
    private String correlationId;
    private ResponseError error;
    private Map<String, Object> result;

    public Response() {
        this.timestamp = System.currentTimeMillis();
    }

    public Response(String correlationId) {
        this.timestamp = System.currentTimeMillis();
        this.correlationId = correlationId;
    }

    public Response(String correlationId, ResponseError error) {
        this.timestamp = System.currentTimeMillis();
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

