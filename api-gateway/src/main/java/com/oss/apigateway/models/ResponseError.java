package com.oss.apigateway.models;

import com.oss.apigateway.exception.ErrorCodes;
import lombok.Data;

@Data
public class ResponseError {

    private final ErrorCodes code;
    private final String message;
    private final Throwable trace;

}
