package org.swasth.apigateway.service;

import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.ServerException;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.redis.cache.RedisCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    @Autowired
    RedisCache redisCache;

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${redis.expires}")
    private int redisExpires;

    public Map<String,Object> fetchDetails(String filterKey, String filterValue) throws Exception {
        String requestBody = "{\"filters\":{\"" + filterKey + "\":{\"eq\":\"" + filterValue + "\"}}}";
        try {
            Map<String,Object> details;
            if(redisCache.isExists(filterValue) == true) {
                details = JSONUtils.deserialize(redisCache.get(filterValue), HashMap.class);
            } else {
                details = getParticipant(getDetails(requestBody));
                redisCache.set(filterValue, JSONUtils.serialize(details), redisExpires);
            }
            return details;
        } catch (ServerException e) {
            logger.info("Redis cache is down, fetching participant details from the registry.");
            return getParticipant(getDetails(requestBody));
        }
    }

    private Map<String,Object> getParticipant(List<Map<String,Object>> searchResult){
        return !searchResult.isEmpty() ? searchResult.get(0):new HashMap<>();
    }

    public List<Map<String,Object>> getDetails(String requestBody) throws Exception {
        String url = registryUrl + "/api/v1/Organisation/search";
        HttpResponse response = null;
        try {
            response = HttpUtils.post(url, requestBody);
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.SERVICE_UNAVAILABLE, "Error connecting to registry service: " + e.getMessage());
        }
        List<Map<String,Object>> details;
        if (response.getStatus() == 200) {
            details = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
        } else {
            throw new Exception("Error in fetching the participant details" + response.getStatus());
        }
        return details;
    }
}
