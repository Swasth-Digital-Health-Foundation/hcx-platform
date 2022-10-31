package io.hcxprotocol.dto;

/**
 * The ResponseError class which wrap the error details and helps in creating an instance of it.
 */
public class ResponseError {

    private String code;
    private String message;
    private String trace;

    public ResponseError() {}

    public ResponseError(String code, String message, String trace) {
        this.code = code;
        this.message = message;
        this.trace = trace;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

}
