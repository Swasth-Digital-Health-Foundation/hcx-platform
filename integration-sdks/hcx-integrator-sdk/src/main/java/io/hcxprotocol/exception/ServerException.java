package io.hcxprotocol.exception;

/**
 * The exception to capture the unknown errors.
 */
public class ServerException extends Exception{

    public ServerException(String message) {
        super(message);
    }
}
