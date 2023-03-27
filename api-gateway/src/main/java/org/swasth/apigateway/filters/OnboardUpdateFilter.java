package org.swasth.apigateway.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.swasth.apigateway.handlers.RequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class OnboardUpdateFilter extends AbstractGatewayFilterFactory<OnboardUpdateFilter.Config> {
    @Autowired
    RequestHandler requestHandler;

    @Override
    public GatewayFilter apply(Config config) {
        Map<String,Object> filterMap = new HashMap<>();
        return ((exchange, chain) -> {
            try{
                String getToken = Objects.requireNonNull(exchange.getRequest().getHeaders().get("Authorization").toString().substring(7));
                filterMap.put("Authorization",getToken);
            }
            catch (Exception e){
                throw new RuntimeException();
            }
            return requestHandler.getUpdatedBody(exchange,chain,filterMap);
        });
    }

    public static class Config {
    }
}
