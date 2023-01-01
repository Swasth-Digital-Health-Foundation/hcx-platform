package org.swasth.common.exception;

public class OTPVerificationException extends Exception {

    private ErrorCodes errCode;

    public OTPVerificationException(String message) {
        super(message);
    }

    public OTPVerificationException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}
