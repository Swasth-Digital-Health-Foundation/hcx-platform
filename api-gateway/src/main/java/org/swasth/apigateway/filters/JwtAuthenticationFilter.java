package org.swasth.apigateway.filters;

import com.auth0.jwk.JwkException;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.swasth.apigateway.constants.FilterOrder;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.JWTVerificationException;
import org.swasth.apigateway.handlers.ExceptionHandler;
import org.swasth.apigateway.models.Acl;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.apigateway.security.JWTVerifierFactory;
import org.swasth.apigateway.security.JwtConfigs;
import org.swasth.apigateway.service.AuthorizationService;
import org.swasth.apigateway.utils.Utils;
import org.swasth.common.utils.JSONUtils;

import net.minidev.json.JSONArray;

import org.apache.logging.log4j.message.ReusableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

/**
 * Authenticates the user by extracting the token from the header and validates the JWT
 * based on the config provided. If the jwt auth is disabled this filter is also disabled.
 * <p>
 * 1. Checks if authentication to be carried out based on attribute set by {@link PreAuthenticationFilter}
 * 2. If auth required, JWT validation is provided by {@link org.swasth.apigateway.security.JWTVerifierFactory}
 * 3. Post authentication, authorization checks are carried out for AUTHENTICATED Roles
 * 4. Detailed authorization to be carried out by implementations of {@link AuthorizationService}
 * 5. If authorization / authentication fails, reason is sent out in the WWW-Authenticate header
 */
@Component
@ConditionalOnBean(JwtConfigs.class)
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtConfigs jwtConfigs;
    private final AuthorizationService authorizationService;
    private final Acl authenticatedAllowedPaths;
    private Map<String,JWTVerifier> verifierCache = new HashMap<>();

    @Value("${allowedUserRolesForProtocolApiAccess}")
    private List<String> allowedUserRolesForProtocolApiAccess;

    @Autowired
    ExceptionHandler exceptionHandler;

    @Autowired
    JWTVerifierFactory jwtVerifierFactory;

    public JwtAuthenticationFilter(JwtConfigs jwtConfigs,
                                   AuthorizationService authorizationService,
                                   Map<String, Acl> aclMap) {
        this.jwtConfigs = jwtConfigs;
        this.authorizationService = authorizationService;
        this.authenticatedAllowedPaths = aclMap.get("AUTHENTICATED");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (exchange.getAttributes().containsKey(AUTH_REQUIRED) && (Boolean) exchange.getAttributes().get(
                AUTH_REQUIRED)) {
            try {
                String token = this.extractJWTToken(exchange.getRequest());
                String path = exchange.getRequest().getPath().value();
                String entityType = getEntityType(token);
                DecodedJWT decodedJWT = getJWTVerifier(entityType).verify(token);

                ServerHttpRequest request = exchange.getRequest().mutate().
                        header(X_JWT_SUB_HEADER, decodedJWT.getSubject()).
                        build();

                if (!authenticatedAllowedPaths.getPaths().contains(path) && !Utils.containsRegexPath(authenticatedAllowedPaths.getRegexPaths(), path)) {
                    String payload = new String(Base64.getDecoder().decode(decodedJWT.getPayload()));
                    JSONArray claims;
                    if(StringUtils.endsWithIgnoreCase(entityType, Constants.API_ACCESS)) {
                        List<String> userRoles = JsonPath.read(payload, jwtConfigs.getApiAccessUserClaimsNamespacePath());
                        if(!validateRoles(allowedUserRolesForProtocolApiAccess, userRoles)){
                            throw new JWTVerificationException(ErrorCodes.ERR_ACCESS_DENIED, MessageFormat.format(USER_ROLE_ACCESS_DENIED_MSG, allowedUserRolesForProtocolApiAccess));
                        }
                       claims = JsonPath.read(payload, jwtConfigs.getApiAccessParticipantClaimsNamespacePath());
                    } else {
                        claims = JsonPath.read(payload, jwtConfigs.getClaimsNamespacePath());
                    }
                    if (!authorizationService.isAuthorized(exchange, claims, entityType)) {
                        throw new JWTVerificationException(ErrorCodes.ERR_ACCESS_DENIED, ACCESS_DENIED_MSG);
                    } 
                }

                return chain.filter(exchange.mutate().request(request).build());

            } catch (Exception e) {
                return exceptionHandler.errorResponse(e, exchange, null, null, new BaseRequest());
            }
        } else {
            return chain.filter(exchange);
        }
    }

    private JWTVerifier getJWTVerifier(String entityType) throws InvalidKeySpecException, NoSuchAlgorithmException, JwkException, IOException, ClientException{
        if (verifierCache.containsKey(entityType)) {
            return verifierCache.get(entityType);
        } else {
            String realm = getEntityRealm().get(entityType);
            String jwkUrl = jwtConfigs.getJwkUrl().replace("realm-name", realm);
            JWTVerifier jwtVerifier = jwtVerifierFactory.create(jwkUrl);
            verifierCache.put(jwkUrl, jwtVerifier);
            return jwtVerifier;
        }
    }

    private Map<String,String> getEntityRealm() {
        Map<String,String> entityRealm = new HashMap<>();
        for (String str : jwtConfigs.getEntityRealm()) {
            String[] parts = str.split("@");
            if (parts.length == 2) {
                entityRealm.put(parts[0], parts[1]);
            }
        }
        return entityRealm;
    }

    private String getEntityType(String token) throws ClientException{
        String entityType = "";
        try{
            entityType =  ((List<String>) JSONUtils.decodeBase64String(token.split("\\.")[1], Map.class).get("entity")).get(0);
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "Invalid authorization token");
        }
        return entityType;
    }

    @Override
    public int getOrder() {
        return FilterOrder.AUTH_FILTER.getOrder();
    }

    private String extractJWTToken(ServerHttpRequest request) throws JWTVerificationException {
        if (!request.getHeaders().containsKey("Authorization")) {
            throw new JWTVerificationException(ErrorCodes.ERR_ACCESS_DENIED, AUTH_HEADER_MISSING);
        }

        List<String> headers = request.getHeaders().get("Authorization");
        if (headers == null || headers.isEmpty()) {
            throw new JWTVerificationException(ErrorCodes.ERR_ACCESS_DENIED, "Authorization header is empty");
        }

        String credential = headers.get(0).trim();
        String[] components = credential.split("\\s");

        if (components.length != 2) {
            throw new JWTVerificationException(ErrorCodes.ERR_ACCESS_DENIED, AUTH_MALFORMED);
        }

        if (!components[0].equals("Bearer")) {
            throw new JWTVerificationException(ErrorCodes.ERR_ACCESS_DENIED, BEARER_MISSING);
        }

        return components[1].trim();
    }

    private boolean validateRoles(List<String> allowedRoles, List<String> inputRoles) {
        for (String role : inputRoles) {
            if (allowedRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

}
