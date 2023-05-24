package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.common.dto.RegistryResponse;
import org.swasth.common.exception.AuthorizationException;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ResourceNotFoundException;
import org.swasth.common.exception.ServerException;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.handlers.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.swasth.common.utils.Constants.*;

public class BaseRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(BaseRegistryService.class);

    @Value("${registry.basePath}")
    protected String registryURL;
    @Autowired
    protected EventGenerator eventGenerator;

    @Autowired
    protected EventHandler eventHandler;
    public HttpResponse<String> registryInvite(Map<String, Object> requestBody, HttpHeaders header, String apiPath) throws JsonProcessingException {
        String url = registryURL + apiPath + "/" + INVITE;
        System.out.println("url" + url);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        return HttpUtils.post(url, JSONUtils.serialize(requestBody), headersMap);
    }

    public RegistryResponse registrySearch(Map<String,Object> requestBody,String apiPath, String entity) throws Exception {
        String url = registryURL + apiPath + "/" + SEARCH;
        HttpResponse<String> response = HttpUtils.post(url, JSONUtils.serialize(requestBody), new HashMap<>());
        if (response.getStatus() == 200) {
            logger.info("Search is completed :: status code: {}", response.getStatus());
        }
        return responseHandler(response,null,entity);
    }

    public HttpResponse<String> registryUpdate(Map<String, Object> requestBody, Map<String, Object> registryDetails, HttpHeaders header, String apiPath) throws JsonProcessingException {
        String url = registryURL + apiPath + "/" + registryDetails.get(OSID);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        return HttpUtils.put(url, JSONUtils.serialize(requestBody), headersMap);
    }

    public HttpResponse<String> registryDelete(Map<String, Object> registryDetails, HttpHeaders header, String apiPath){
        String url = registryURL + apiPath + "/" + registryDetails.get(OSID);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(AUTHORIZATION, Objects.requireNonNull(header.get(AUTHORIZATION)).get(0));
        return HttpUtils.delete(url, headersMap);
    }

    private String getErrorMessage(HttpResponse<String> response) throws Exception {
        Map<String, Object> result = JSONUtils.deserialize(response.getBody(), HashMap.class);
        return (String) ((Map<String, Object>) result.get("params")).get("errmsg");
    }

    protected Map<String, Object> getCData(String action, Map<String, Object> registryDetails) {
        Map<String, Object> cdata = new HashMap<>();
        cdata.put(ACTION, action);
        cdata.putAll(registryDetails);
        return cdata;
    }

    public RegistryResponse responseHandler(HttpResponse<String> response, String code, String entity) throws Exception {
        switch (response.getStatus()) {
            case 200:
                if (response.getBody().isEmpty()) {
                    return new RegistryResponse("", entity);
                } else {
                    if (response.getBody().startsWith("["))
                        return new RegistryResponse(JSONUtils.deserialize(response.getBody(), ArrayList.class), entity);
                    else
                        return new RegistryResponse(code, entity);
                }
            case 400:
                throw new ClientException(getErrorMessage(response));
            case 401:
                throw new AuthorizationException(getErrorMessage(response));
            case 404:
                throw new ResourceNotFoundException(getErrorMessage(response));
            default:
                throw new ServerException(getErrorMessage(response));
        }
    }

    protected ResponseEntity<Object> getSuccessResponse(Object response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
