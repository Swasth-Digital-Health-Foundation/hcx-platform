package org.swasth.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.helpers.ExceptionHandler;
import org.swasth.apigateway.models.Request;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.apigateway.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.swasth.apigateway.constants.Constants.*;

@Component
public class HCXValidationFilter extends AbstractGatewayFilterFactory<HCXValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(HCXValidationFilter.class);
    private final JWTVerifier jwtVerifier;

    @Autowired
    RegistryService registryService;

    @Autowired
    AuditService auditService;

    @Autowired
    ExceptionHandler exceptionHandler;

    @Autowired
    protected Environment env;

    @Value("${timestamp.range}")
    protected int timestampRange;

    public HCXValidationFilter(@Qualifier("jwk") JWTVerifier jwtVerifier) {
        super(Config.class);
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String correlationId = null;
            String apiCallId = null;
            try {
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                String path = exchange.getRequest().getPath().value();
                Request request = new Request(JSONUtils.deserialize(cachedBody.toString(), HashMap.class));
                correlationId = request.getCorrelationId();
                apiCallId = request.getApiCallId();
                request.validate(getMandatoryHeaders(), getDetails(request.getSenderCode()), getDetails(request.getRecipientCode()), getSubject(exchange), timestampRange);
                validateUsingAuditData(path, request);
            } catch (Exception e) {
                return exceptionHandler.errorResponse(e, exchange, correlationId, apiCallId);
            }
            return chain.filter(exchange);
        };
    }

    private void validateUsingAuditData(String apiAction, Request request) throws Exception {
        List<Object> auditData = getAuditData(Collections.singletonMap(CORRELATION_ID, request.getCorrelationId()));
        if(ON_ACTION_APIS.contains(apiAction)) {
            request.validateCondition(auditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, "The on_action request should contain the same correlation id as in corresponding action request");
            Map<String,Object> auditEvent = (Map<String, Object>) auditData.get(0);
            if(auditEvent.containsKey(WORKFLOW_ID)) {
                request.validateCondition(!request.getWorkflowId().equals(auditEvent.get(WORKFLOW_ID)), ErrorCodes.ERR_INVALID_WORKFLOW_ID, "he on_action request should contain the same workflow id as in corresponding action request");
            }
        } else {
            request.validateCondition(!auditData.isEmpty(), ErrorCodes.ERR_INVALID_CORRELATION_ID, "Request already exist with same correlation id");
        }
    }

    private List<String> getMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty("protocol.headers.mandatory", List.class));
        mandatoryHeaders.addAll(env.getProperty("headers.jose", List.class));
        return mandatoryHeaders;
    }

    private String getSubject(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().get(AUTHORIZATION).get(0).trim().split("\\s")[1].trim();
        DecodedJWT decodedJWT = this.jwtVerifier.verify(token);
        return decodedJWT.getSubject();
    }

    private Map<String,Object> getDetails(String code) throws Exception {
        return registryService.fetchDetails("osid", code);
    }

    private List<Object> getAuditData(Map<String,Object> filters) throws Exception {
        return auditService.getAuditLogs(filters);
    }

    public static class Config {
    }

}
