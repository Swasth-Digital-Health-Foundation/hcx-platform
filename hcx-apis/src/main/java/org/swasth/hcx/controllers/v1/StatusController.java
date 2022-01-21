package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.*;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.managers.HealthCheckManager;

import java.util.HashMap;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(value = "/v1/hcx")
public class StatusController extends BaseController {

    @Value("${kafka.topic.status}")
    private String topic;

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public ResponseEntity<Object> status(@RequestBody Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        try {
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException(ErrorCodes.SERVICE_UNAVAILABLE, "Service is unavailable");
            Request request = new Request(requestBody);
            setResponseParams(request, response);
            request.validate(getMandatoryHeaders(), getAuditData(request, HCX_STATUS), HCX_STATUS, timestampRange);
            Map<String, Object> hcxHeaders = request.getHcxHeaders();
            // TODO: filter properties validation
            if (!hcxHeaders.containsKey(STATUS_FILTERS) || ((Map<String, Object>) hcxHeaders.get(STATUS_FILTERS)).isEmpty()) {
                throw new ClientException("Invalid request, status filters is missing or empty.");
            }
            // Assuming a single result will be fetched for given api_call_id
            HeaderAudit result = auditService.search(new SearchRequestDTO((Map<String, String>) hcxHeaders.get(STATUS_FILTERS))).get(0);
            if (!hcxHeaders.get(SENDER_CODE).equals(result.getSender_code())) {
                throw new ClientException("Request does not belongs to sender");
            }
            String entityType = result.getAction().split("/")[2];
            if (!STATUS_SEARCH_ALLOWED_ENTITIES.contains(entityType)) {
                throw new ClientException("Invalid entity, status search allowed only for entities: " + STATUS_SEARCH_ALLOWED_ENTITIES);
            }
            StatusResponse statusResponse = new StatusResponse(entityType, result.getSender_code(), result.getRecipient_code(), (String) result.getStatus());
            Map<String,Object> statusResponseMap = JSONUtils.convert(statusResponse, HashMap.class);
            if (result.getStatus().equals("request.queued")) {
                response.setResult(statusResponseMap);
            } else if (result.getStatus().equals("request.dispatched")) {
                response.setResult(statusResponseMap);
                processAndSendEvent(HCX_STATUS, topic, request);
            } else {
                // TODO: handle for other status
                System.out.println("TODO for status " + result.getStatus());
            }
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @RequestMapping(value = "/on_status", method = RequestMethod.POST)
    public ResponseEntity<Object> onStatus(@RequestBody Map<String, Object> requestBody) {
        Response response = new Response();
        try {
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException(ErrorCodes.SERVICE_UNAVAILABLE, "Service is unavailable");
            Request request = new Request(requestBody);
            setResponseParams(request, response);
            request.validate(getMandatoryHeaders(), getAuditData(request, HCX_ONSTATUS), HCX_ONSTATUS, timestampRange);
            Map<String, Object> hcxHeaders = request.getHcxHeaders();
            if(!hcxHeaders.containsKey(STATUS_RESPONSE) || ((Map<String, Object>) hcxHeaders.get(STATUS_RESPONSE)).isEmpty()) {
                throw new ClientException("Invalid request, status response is missing or empty.");
            }
            // TODO: status response property validation
            processAndSendEvent(HCX_ONSTATUS, topic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

}
