package org.swasth.apigateway.service;

import org.swasth.apigateway.cache.RedisCache;
import org.swasth.apigateway.constants.Constants;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.ServerException;
import org.swasth.apigateway.utils.HttpUtils;
import org.swasth.apigateway.utils.JSONUtils;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
        try {
            Map<String,Object> details;
            if(redisCache.isExists(filterValue) == true) {
                details = JSONUtils.deserialize(redisCache.get(filterValue), HashMap.class);
            } else {
                details = getDetails(filterKey, filterValue);
                redisCache.set(filterValue, JSONUtils.serialize(details), redisExpires);
            }
            return details;
        } catch (ServerException e) {
            logger.info("Redis cache is down, fetching participant details from the registry.");
            return getDetails(filterKey, filterValue);
        }
    }

    public Map<String,Object> getDetails(String filterKey, String filterValue) throws Exception {
        String url = registryUrl + "/api/v1/Organisation/search";
        String requestBody = "{\"filters\":{\"" + filterKey + "\":{\"eq\":\"" + filterValue + "\"}}}";
        HttpResponse response = null;
        try {
            response = HttpUtils.post(url, requestBody);
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.SERVICE_UNAVAILABLE, "Error connecting to registry service: " + e.getMessage());
        }
        Map<String,Object> details = new HashMap<>();
        if (response != null && response.getStatus() == 200) {
            ArrayList result = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
            if (!result.isEmpty()) {
                details = (Map<String, Object>) result.get(0);
            }
        } else {
            throw new Exception("Error in fetching the participant details" + response.getStatus());
        }
        return details;
    }
}
