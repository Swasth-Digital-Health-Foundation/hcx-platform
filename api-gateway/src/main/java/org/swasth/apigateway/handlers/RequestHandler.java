package org.swasth.apigateway.handlers;

import kong.unirest.ContentType;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class RequestHandler {

    public Mono<Void> getUpdatedBody(ServerWebExchange exchange, GatewayFilterChain chain, Map<String, Object> updatedBody) {
        ModifyRequestBodyGatewayFilterFactory.Config modifyRequestConfig = new ModifyRequestBodyGatewayFilterFactory.Config()
                .setContentType(ContentType.APPLICATION_JSON.getMimeType())
                .setRewriteFunction(String.class, Map.class, (exchange1, cachedBody) -> Mono.just(updatedBody));
        return new ModifyRequestBodyGatewayFilterFactory().apply(modifyRequestConfig).filter(exchange, chain);
    }

}
