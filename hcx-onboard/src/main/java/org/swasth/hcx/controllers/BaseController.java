package org.swasth.hcx.controllers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.*;
import org.swasth.hcx.helpers.EventGenerator;

import static org.swasth.common.utils.Constants.FAILED;
import static org.swasth.common.utils.Constants.SUCCESSFUL;


public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected Environment environment;
    protected AuditIndexer indexer;
    protected EventGenerator generator;
    @Autowired
    public BaseController(Environment environment,AuditIndexer indexer,EventGenerator generator){
        this.environment=environment;
        this.indexer=indexer;
        this.generator=generator;
    }

    public BaseController() {

    }

    protected Response errorResponse(Response response, ErrorCodes code, Exception e) {
        response.setError(new ResponseError(code, e.getMessage(), e.getCause()));
        return response;
    }

    protected ResponseEntity<Object> getSuccessResponse(Object response) {
        ((Response) response).setStatus(SUCCESSFUL.toUpperCase());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    protected ResponseEntity<Object> exceptionHandler(String email, String action, Response response, Exception e) throws Exception {
        response.setStatus(FAILED.toUpperCase());
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((ClientException) e).getErrCode();
        } else if (e instanceof VerificationException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((VerificationException) e).getErrCode();
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
        if(StringUtils.isEmpty(email))
            indexer.createDocument(generator.getOnboardErrorEvent(email, action, new ResponseError(errorCode, e.getMessage(), e.getCause())));
        return new ResponseEntity<>(errorResponse(response, errorCode, e), status);
    }
}
