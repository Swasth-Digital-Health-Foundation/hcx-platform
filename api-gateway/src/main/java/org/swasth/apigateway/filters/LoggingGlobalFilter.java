package org.swasth.apigateway.filters;

import org.swasth.apigateway.constants.FilterOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Logs request metadata, headers & response metadata
 */
@Component
@Slf4j
public class LoggingGlobalFilter implements Ordered, GlobalFilter {

    private static final String START_TIME = "startTime";

    private static final String HTTP_SCHEME = "http";

    private static final String HTTPS_SCHEME = "https";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI requestURI = request.getURI();
        String scheme = requestURI.getScheme();
        /*
         * not http or https scheme
         */
        if ((!HTTP_SCHEME.equalsIgnoreCase(scheme) && !HTTPS_SCHEME.equals(scheme))) {
            return chain.filter(exchange);
        }
        logRequest(exchange);
        return chain.filter(exchange).then(Mono.fromRunnable(()->logResponse(exchange)));
    }

    @Override
    public int getOrder() {
        return FilterOrder.LOGGING_FILTER.getOrder();
    }

    /**
     * log request
     * @param exchange
     */
    private void logRequest(ServerWebExchange exchange){
        ServerHttpRequest request = exchange.getRequest();
        URI requestURI = request.getURI();
        String scheme = requestURI.getScheme();
        HttpHeaders headers = request.getHeaders();
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME, startTime);
        log.info("[RequestLogFilter](Request) Scheme:{},Path:{}, Method:{},IP:{},Host:{}",
                scheme,
                requestURI.getPath(),
                request.getMethod(), getIpAddress(request),requestURI.getHost());
        headers.forEach((key,value)-> log.debug("[RequestLogFilter](Request)Headers:Key->{},Value->{}",key,value));
    }

    /**
     * log response exclude response body
     * @param exchange
     */
    private Mono<Void> logResponse(ServerWebExchange exchange){
        Long startTime = exchange.getAttribute(START_TIME);
        long executeTime = 0L;
        if(startTime != null ){
            executeTime = (System.currentTimeMillis() - startTime);
            log.info("[RequestLogFilter](Response)Response Time:{}",executeTime);
        }
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.forEach((key,value)-> log.debug("[RequestLogFilter] Headers:Key->{},Value->{}",
                key,
                value));
        MediaType contentType = headers.getContentType();
        long length = headers.getContentLength();
        log.info("[RequestLogFilter](Response) HttpStatus:{},ContentType:{},Content Length:{}",
                response.getStatusCode(),contentType,length);
        return Mono.empty();
    }

    /**
     * get Real Ip Address
     * @param request ServerHttpRequest
     * @return
     */
    private String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if((ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) && request.getRemoteAddress() != null) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        if(ip != null && ip.length() > 15 && ip.contains(",")){
            ip = ip.substring(0,ip.indexOf(","));
        }
        return ip;
    }
}


