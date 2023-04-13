package org.swasth.common.exception;

public class VerificationException extends Exception {

    private ErrorCodes errCode;

    public VerificationException(String message) {
        super(message);
    }

    public VerificationException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}
