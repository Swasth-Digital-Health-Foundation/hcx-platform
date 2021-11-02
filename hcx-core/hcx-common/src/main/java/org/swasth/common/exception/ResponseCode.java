package org.swasth.common.exception;

public enum ResponseCode {

    OK(200), ACCEPTED(202), CLIENT_ERROR(400), SERVER_ERROR(500), RESOURCE_NOT_FOUND(404), PARTIAL_SUCCESS(207);

    private int code;

    private ResponseCode(int code) {
        this.code = code;
    }

}
