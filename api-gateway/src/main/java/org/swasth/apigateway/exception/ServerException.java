package org.swasth.apigateway.exception;

public class ServerException extends Exception{

    private ErrorCodes errCode;

    public ServerException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}
