package org.swasth.hcx.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ServiceUnavailbleException;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ServiceUnavailbleException.class)
    public ResponseEntity<Object> handleServiceUnavailableException(ServiceUnavailbleException ex) {
        return new ResponseEntity<>(new Response(new ResponseError(ex.getErrCode(), ex.getMessage(), null)), HttpStatus.ACCEPTED);
    }
}
