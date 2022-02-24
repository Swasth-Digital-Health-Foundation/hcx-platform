package org.swasth.apigateway.service;

import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.exception.ServerException;
import org.swasth.apigateway.utils.HttpUtils;
import org.swasth.apigateway.utils.JSONUtils;

import java.util.*;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Value("${hcx-api.basePath}")
    private String hcxApiUrl;

    public List<Map<String, Object>> getAuditLogs(Map<String,String> filters) throws Exception {
        String url = hcxApiUrl + "/v1/audit/search";
        HttpResponse response;
        try {
            response = HttpUtils.post(url, JSONUtils.serialize(Collections.singletonMap("filters", filters)));
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.SERVICE_UNAVAILABLE, "Error connecting to audit service: " + e.getMessage());
        }
        List<Map<String,Object>> details;
        if (response != null && response.getStatus() == 200) {
            details = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
            System.out.println("Audit filters: " + filters + " Audit data count: " + details.size() + " Audit data: " + details);
        } else {
            throw new Exception("Error in fetching the audit logs" + response.getStatus());
        }
        return details;
    }

}
