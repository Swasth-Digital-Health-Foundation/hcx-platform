package org.swasth.apigateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.swasth.apigateway.constants.Constants.CORRELATION_ID;
import static org.swasth.apigateway.constants.FilterOrder.CORRELATION_ID_FILTER;

/**
 * Adds a correlation id to the header for all incoming requests.
 * Filter with highest priority to ensure all logs contain the correlation id
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uuid = UUID.randomUUID().toString();
        exchange.getAttributes().put(CORRELATION_ID, uuid);
        exchange.mutate().request(builder -> builder.header(CORRELATION_ID, uuid));
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return CORRELATION_ID_FILTER.getOrder();
    }
}
