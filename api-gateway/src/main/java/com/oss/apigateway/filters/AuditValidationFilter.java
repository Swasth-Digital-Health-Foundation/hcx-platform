package com.oss.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oss.apigateway.cache.RedisCache;
import com.oss.apigateway.constants.Constants;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.exception.ServerException;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.utils.HttpUtils;
import com.oss.apigateway.utils.JSONUtils;
import com.oss.apigateway.utils.Utils;
import kong.unirest.ContentType;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

@Component
public class AuditValidationFilter extends AbstractGatewayFilterFactory<AuditValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuditValidationFilter.class);
    private final JWTVerifier jwtVerifier;

    @Autowired
    RedisCache redisCache;

    @Value("${registry.basePath}")
    private String registryUrl;

    public AuditValidationFilter(@Qualifier("jwk") JWTVerifier jwtVerifier) {
        super(Config.class);
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("exchange body in audit validation filter    " + exchange.getRequest().getHeaders().getFirst("X-jwt-sub"));
            System.out.println(new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer())));
            Map filterMap;
            try {
                String sub = exchange.getRequest().getHeaders().getFirst("X-jwt-sub");
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                filterMap = JSONUtils.deserialize(cachedBody.toString(), HashMap.class);
                System.out.println("request body");
                System.out.println(filterMap);
                Map<String, Object> participant = this.getDetails(exchange.getRequest().getHeaders().getFirst("X-jwt-sub"));
                System.out.println("participant id" + participant);
                if (!participant.isEmpty()) {
                    ArrayList<String> roles = (ArrayList<String>) participant.get("roles");
                    String code = (String) participant.get("osid");
                    Map<String, String> filters = (Map<String, String>) filterMap.get("filters");
                    if(roles.contains("payor") || roles.contains("provider")) {
                        filters.put("sender_code", code);
                        System.out.println("filters updated" + filters + "  " + roles);
                        filterMap.put("filters", filters);
                        System.out.println("protectedmap updated" + filterMap);
                    }
                } else {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender is not exist in registry");
                }
            } catch (ClientException e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.BAD_REQUEST, e.getErrCode(), e);
            } catch (ServerException e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, e.getErrCode(), e);
            } catch (Exception e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, null, e);
            }
            ModifyRequestBodyGatewayFilterFactory.Config modifyRequestConfig = new ModifyRequestBodyGatewayFilterFactory.Config()
                    .setContentType(ContentType.APPLICATION_JSON.getMimeType())
                    .setRewriteFunction(String.class, Map.class, (exchange1, cachedBody) -> Mono.just(filterMap));
            return new ModifyRequestBodyGatewayFilterFactory().apply(modifyRequestConfig).filter(exchange, chain);
        };
    }

    private Map<String,Object> getDetails(String code) throws Exception {
        String url = registryUrl + "/api/v1/Organisation/search";
        String requestBody = "{\"filters\":{\"osOwner\":{\"eq\":\"" + code + "\"}}}";
        HttpResponse response = null;
        try {
            response = HttpUtils.post(url, requestBody);
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.SERVICE_UNAVAILABLE, "Error connecting to registry service: " + e.getMessage());
        }
        Map<String,Object> details = new HashMap<>();
        if (response != null && response.getStatus() == 200) {
            ArrayList result = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
            if (!result.isEmpty()) {
                details = (Map<String, Object>) result.get(0);
            }
        } else {
            throw new Exception("Error in fetching the participant details" + response.getStatus());
        }
        return details;
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

    public static class Config {
    }

}
