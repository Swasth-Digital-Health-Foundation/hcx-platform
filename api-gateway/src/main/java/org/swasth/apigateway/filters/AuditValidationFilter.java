package org.swasth.apigateway.filters;

import com.auth0.jwt.JWTVerifier;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.helpers.ExceptionHandler;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.apigateway.utils.JSONUtils;
import kong.unirest.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;

@Component
public class AuditValidationFilter extends AbstractGatewayFilterFactory<AuditValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuditValidationFilter.class);
    private final JWTVerifier jwtVerifier;

    @Autowired
    RegistryService registryService;

    @Autowired
    ExceptionHandler exceptionHandler;

    public AuditValidationFilter(@Qualifier("jwk") JWTVerifier jwtVerifier) {
        super(Config.class);
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Map filterMap;
            try {
                String sub = exchange.getRequest().getHeaders().getFirst("X-jwt-sub");
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                filterMap = JSONUtils.deserialize(cachedBody.toString(), HashMap.class);
                Map<String, Object> participant = registryService.getDetails("osOwner", sub);
                if (!participant.isEmpty()) {
                    ArrayList<String> roles = (ArrayList<String>) participant.get("roles");
                    String code = (String) participant.get("osid");
                    Map<String, String> filters = (Map<String, String>) filterMap.get("filters");
                    if(roles.contains("payor") || roles.contains("provider")) {
                        filters.put("x-hcx-sender_code", new StringBuilder().append("1-").append(code).toString());
                        filterMap.put("filters", filters);
                        System.out.println("updated filters" + filterMap);
                    }
                } else {
                    throw new ClientException(ErrorCodes.ERR_INVALID_SENDER, "Sender is not exist in registry");
                }
            } catch (Exception e) {
                return exceptionHandler.errorResponse(e, exchange, null, null);
            }
            ModifyRequestBodyGatewayFilterFactory.Config modifyRequestConfig = new ModifyRequestBodyGatewayFilterFactory.Config()
                    .setContentType(ContentType.APPLICATION_JSON.getMimeType())
                    .setRewriteFunction(String.class, Map.class, (exchange1, cachedBody) -> Mono.just(filterMap));
            return new ModifyRequestBodyGatewayFilterFactory().apply(modifyRequestConfig).filter(exchange, chain);
        };
    }

    public static class Config {
    }

}
