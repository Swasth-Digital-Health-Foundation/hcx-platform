package com.oss.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import com.oss.apigateway.cache.RedisCache;
import com.oss.apigateway.constants.Constants;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.utils.HttpUtils;
import com.oss.apigateway.utils.JSONUtils;
import kong.unirest.HttpResponse;
import kong.unirest.json.JSONString;
import net.minidev.json.JSONArray;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
            String corrId = null;
            try {
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                String[] requestBody = formatRequestBody(JSONUtils.deserialize(cachedBody.toString(), HashMap.class));
                Map protectedMap;
                try {
                    protectedMap = JSONUtils.decodeBase64String(requestBody[0], HashMap.class);
                } catch (JsonParseException e) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_WRONG_ENCODED_PROTECTED, "Error while parsing protected headers");
                }
                if (!protectedMap.containsKey(Constants.CORRELATION_ID) || !(protectedMap.get(Constants.CORRELATION_ID) instanceof String) || ((String) protectedMap.get(Constants.CORRELATION_ID)).isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CORREL_ID, "Correlation id cannot be null, empty and other than 'String'");
                }
                corrId = (String) protectedMap.get(Constants.CORRELATION_ID);
                Object senderCode = protectedMap.get(Constants.SENDER_CODE);
                Object recipientCode = protectedMap.get(Constants.RECIPIENT_CODE);
                if (!(senderCode instanceof String) || ((String) senderCode).isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender code cannot be null, empty and other than 'String'");
                }
                if (!(recipientCode instanceof String) || ((String) recipientCode).isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient code cannot be null, empty and other than 'String'");
                }
                Map<String,Object> senderDetails = getDetails(senderCode.toString());
                Map<String,Object> recipientDetails = getDetails(recipientCode.toString());
                if (senderCode.equals(recipientCode)) {
                    throw new ClientException("sender and recipient code cannot be the same");
                }
                if (senderDetails.isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender is not exist in registry");
                }
                if (recipientDetails.isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient is not exist in registry");
                }
                validateCallerId(exchange, senderDetails);
            } catch (ClientException e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.BAD_REQUEST, corrId, e.getErrCode(), e);
            } catch (Exception e) {
                logger.error(e.toString());
                return this.onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, corrId, null, e);
            }
            return chain.filter(exchange);
        };
    }

    private void validateCallerId(ServerWebExchange exchange, Map<String,Object> senderDetails) throws ClientException {
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().get(Constants.AUTHORIZATION).get(0).trim().split("\\s")[1].trim();
        DecodedJWT decodedJWT = this.jwtVerifier.verify(token);
        String subject = decodedJWT.getSubject();
        if(!StringUtils.equals(((ArrayList) senderDetails.get(Constants.OS_OWNER)).get(0).toString(),subject)){
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CALLER_ID, "Caller id is mismatched");
        }
    }

    private String[] formatRequestBody(Map<String, Object> requestBody) throws Exception {
        try {
            String str = (String) requestBody.get(Constants.PAYLOAD);
            return str.split("\\.");
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }
    }

    private Map<String,Object> getDetails(String code) throws Exception {
        String details = redisCache.get(code);
        if(details == null || details.isEmpty()) {
            String url = registryUrl + "/api/v1/Organisation/search";
            String requestBody = "{\"entityType\":[\"Organisation\"],\"filters\":{\"osid\":{\"eq\":\"" + code + "\"}}}";
            HttpResponse response = HttpUtils.post(url, requestBody);
            if (response != null && response.getStatus() == 200) {
                ArrayList result = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
                if (!result.isEmpty()){
                    details = JSONUtils.serialize(result.get(0));
                    redisCache.set(code, details);
                }
            }
            else {
                throw new Exception("Error in fetching the participant details " + response.getStatus());
            }
        }
        return JSONUtils.deserialize(details, HashMap.class);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String corrId, ErrorCodes code, Exception e) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        Response resp = new Response(corrId, new ResponseError(code, e.getMessage(), e.getCause()));
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
