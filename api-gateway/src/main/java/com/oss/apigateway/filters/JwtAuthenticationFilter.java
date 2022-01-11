package com.oss.apigateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oss.apigateway.constants.FilterOrder;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.exception.ServerException;
import com.oss.apigateway.models.Acl;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.security.JwtConfigs;
import com.oss.apigateway.service.AuthorizationService;
import com.oss.apigateway.utils.JSONUtils;
import com.oss.apigateway.utils.Utils;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.oss.apigateway.constants.Constants.AUTH_REQUIRED;
import static com.oss.apigateway.constants.Constants.X_JWT_SUB_HEADER;

/**
 * Authenticates the user by extracting the token from the header and validates the JWT
 * based on the config provided. If the jwt auth is disabled this filter is also disabled.
 *
 * 1. Checks if authentication to be carried out based on attribute set by {@link PreAuthenticationFilter}
 * 2. If auth required, JWT validation is provided by {@link com.oss.apigateway.security.JWTVerifierFactory}
 * 3. Post authentication, authorization checks are carried out for AUTHENTICATED Roles
 * 4. Detailed authorization to be carried out by implementations of {@link AuthorizationService}
 * 5. If authorization / authentication fails, reason is sent out in the WWW-Authenticate header
 */
@Component
@ConditionalOnBean(JwtConfigs.class)
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final String WWW_AUTH_HEADER = "WWW-Authenticate";

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JWTVerifier jwtVerifier;
    private final JwtConfigs jwtConfigs;
    private final AuthorizationService authorizationService;
    private final Acl authenticatedAllowedPaths;

    public JwtAuthenticationFilter(@Qualifier("jwk") JWTVerifier jwtVerifier, JwtConfigs jwtConfigs,
                                   AuthorizationService authorizationService,
                                   Map<String, Acl> aclMap) {
        this.jwtVerifier = jwtVerifier;
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
                DecodedJWT decodedJWT = this.jwtVerifier.verify(token);

                ServerHttpRequest request = exchange.getRequest().mutate().
                        header(X_JWT_SUB_HEADER, decodedJWT.getSubject()).
                        build();

                if (!authenticatedAllowedPaths.getPaths().contains(path) && !Utils.containsRegexPath(authenticatedAllowedPaths.getRegexPaths(),
                        path)) {

                    String payload = new String(Base64.getDecoder().decode(decodedJWT.getPayload()));

                    JSONArray claims = JsonPath.read(payload, jwtConfigs.getClaimsNamespacePath());

//                String temp = "{\n" +
//                        "  \"jti\": \"5ffb53b4-1e6b-43aa-b8f5-60b959ba84fe\",\n" +
//                        "  \"sub\": \"admin\",\n" +
//                        "  \"instances\": [\n" +
//                        "    \"SSO\",\n" +
//                        "    \"ril_bhi\"\n" +
//                        "  ],\n" +
//                        "  \"iat\": 1638598268,\n" +
//                        "  \"exp\": 1639203068\n" +
//                        "}";
//                JSONArray tempClaims = JsonPath.read(temp, "$.instances");

                    if (!authorizationService.isAuthorized(exchange, claims)) {
                        throw new ClientException(ErrorCodes.CLIENT_ERR_ACCESS_DENIED, "Access denied");
                    }
                }

                return chain.filter(exchange.mutate().request(request).build());

            } catch (ClientException e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.UNAUTHORIZED, e.getErrCode(), e);
            }
        } else {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return FilterOrder.AUTH_FILTER.getOrder();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, ErrorCodes code, Exception e) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        Response resp = new Response(new ResponseError(code, e.getMessage(), e.getCause()));
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

    private String extractJWTToken(ServerHttpRequest request) throws ClientException {
        if (!request.getHeaders().containsKey("Authorization")) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_AUTH_TOKEN, "Authorization header is missing");
        }

        List<String> headers = request.getHeaders().get("Authorization");
        if (headers == null || headers.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_AUTH_TOKEN, "Authorization header is empty");
        }

        String credential = headers.get(0).trim();
        String[] components = credential.split("\\s");

        if (components.length != 2) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_AUTH_TOKEN, "Malformat Authorization content");
        }

        if (!components[0].equals("Bearer")) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_AUTH_TOKEN, "Bearer is needed");
        }

        return components[1].trim();
    }

}
