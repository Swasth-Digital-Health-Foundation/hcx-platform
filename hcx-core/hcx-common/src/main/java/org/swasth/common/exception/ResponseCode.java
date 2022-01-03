package org.swasth.common.exception;

public enum ResponseCode {

    CLIENT_ERROR(400), SERVER_ERROR(500);

    private int code;

    private ResponseCode(int code) {
        this.code = code;
    }

}
