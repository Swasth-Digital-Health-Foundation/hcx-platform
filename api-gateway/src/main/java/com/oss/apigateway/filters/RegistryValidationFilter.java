package com.oss.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oss.apigateway.cache.RedisCache;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.exception.ServerException;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.utils.HttpUtils;
import com.oss.apigateway.utils.JSONUtils;
import com.oss.apigateway.utils.Utils;
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

import static com.oss.apigateway.constants.Constants.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

@Component
public class RegistryValidationFilter extends AbstractGatewayFilterFactory<RegistryValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RegistryValidationFilter.class);
    private final JWTVerifier jwtVerifier;

    @Autowired
    RedisCache redisCache;

    @Value("${registry.basePath}")
    private String registryUrl;

    public RegistryValidationFilter(@Qualifier("jwk") JWTVerifier jwtVerifier) {
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
                String[] requestBody = formatRequestBody(JSONUtils.deserialize(cachedBody.toString(), HashMap.class));
                Map protectedMap;
                try {
                    protectedMap = JSONUtils.decodeBase64String(requestBody[0], HashMap.class);
                    System.out.println(protectedMap);
                } catch (JsonParseException e) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_WRONG_ENCODED_PROTECTED, "Error while parsing protected headers");
                }
                if (!protectedMap.containsKey(CORRELATION_ID) || !(protectedMap.get(CORRELATION_ID) instanceof String) || ((String) protectedMap.get(CORRELATION_ID)).isEmpty() || !Utils.isUUID((String) protectedMap.get(CORRELATION_ID))) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CORREL_ID, "Correlation id should be a valid UUID");
                }
                if (!protectedMap.containsKey(API_CALL_ID) || !(protectedMap.get(API_CALL_ID) instanceof String) || ((String) protectedMap.get(API_CALL_ID)).isEmpty() || !Utils.isUUID((String) protectedMap.get(API_CALL_ID))) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_API_CALL_ID, "API call id should be a valid UUID");
                }
                correlationId = (String) protectedMap.get(CORRELATION_ID);
                apiCallId = (String) protectedMap.get(API_CALL_ID);
                Object senderCode = protectedMap.get(SENDER_CODE);
                Object recipientCode = protectedMap.get(RECIPIENT_CODE);
                if (!(senderCode instanceof String) || ((String) senderCode).isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Invalid sender code");
                }
                if (!(recipientCode instanceof String) || ((String) recipientCode).isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Invalid recipient code");
                }
                if (senderCode.equals(recipientCode)) {
                    throw new ClientException("sender and recipient code cannot be the same");
                }
                Map<String,Object> senderDetails = fetchDetails(senderCode.toString());
                Map<String,Object> recipientDetails = fetchDetails(recipientCode.toString());
                if (senderDetails.isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender is not exist in registry");
                }
                if(StringUtils.equals((String) senderDetails.get(STATUS),BLOCKED)){
                    throw new ClientException(ErrorCodes.CLIENT_ERR_SENDER_BLOCKED, "Sender is blocked as per the registry");
                }
                validateCallerId(exchange, senderDetails);
                if (recipientDetails.isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient is not exist in registry");
                }
                if(StringUtils.equals((String) recipientDetails.get(STATUS),BLOCKED)){
                    throw new ClientException(ErrorCodes.CLIENT_ERR_RECIPIENT_BLOCKED, "Recipient is blocked as per the registry");
                }
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

    private void validateCallerId(ServerWebExchange exchange, Map<String,Object> senderDetails) throws ClientException {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().get(AUTHORIZATION).get(0).trim().split("\\s")[1].trim();
        DecodedJWT decodedJWT = this.jwtVerifier.verify(token);
        String subject = decodedJWT.getSubject();
        if(!StringUtils.equals(((ArrayList) senderDetails.get(OS_OWNER)).get(0).toString(),subject)){
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CALLER_ID, "Caller id is mismatched");
        }
    }

    private String[] formatRequestBody(Map<String, Object> requestBody) throws Exception {
        try {
            String str = (String) requestBody.get(PAYLOAD);
            return str.split("\\.");
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }
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
