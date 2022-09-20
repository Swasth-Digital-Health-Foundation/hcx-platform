package org.swasth.common.response;

public enum ResponseCode {
    //Response Codes
    OK(200), CLIENT_ERROR(400), UNAUTHORIZED(401), RESOURCE_NOT_FOUND(404), SERVER_ERROR(500), SERVICE_UNAVAILABLE(503);
    private int responseCode;

    ResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

}
