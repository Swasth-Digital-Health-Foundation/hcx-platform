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
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.apigateway.utils.Utils;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Request;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.HttpUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.UUIDUtils;

import java.text.MessageFormat;
import java.util.*;
import static org.swasth.common.utils.Constants.*;


@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Value("${hcx-api.basePath}")
    private String hcxApiUrl;

    @Value("${version.internal}")
    private String internalVersion;

    @Autowired
    private AuditIndexer auditIndexer;

    @Autowired
    private EventGenerator eventGenerator;
    public List<Map<String, Object>> getAuditLogs(Map<String,String> filters) throws Exception {
        String url = hcxApiUrl + "/" + internalVersion + Constants.AUDIT_SEARCH;
        HttpResponse response;
        try {
            response = HttpUtils.post(url, JSONUtils.serialize(Collections.singletonMap("filters", filters)));
        } catch (UnirestException e) {
            throw new ServerException(ErrorCodes.SERVICE_UNAVAILABLE, "Error connecting to audit service: " + e.getMessage());
        }
        List<Map<String,Object>> details;
        if (response.getStatus() == 200) {
            details = JSONUtils.deserialize((String) response.getBody(), ArrayList.class);
            logger.info("Audit filters: " + filters + " Audit data count: " + details.size() + " Audit data: " + details);
        } else {
            logger.error("Error while fetching the audit data :: status: {} :: message: {}", response.getStatus(), response.getBody());
            throw new ServerException(ErrorCodes.INTERNAL_SERVER_ERROR, MessageFormat.format("Error while fetching the audit data :: status: {0} :: message: {1}", response.getStatus(), response.getBody()));
        }
        return details;
    }

    public void createAuditLog(BaseRequest request) throws Exception {
        Request req = new Request(request.getPayload(), request.getApiAction().replaceAll("/" + internalVersion, ""));
        req.setStatus(ERROR_STATUS);
        req.setErrorDetails(request.getErrorDetails());
        auditIndexer.createDocument(eventGenerator.generateAuditEvent(req));
    }

    public void updateAuditLog(Map<String,Object> event) throws Exception {
        auditIndexer.createDocument(event);
    }
}