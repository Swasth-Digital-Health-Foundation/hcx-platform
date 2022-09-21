package org.swasth.apigateway.handlers;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.JWTVerificationException;
import org.swasth.apigateway.exception.ServerException;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.apigateway.models.Response;
import org.swasth.apigateway.models.ResponseError;
import org.swasth.apigateway.service.AuditService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Map;

import static org.swasth.common.response.ResponseMessage.AUDIT_LOG_MSG;

@Component
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Autowired
    private AuditService auditService;

    public Mono<Void> errorResponse(Exception e, ServerWebExchange exchange, String correlationId, String apiCallId, BaseRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCodes errorCode = ErrorCodes.INTERNAL_SERVER_ERROR;
        Exception ex = e;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = ((ClientException) e).getErrCode();
        } else if (e instanceof ServerException) {
            errorCode = ((ServerException) e).getErrCode();
        } else if (e instanceof JWTVerificationException) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = ((JWTVerificationException) e).getErrCode();
        } else if (e instanceof TokenExpiredException) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = ErrorCodes.ERR_ACCESS_DENIED;
        }
        try {
            if (request.getApiAction() != null && Constants.ALLOWED_ENTITIES_ERROR_AUDIT_CREATION.contains(request.getEntity(request.getApiAction()))) {
                request.setErrorDetails(JSONUtils.deserialize(JSONUtils.serialize(new ResponseError(errorCode, e.getMessage(), e.getCause())), Map.class));
                auditService.createAuditLog(request);
            }
        } catch (Exception exception) {
            logger.error(MessageFormat.format(AUDIT_LOG_MSG, exception.getMessage()));
        }
        return this.onError(exchange, status, correlationId, apiCallId, errorCode, ex);
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
