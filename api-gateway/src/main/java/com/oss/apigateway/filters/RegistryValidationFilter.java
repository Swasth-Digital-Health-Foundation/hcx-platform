package com.oss.apigateway.filters;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oss.apigateway.cache.RedisCache;
import com.oss.apigateway.constants.Constants;
import com.oss.apigateway.exception.ClientException;
import com.oss.apigateway.exception.ErrorCodes;
import com.oss.apigateway.models.Response;
import com.oss.apigateway.models.ResponseError;
import com.oss.apigateway.utils.HttpUtils;
import com.oss.apigateway.utils.JSONUtils;
import kong.unirest.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
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
public class RegistryValidationFilter extends AbstractGatewayFilterFactory<RegistryValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RegistryValidationFilter.class);

    @Autowired
    RedisCache redisCache;

    @Value("${registry.basePath}")
    private String registryUrl;

    private static final String WWW_VALIDATE_HEADER = "WWW-Validate";

    public RegistryValidationFilter() {
        super(Config.class);
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
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_PROTECTED, "Error while parsing protected headers");
                }
                corrId = (String) protectedMap.get(Constants.CORRELATION_ID);
                String senderCode = (String) protectedMap.get(Constants.SENDER_CODE);
                String recipientCode = (String) protectedMap.get(Constants.RECIPIENT_CODE);
                Map<String,Object> senderDetails = getDetails(senderCode);
                Map<String,Object> recipientDetails = getDetails(recipientCode);
                if (senderCode.equals(recipientCode)) {
                    throw new ClientException("sender and recipient code cannot be the same");
                }
                if (senderDetails.isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender is not exist in registry");
                }
                if (recipientDetails.isEmpty()) {
                    throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient is not exist in registry");
                }
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

    private String[] formatRequestBody(Map<String, Object> requestBody) throws Exception {
        try {
            String str = (String) requestBody.get(Constants.PAYLOAD);
            return str.split("\\.");
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }
    }

    private Map<String,Object> getDetails(String entityId) throws Exception {
        String details = redisCache.get(entityId);
        if(details == null || details.isEmpty()) {
            String url = registryUrl + "/api/v1/Organisation/search";
            String requestBody = "{\"entityType\":[\"Organisation\"],\"filters\":{\"osid\":{\"eq\":\"" + entityId + "\"}}}";
            HttpResponse response = HttpUtils.post(url, requestBody);
            if (response != null && response.getStatus() == 200) {
                ArrayList result = JSONUtils.deserialize(response.getBody().toString(), ArrayList.class);
                if (!result.isEmpty()){
                    details = (String) result.get(0);
                    redisCache.set(entityId,result.get(0).toString());
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
            return response.writeWith(Mono.just(obj).map(r -> dataBufferFactory.wrap(r)));
        } catch (JsonProcessingException ex) {
            logger.error(ex.toString());
        }
        return response.setComplete();
    }

    public static class Config {
    }

}
