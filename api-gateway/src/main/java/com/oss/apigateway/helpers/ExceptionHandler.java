package com.oss.apigateway.helpers;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.exception.JWTVerificationException;
import com.oss.apigateway.exception.ServerException;
import com.oss.apigateway.filters.JwtAuthenticationFilter;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public Mono<Void> errorResponse(Exception e, ServerWebExchange exchange, String correlationId, String apiCallId){
        if (e instanceof ClientException) {
            return this.onError(exchange, HttpStatus.BAD_REQUEST, correlationId, apiCallId, ((ClientException) e).getErrCode(), e);
        } else if (e instanceof ServerException) {
            return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, correlationId, apiCallId, ((ServerException) e).getErrCode(), e);
        } else if (e instanceof JWTVerificationException) {
            return this.onError(exchange, HttpStatus.UNAUTHORIZED, correlationId, apiCallId, ((JWTVerificationException) e).getErrCode(), e);
        } else if (e instanceof TokenExpiredException) {
            return this.onError(exchange, HttpStatus.UNAUTHORIZED, correlationId, apiCallId, ErrorCodes.CLIENT_ERR_ACCESS_DENIED, e);
        } else {
            return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, correlationId, apiCallId, null, e);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String correlationId, String apiCallId, ErrorCodes code, Exception e) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        Response resp = new Response(correlationId, apiCallId, new ResponseError(code, e.getMessage(), e.getCause()));
        try {
            byte[] obj = JSONUtils.convertToByte(resp);
            response.setStatusCode(status);
            response.getHeaders().add("Content-Type", "application/json");
            return response.writeWith(Mono.just(obj).map(r -> dataBufferFactory.wrap(r)));
        } catch (JsonProcessingException ex) {
            logger.error(ex.toString());
        }
        return response.setComplete();
    }

}
