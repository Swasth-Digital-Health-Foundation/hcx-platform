package org.swasth.common.dto;

import org.swasth.common.exception.ErrorCodes;

public class ResponseError {
    private ErrorCodes code;
    private String message;
    private Throwable trace;

    public ResponseError() {}

    public ResponseError(ErrorCodes code, String message, Throwable trace) {
        this.code = code;
        this.message = message;
        this.trace = trace;
    }

    public ErrorCodes getCode() {
        return code;
    }

    public void setCode(ErrorCodes code) {
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