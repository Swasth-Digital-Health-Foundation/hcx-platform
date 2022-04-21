package org.swasth.apigateway.exception;

public class JWTVerificationException extends Exception {

    private ErrorCodes errCode;

    public JWTVerificationException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }

}
