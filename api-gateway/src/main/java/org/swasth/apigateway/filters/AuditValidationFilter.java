package org.swasth.apigateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.handlers.ExceptionHandler;
import org.swasth.apigateway.handlers.RequestHandler;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.swasth.common.response.ResponseMessage.INVALID_SENDER;

@Component
public class AuditValidationFilter extends AbstractGatewayFilterFactory<AuditValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuditValidationFilter.class);

    @Autowired
    RegistryService registryService;

    @Autowired
    ExceptionHandler exceptionHandler;

    @Autowired
    RequestHandler requestHandler;

    public AuditValidationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Map<String, Object> filterMap;
            Mono<Void> modifiedReq;
            try {
                String sub = exchange.getRequest().getHeaders().getFirst("X-jwt-sub");
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                filterMap = JSONUtils.deserialize(cachedBody.toString(), HashMap.class);
                String searchRequest = "{\"filters\":{\"" + Constants.OS_OWNER + "\":{\"eq\":\"" + sub + "\"}}}";
                List<Map<String, Object>> searchResult = registryService.getDetails(searchRequest);
                if (!searchResult.isEmpty()) {
                    Map<String, Object> participant = searchResult.get(0);
                    ArrayList<String> roles = (ArrayList<String>) participant.get("roles");
                    String code = (String) participant.get(Constants.PARTICIPANT_CODE);
                    Map<String, String> filters = (Map<String, String>) filterMap.get("filters");
                    if (roles.contains("payor") || roles.contains("provider")) {
                        filters.put("x-hcx-sender_code", code);
                        filterMap.put("filters", filters);
                        logger.debug("updated filters: {}", filterMap);
                    }
                } else {
                    throw new ClientException(ErrorCodes.ERR_INVALID_SENDER, INVALID_SENDER);
                }
            modifiedReq = requestHandler.getUpdatedBody(exchange, chain, filterMap);
            } catch (Exception e) {
                return exceptionHandler.errorResponse(e, exchange, null, null, new BaseRequest());
            }
            return modifiedReq;
        };
    }
    
    public static class Config {
    }

}
