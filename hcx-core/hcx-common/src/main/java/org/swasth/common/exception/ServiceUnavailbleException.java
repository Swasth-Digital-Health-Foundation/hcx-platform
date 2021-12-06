package org.swasth.common.exception;

import javax.naming.ServiceUnavailableException;

public class ServiceUnavailbleException extends ServiceUnavailableException {

    private ErrorCodes errCode;

    public ServiceUnavailbleException(String message) {
        super(message);
    }

    public ServiceUnavailbleException(ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}
