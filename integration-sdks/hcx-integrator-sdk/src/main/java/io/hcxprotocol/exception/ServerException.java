package io.hcxprotocol.exception;

/**
 * The exception to capture the unknown exception.
 */
public class ServerException extends Exception{

    public ServerException(String message) {
        super(message);
    }
}
