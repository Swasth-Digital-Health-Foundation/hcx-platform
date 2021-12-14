package com.oss.apigateway.filters;

import com.oss.apigateway.constants.Constants;
import com.oss.apigateway.constants.FilterOrder;
import com.oss.apigateway.models.Acl;
import com.oss.apigateway.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Carries out pre authentication checks
 *
 * If the path is to be allowed access to anonymous users then skips authentication / authorization
 * checks and provides access
 */
@Component
@Slf4j
public class PreAuthenticationFilter implements GlobalFilter, Ordered {

    private final Acl anonymousAllowedPaths;

    @Autowired
    public PreAuthenticationFilter(Map<String, Acl> aclMap) {
        this.anonymousAllowedPaths = aclMap.get("ANONYMOUS");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if(anonymousAllowedPaths.getPaths().contains(path) || Utils.containsRegexPath(anonymousAllowedPaths.getRegexPaths(), path)){
            exchange.getAttributes().put(Constants.AUTH_REQUIRED, false);
        }
        else{
        exchange.getAttributes().put(Constants.AUTH_REQUIRED, true);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FilterOrder.PRE_AUTH_FILTER.getOrder();
    }

}
