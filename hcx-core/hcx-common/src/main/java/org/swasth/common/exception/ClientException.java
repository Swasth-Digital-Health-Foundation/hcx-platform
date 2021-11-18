package org.swasth.common.exception;

public class ClientException extends Exception{

    private String errCode;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String errCode, String message) {
        super(message);
        this.errCode = errCode;
    }
}
