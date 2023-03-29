package org.swasth.apigateway.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.swasth.apigateway.handlers.ExceptionHandler;
import org.swasth.apigateway.handlers.RequestHandler;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.common.utils.JSONUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.swasth.common.utils.Constants.JWT_TOKEN;

@Component
public class OnboardFilter extends AbstractGatewayFilterFactory<OnboardFilter.Config> {
    @Autowired
    RequestHandler requestHandler;

    @Autowired
    ExceptionHandler exceptionHandler;

    public OnboardFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        Map<String, Object> requestBody = new HashMap<>();
        return ((exchange, chain) -> {
            try {
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                requestBody.putAll(JSONUtils.deserialize(cachedBody.toString(), HashMap.class));
                if (!requestBody.containsKey(JWT_TOKEN)) {
                    String token = Objects.requireNonNull(exchange.getRequest().getHeaders().get("Authorization").toString().substring(7).replace("]",""));
                    requestBody.put(JWT_TOKEN, token);
                }
            } catch (Exception e) {
                return exceptionHandler.errorResponse(e, exchange, null, null, new BaseRequest());
            }
            return requestHandler.getUpdatedBody(exchange, chain, requestBody);
        });
    }

    public static class Config {
    }
}
