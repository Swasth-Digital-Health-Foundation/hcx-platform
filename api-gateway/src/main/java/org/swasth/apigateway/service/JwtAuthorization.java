package org.swasth.apigateway.service;

import org.swasth.apigateway.models.Acl;
import org.swasth.apigateway.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;

@Service
public class JwtAuthorization implements AuthorizationService{

    private final Map<String, Acl> aclMap;

    public JwtAuthorization(Map<String, Acl> aclMap) {
        this.aclMap = aclMap;
    }

    @Override
    public boolean isAuthorized(ServerWebExchange exchange, Object payload, String entityType) {
        String path = exchange.getRequest().getPath().value();
        List<String> userRoles = (List) payload;

        if(StringUtils.equalsAnyIgnoreCase(entityType, "User")) {
            userRoles.add(entityType.toLowerCase());
        }

        for(String role : userRoles){
            if(aclMap.containsKey(role)){
                Acl acl = aclMap.get(role);
                if(acl.getPaths().stream().anyMatch(path::equalsIgnoreCase) || Utils.containsRegexPath(acl.getRegexPaths(),
                        path)){
                    return true;
                }
            }
        }

        return false;
    }
}
