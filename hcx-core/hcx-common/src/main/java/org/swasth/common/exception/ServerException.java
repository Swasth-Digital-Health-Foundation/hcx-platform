package org.swasth.common.exception;

public class ServerException extends Exception{

    private ErrorCodes errCode;

    public ServerException(String message) {
        super(message);
    }

    public ServerException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}
