package com.oss.apigateway.service;

import com.oss.apigateway.models.Acl;
import com.oss.apigateway.utils.Utils;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AddverbAuthorization implements AuthorizationService{

    private static final String INSTANCE_CODE_HEADER_NAME = "instanceCode";
    private final Map<String, Acl> aclMap;
    private final RestTemplate restTemplate;
    private final String addverbAuthorizationUrl;

    @Autowired
    public AddverbAuthorization(Map<String, Acl> aclMap, RestTemplate restTemplate,
                                @Value("${addverbAuthorizationUrl}") String addverbAuthorizationUrl) {
        this.aclMap = aclMap;
        this.restTemplate = restTemplate;
        this.addverbAuthorizationUrl = addverbAuthorizationUrl;
    }

    @Override
    public boolean isAuthorized(ServerWebExchange exchange, Object payload) {
        String path = exchange.getRequest().getPath().value();
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if(!headers.containsKey(INSTANCE_CODE_HEADER_NAME)){
            return false;
        }
        String instanceCode = headers.getFirst(INSTANCE_CODE_HEADER_NAME);
        List<String> userAllowedInstances = (List) payload;
        if( !userAllowedInstances.stream().anyMatch(exchange.getRequest().getHeaders().getFirst(INSTANCE_CODE_HEADER_NAME)::equalsIgnoreCase)){
            return false;
        }
        else {
            List<String> roles = fetchUserRolesForInstance(addverbAuthorizationUrl, instanceCode, headers.getFirst(
                    "Authorization"));
            for(String role : roles){
                if(aclMap.containsKey(role)){
                    Acl acl = aclMap.get(role);
                    if(acl.getPaths().contains(path) || Utils.containsRegexPath(acl.getRegexPaths(), path)){
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private List<String> fetchUserRolesForInstance(String url, String instanceCode, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json");
        headers.set(INSTANCE_CODE_HEADER_NAME, instanceCode);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, String.class, instanceCode);

        String body = response.getBody();
        JSONArray parsedBody = JsonPath.read(body, "$.instanceGroupList."+instanceCode+".*.roleList.*.roleId");
        if(parsedBody.isEmpty())
            return Collections.emptyList();
        else
            return (List) parsedBody;

    }

}
