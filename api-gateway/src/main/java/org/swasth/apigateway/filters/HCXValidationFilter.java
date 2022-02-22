package org.swasth.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.swasth.apigateway.exception.ClientException;
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
                Request request = new Request(JSONUtils.deserialize(cachedBody.toString(), HashMap.class));
                correlationId = request.getCorrelationId();
                apiCallId = request.getApiCallId();
                Map<String,Object> senderDetails = getDetails(request.getSenderCode());
                Map<String,Object> recipientDetails = getDetails(request.getRecipientCode());
                request.validate(getMandatoryHeaders(), senderDetails, recipientDetails, getSubject(exchange), timestampRange);
                validateUsingAuditData(request, senderDetails, recipientDetails, path);
            } catch (Exception e) {
                return exceptionHandler.errorResponse(e, exchange, correlationId, apiCallId);
            }
            return chain.filter(exchange);
        };
    }

    private void validateUsingAuditData(Request request, Map<String,Object> senderDetails, Map<String,Object> recipientDetails, String path) throws Exception {
        if(!getAuditData(Collections.singletonMap(API_CALL_ID, request.getApiCallId())).isEmpty()){
            throw new ClientException(ErrorCodes.ERR_INVALID_API_CALL_ID, "Request exist with same api call id");
        }
        List<Map<String,Object>> auditData = getAuditData(Collections.singletonMap(CORRELATION_ID, request.getCorrelationId()));
        // validate request cycle is not closed
        for(Map<String,Object> audit: auditData){
            String action = (String) audit.get(ACTION);
            String entity = getEntity(action);
            if(!OPERATIONAL_ENTITIES.contains(entity) && action.contains("on_") && ((List<String>) audit.get(RECIPIENT_ROLE)).contains(PROVIDER) && audit.get(STATUS).equals(STATUS_COMPLETE)){
                throw new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID, "Invalid request, cycle is closed for correlation id");
            }
        }
        if(path.contains("on_")) {
            Map<String,String> filters = new HashMap<>();
            filters.put(SENDER_CODE, request.getRecipientCode());
            filters.put(RECIPIENT_CODE, request.getSenderCode());
            filters.put(CORRELATION_ID, request.getCorrelationId());
            List<Map<String,Object>> onActionAuditData = getAuditData(filters);
            if (auditData.isEmpty()) {
                throw new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID, "Invalid on_action request, corresponding action request does not exist");
            }
            validateWorkflowId(request, onActionAuditData.get(0));
        }
        List<String> senderRoles = (List<String>) senderDetails.get(ROLES);
        List<String> recipientRoles = (List<String>) recipientDetails.get(ROLES);
        // forward flow validations
        if(isForwardRequest(senderRoles, recipientRoles, auditData)){
            if(!allowedEntitiesForForward.contains(getEntity(path))){
                throw new ClientException(ErrorCodes.ERR_INVALID_FORWARD_REQ, "Entity is not allowed for forwarding");
            }
            validateWorkflowId(request, auditData.get(0));
            if(!path.contains("on_")){
                for (Map<String, Object> audit : auditData) {
                    if (request.getRecipientCode().equals(audit.get(SENDER_CODE))) {
                        throw new ClientException(ErrorCodes.ERR_INVALID_FORWARD_REQ, "Request cannot be forwarded to the forward initiators");
                    }
                }
            }
        } else if(!path.contains("on_") && checkParticipantRole(senderRoles) && recipientRoles.contains(PROVIDER)) {
            throw new ClientException("Invalid recipient");
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

    private List<Map<String, Object>> getAuditData(Map<String,String> filters) throws Exception {
        return auditService.getAuditLogs(filters);
    }

    public boolean isForwardRequest(List<String> senderRoles, List<String> recipientRoles, List<Map<String,Object>> auditData) throws ClientException {
        if(checkParticipantRole(senderRoles) && checkParticipantRole(recipientRoles)){
            if(!auditData.isEmpty())
                return true;
            else
                throw new ClientException(ErrorCodes.ERR_INVALID_FORWARD_REQ, "The request contains invalid correlation id");
        }
        return false;
    }

    private void validateWorkflowId(Request request, Map<String, Object> auditEvent) throws ClientException {
        if (auditEvent.containsKey(WORKFLOW_ID)) {
            if (!request.getHcxHeaders().containsKey(WORKFLOW_ID) || !request.getWorkflowId().equals(auditEvent.get(WORKFLOW_ID))) {
                throw new ClientException(ErrorCodes.ERR_INVALID_WORKFLOW_ID, "The request contains invalid workflow id");
            }
        }
    }

    private boolean checkParticipantRole(List<String> roles){
        for(String role: roles){
            if (allowedRolesForForward.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public String getEntity(String path){
        if (path.contains("status")) {
            return "status";
        } else if (path.contains("search")) {
            return "search";
        } else {
            String[] str = path.split("/");
            return str[str.length-2];
        }
    }

    public static class Config {
    }

}
