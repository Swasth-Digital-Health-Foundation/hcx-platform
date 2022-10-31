package org.swasth.apigateway.service;

import org.springframework.web.server.ServerWebExchange;

public interface AuthorizationService {
    public boolean isAuthorized(ServerWebExchange exchange, Object payload);
}
