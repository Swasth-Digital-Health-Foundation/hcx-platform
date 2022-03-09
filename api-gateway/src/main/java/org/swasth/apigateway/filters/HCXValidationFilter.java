package org.swasth.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.helpers.ExceptionHandler;
import org.swasth.apigateway.models.JSONRequest;
import org.swasth.apigateway.models.JWERequest;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.apigateway.utils.JSONUtils;

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
    private Environment env;

    @Value("${timestamp.range}")
    private int timestampRange;

    @Value("${allowedEntitiesForForward}")
    private List<String> allowedEntitiesForForward;

    @Value("${allowedRolesForForward}")
    private List<String> allowedRolesForForward;

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
                Map<String, Object> requestBody = JSONUtils.deserialize(cachedBody.toString(), HashMap.class);
                if (requestBody.containsKey(PAYLOAD)) {
                    JWERequest jweRequest = new JWERequest(requestBody, false, path);
                    correlationId = jweRequest.getCorrelationId();
                    apiCallId = jweRequest.getApiCallId();
                    Map<String,Object> senderDetails = getDetails(jweRequest.getSenderCode());
                    Map<String,Object> recipientDetails = getDetails(jweRequest.getRecipientCode());
                    jweRequest.validate(getMandatoryHeaders(), getSubject(exchange), timestampRange, senderDetails, recipientDetails);
                    jweRequest.validateUsingAuditData(allowedEntitiesForForward, allowedRolesForForward, senderDetails, recipientDetails, getCorrelationAuditData(jweRequest.getCorrelationId()), getCallAuditData(jweRequest.getApiCallId()), getParticipantCtxAuditData(jweRequest.getSenderCode(), jweRequest.getRecipientCode(), jweRequest.getCorrelationId()), path);
                } else {
                    JSONRequest jsonRequest = new JSONRequest(requestBody, true, path);
                    correlationId = jsonRequest.getCorrelationId();
                    apiCallId = jsonRequest.getApiCallId();
                    if(ERROR_RESPONSE.equalsIgnoreCase(jsonRequest.getStatus())) {
                        jsonRequest.validate(getErrorMandatoryHeaders(), getSubject(exchange), timestampRange, getDetails(jsonRequest.getSenderCode()), getDetails(jsonRequest.getRecipientCode()));
                    } else {
                        jsonRequest.validate(getRedirectMandatoryHeaders(), getSubject(exchange), timestampRange, getDetails(jsonRequest.getSenderCode()), getDetails(jsonRequest.getRecipientCode()));
                        if (getApisForRedirect().contains(path)) {
                            if (REDIRECT_STATUS.equalsIgnoreCase(jsonRequest.getStatus()))
                                jsonRequest.validateRedirect(getRolesForRedirect(), getDetails(jsonRequest.getRedirectTo()), getCallAuditData(jsonRequest.getApiCallId()), getCorrelationAuditData(jsonRequest.getCorrelationId()));
                            else
                                throw new ClientException(ErrorCodes.ERR_INVALID_REDIRECT_TO, "Invalid redirect request," + jsonRequest.getStatus() + " status is not allowed for redirect, Allowed status is " + REDIRECT_STATUS);
                        } else
                            throw new ClientException(ErrorCodes.ERR_INVALID_REDIRECT_TO,"Invalid redirect request," + jsonRequest.getApiAction() + " is not allowed for redirect, Allowed APIs are: ");
                    }
                }
            } catch (Exception e) {
                logger.error("Exception occurred for request with correlationId: " + correlationId);
                return exceptionHandler.errorResponse(e, exchange, correlationId, apiCallId);
            }
            return chain.filter(exchange);
        };
    }

    private List<Map<String, Object>> getCallAuditData(String apiCallId) throws Exception {
        return getAuditData(Collections.singletonMap(API_CALL_ID, apiCallId));
    }

    private List<Map<String, Object>> getCorrelationAuditData(String correlationId) throws Exception {
        return getAuditData(Collections.singletonMap(CORRELATION_ID, correlationId));
    }

    private List<Map<String, Object>> getParticipantCtxAuditData(String senderCode, String recipientCode, String correlationId) throws Exception {
        Map<String,String> filters = new HashMap<>();
        filters.put(SENDER_CODE, recipientCode);
        filters.put(RECIPIENT_CODE, senderCode);
        filters.put(CORRELATION_ID, correlationId);
        return getAuditData(filters);
    }

    private List<String> getMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty("protocol.headers.mandatory", List.class));
        mandatoryHeaders.addAll(env.getProperty("headers.jose", List.class));
        return mandatoryHeaders;
    }

    private List<String> getErrorMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty("plainrequest.headers.mandatory", List.class));
        return mandatoryHeaders;
    }

    private String getSubject(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().get(AUTHORIZATION).get(0).trim().split("\\s")[1].trim();
        DecodedJWT decodedJWT = this.jwtVerifier.verify(token);
        return decodedJWT.getSubject();
    }

    private Map<String, Object> getDetails(String code) throws Exception {
        return registryService.fetchDetails("osid", code);
    }

    private List<Map<String, Object>> getAuditData(Map<String,String> filters) throws Exception {
        return auditService.getAuditLogs(filters);
    }

    public static class Config {
    }

    private List<String> getRedirectMandatoryHeaders() {
        List<String> plainMandatoryHeaders = new ArrayList<>();
        plainMandatoryHeaders.addAll(env.getProperty("redirect.headers.mandatory", List.class));
        return plainMandatoryHeaders;
    }

    private List<String> getApisForRedirect() {
        List<String> allowedApis = new ArrayList<>();
        allowedApis.addAll(env.getProperty("redirect.apis", List.class));
        return allowedApis;
    }

    private List<String> getRolesForRedirect() {
        List<String> allowedRoles = new ArrayList<>();
        allowedRoles.addAll(env.getProperty("redirect.roles", List.class));
        return allowedRoles;
    }

}
