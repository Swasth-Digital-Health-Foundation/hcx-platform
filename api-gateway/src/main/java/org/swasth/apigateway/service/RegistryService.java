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
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.redis.cache.RedisCache;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.response.ResponseMessage.REGISTRY_SERVICE_ERROR;
import static org.swasth.common.response.ResponseMessage.REGISTRY_SERVICE_FETCH_MSG;

@Service
public class RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    @Autowired
    RedisCache redisCache;

    @Value("${registry.basePath}")
    private String registryUrl;

    @Value("${redis.expires}")
    private int redisExpires;

    @Value("${hcx-api.basePath}")
    private String hcxApiUrl;

    @Value("${version.internal}")
    private String internalVersion;

    public Map<String, Object> fetchDetails(String filterKey, String filterValue) throws Exception {
        String requestBody = "{\"filters\":{\"" + filterKey + "\":{\"eq\":\"" + filterValue + "\"}}}";
        try {
            Map<String, Object> details;
            if (redisCache.isExists(filterValue) == true) {
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

    private Map<String, Object> getParticipant(List<Map<String, Object>> searchResult) {
        return !searchResult.isEmpty() ? searchResult.get(0) : new HashMap<>();
    }

    public List<Map<String, Object>> getDetails(String requestBody) throws Exception {
        HttpResponse<String> response = HttpUtils.post( hcxApiUrl+ "/" + internalVersion + "/participant/search", requestBody);
        Map<String,Object> respMap = (Map<String, Object>) JSONUtils.deserialize(response.getBody(), Map.class);
        List<Map<String,Object>> details;
        if (response.getStatus() == 200) {
            details = (List<Map<String, Object>>) respMap.get(Constants.PARTICIPANTS);
        } else {
            String errMsg = ((Map<String,Object>) respMap.getOrDefault("error",  new HashMap<>())).getOrDefault("message", respMap).toString();
            logger.error("Error while fetching the participant details from the registry :: status: {} :: message: {}", response.getStatus(), errMsg);
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, MessageFormat.format("Error while fetching the participant details from the registry :: status: {0} :: message: {1}", response.getStatus(),  errMsg));
        }
        return details;
    }
}
