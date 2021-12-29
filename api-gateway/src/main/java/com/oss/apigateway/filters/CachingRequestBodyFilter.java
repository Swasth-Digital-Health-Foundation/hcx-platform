package com.oss.apigateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;

@Component
public class CachingRequestBodyFilter extends AbstractGatewayFilterFactory<CachingRequestBodyFilter.Config> {

    public CachingRequestBodyFilter() {
        super(Config.class);
    }

    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ServerWebExchangeUtils.cacheRequestBody(exchange,
                (serverHttpRequest) -> chain.filter(exchange.mutate().request(serverHttpRequest).build()));
    }

    public static class Config {
    }
}