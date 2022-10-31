package org.swasth.apigateway.exception;

public class ClientException extends Exception {

    private ErrorCodes errCode;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }

}
