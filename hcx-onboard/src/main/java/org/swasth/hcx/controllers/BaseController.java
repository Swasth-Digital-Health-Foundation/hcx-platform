package org.swasth.hcx.controllers;

import kong.unirest.HttpResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.ParticipantResponse;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.*;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.helpers.EventGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.FAILED;
import static org.swasth.common.utils.Constants.SUCCESSFUL;


public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    protected Environment env;

    @Autowired
    protected AuditIndexer auditIndexer;

    @Autowired
    protected EventGenerator eventGenerator;

    protected Response errorResponse(Response response, ErrorCodes code, Exception e) {
        response.setError(new ResponseError(code, e.getMessage(), e.getCause()));
        return response;
    }

    protected ResponseEntity<Object> getSuccessResponse(Object response) {
        ((Response) response).setStatus(SUCCESSFUL.toUpperCase());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    protected ResponseEntity<Object> exceptionHandler(Response response, Exception e){
        logger.error("Exception: {} :: Trace: {}", e.getMessage(), ExceptionUtils.getStackTrace(e));
        response.setStatus(FAILED.toUpperCase());
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((ClientException) e).getErrCode();
        } else if (e instanceof OTPVerificationException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((OTPVerificationException) e).getErrCode();
        } else if (e instanceof ServiceUnavailbleException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = ((ServiceUnavailbleException) e).getErrCode();
        } else if (e instanceof ServerException) {
            errorCode = ((ServerException) e).getErrCode();
        } else if (e instanceof AuthorizationException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (e instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(errorResponse(response, errorCode, e), status);
    }


}
