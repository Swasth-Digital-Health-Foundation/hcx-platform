package org.swasth.apigateway.handlers;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.swasth.apigateway.decorator.RequestDecorator;
import org.swasth.common.utils.JSONUtils;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class RequestHandler {

    public Mono<Void> getUpdatedBody(ServerWebExchange exchange, GatewayFilterChain chain, Map<String,Object> request) throws Exception {
        byte[] modifiedBody = JSONUtils.serialize(request).getBytes();
        RequestDecorator requestDecorator = new RequestDecorator(exchange.getRequest(), modifiedBody);
        exchange = exchange.mutate().request(requestDecorator.mutate().header("Content-Length", String.valueOf(modifiedBody.length)).build()).build();
        return chain.filter(exchange);
    }

}
