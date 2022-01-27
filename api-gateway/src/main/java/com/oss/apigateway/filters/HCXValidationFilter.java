package com.oss.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oss.apigateway.cache.RedisCache;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.exception.ServerException;
import com.oss.apigateway.models.Request;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.utils.HttpUtils;
import com.oss.apigateway.utils.JSONUtils;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
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
import java.util.List;
import java.util.Map;

import static com.oss.apigateway.constants.Constants.AUTHORIZATION;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

@Component
public class HCXValidationFilter extends AbstractGatewayFilterFactory<HCXValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(HCXValidationFilter.class);
    private final JWTVerifier jwtVerifier;

    @Autowired
    RedisCache redisCache;

    @Autowired
    protected Environment env;

    @Value("${timestamp.range}")
    protected int timestampRange;

    @Value("${registry.basePath}")
    private String registryUrl;

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
                Request request = new Request(JSONUtils.deserialize(cachedBody.toString(), HashMap.class));
                correlationId = request.getCorrelationId();
                apiCallId = request.getApiCallId();
                request.validate(getMandatoryHeaders(), fetchDetails(request.getSenderCode()), fetchDetails(request.getRecipientCode()), getSubject(exchange), timestampRange);
            } catch (ClientException e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.BAD_REQUEST, correlationId, apiCallId, e.getErrCode(), e);
            } catch (ServerException e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, correlationId, apiCallId, e.getErrCode(), e);
            } catch (Exception e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, correlationId, apiCallId, null, e);
            }
            return chain.filter(exchange);
        };
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

    private Map<String,Object> fetchDetails(String code) throws Exception {
        try {
            Map<String,Object> details;
            if(redisCache.isExists(code) == true) {
                details = JSONUtils.deserialize(redisCache.get(code), HashMap.class);
            } else {
                details = getDetails(code);
                redisCache.set(code, JSONUtils.serialize(details));
            }
            return details;
        } catch (ServerException e) {
            logger.info("Redis cache is down, fetching participant details from the registry.");
            return getDetails(code);
        }
    }

    private Map<String,Object> getDetails(String code) throws Exception {
        String url = registryUrl + "/api/v1/Organisation/search";
        String requestBody = "{\"filters\":{\"osid\":{\"eq\":\"" + code + "\"}}}";
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

    public static class Config {
    }

}
