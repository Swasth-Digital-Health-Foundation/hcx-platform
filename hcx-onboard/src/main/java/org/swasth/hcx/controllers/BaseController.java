package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.*;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.hcx.handlers.EventHandler;

import java.util.Map;

import static org.swasth.common.utils.Constants.ERROR_STATUS;

public class BaseController {

    @Autowired
    protected Environment env;

    @Autowired
    protected AuditIndexer auditIndexer;

    @Autowired
    protected EventGenerator eventGenerator;

    @Autowired
    protected EventHandler eventHandler;

    protected Response errorResponse(Response response, ErrorCodes code, Exception e) {
        response.setError(new ResponseError(code, e.getMessage(), e.getCause()));
        return response;
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Request request, String kafkaTopic) throws Exception {
        Response response = new Response(request);
        try {
            eventHandler.processAndSendEvent(kafkaTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandlerWithAudit(request, response, e);
        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String kafkaTopic) throws Exception {
        Request request = new Request(requestBody, apiAction);
        Response response = new Response(request);
        try {
            eventHandler.processAndSendEvent(kafkaTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandlerWithAudit(request, response, e);
        }
    }

    protected ResponseEntity<Object> exceptionHandlerWithAudit(Request request, Response response, Exception e) throws Exception {
        request.setStatus(ERROR_STATUS);
        auditIndexer.createDocument(eventGenerator.generateAuditEvent(request));
        return exceptionHandler(response, e);
    }

    protected ResponseEntity<Object> exceptionHandler(Response response, Exception e){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((ClientException) e).getErrCode();
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
