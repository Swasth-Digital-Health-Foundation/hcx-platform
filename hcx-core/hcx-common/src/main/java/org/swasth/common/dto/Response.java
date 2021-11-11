package org.swasth.common.dto;

import org.swasth.common.exception.ResponseCode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private String timestamp;
    private String correlation_id;
    private ResponseParams params;

    public Response() {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    }

    public Response(ResponseParams responseParams) {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
        this.params = responseParams;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlation_id;
    }

    public void setCorrelationId(String correlation_id) {
        this.correlation_id = correlation_id;
    }

    public ResponseParams getParams() {
        return params;
    }

    public void setParams(ResponseParams params) {
        this.params = params;
    }

}

