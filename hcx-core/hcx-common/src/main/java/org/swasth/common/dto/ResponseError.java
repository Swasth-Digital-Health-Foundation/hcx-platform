package org.swasth.common.dto;

import org.swasth.common.exception.ResponseCode;

public class ResponseError {
    private ResponseCode code;
    private String message;
    private Throwable trace;

    public ResponseError() {}

    public ResponseError(ResponseCode code, String message, Throwable trace) {
        this.code = code;
        this.message = message;
        this.trace = trace;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getTrace() {
        return trace;
    }

    public void setTrace(Throwable trace) {
        this.trace = trace;
    }

}
