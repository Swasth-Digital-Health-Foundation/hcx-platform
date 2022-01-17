package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.HeaderAudit;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.SearchRequestDTO;
import org.swasth.common.dto.StatusResponse;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.hcx.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import static org.swasth.hcx.utils.Constants.*;

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
            Map<String, Object> protectedMap = JSONUtils.decodeBase64String(((String) requestBody.get(Constants.PAYLOAD)).split("\\.")[0], HashMap.class);
            validateRequestBody(requestBody);
            // TODO: filter properties validation
            if (!protectedMap.containsKey(STATUS_FILTERS) || ((Map<String, Object>) protectedMap.get(STATUS_FILTERS)).isEmpty()) {
                throw new ClientException("Invalid request, status filters is missing or empty.");
            }
            // Assuming a single result will be fetched for given request_id
            HeaderAudit result = auditService.search(new SearchRequestDTO((HashMap<String, String>) protectedMap.get(STATUS_FILTERS))).get(0);
            if (!protectedMap.get(SENDER_CODE).equals(result.getSender_code())) {
                throw new ClientException("Request_id does not belongs to sender");
            }
            String entityType = result.getAction().split("/")[2];
            if (!STATUS_SEARCH_ALLOWED_ENTITIES.contains(entityType)) {
                throw new ClientException("Invalid entity, status search allowed only for entities: " + STATUS_SEARCH_ALLOWED_ENTITIES);
            }
            StatusResponse statusResponse = new StatusResponse(result.getRequest_id(), result.getCorrelation_id(), result.getWorkflow_id(), entityType, result.getSender_code(), result.getRecipient_code(), (String) result.getStatus());
            Map<String,Object> statusResponseMap = JSONUtils.convert(statusResponse, HashMap.class);
            if (result.getStatus().equals("request.queued")) {
                response.setResult(statusResponseMap);
            } else if (result.getStatus().equals("request.dispatched")) {
                response.setResult(statusResponseMap);
                processAndSendEvent(HCX_STATUS, topic, requestBody);
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
    public ResponseEntity<Object> onStatus(@RequestBody Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        try {
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException(ErrorCodes.SERVICE_UNAVAILABLE, "Service is unavailable");
            Map<String, Object> protectedMap = JSONUtils.decodeBase64String(((String) requestBody.get(Constants.PAYLOAD)).split("\\.")[0], HashMap.class);
            validateRequestBody(requestBody);
            if(!protectedMap.containsKey(STATUS_RESPONSE) || ((Map<String, Object>) protectedMap.get(STATUS_RESPONSE)).isEmpty()) {
                throw new ClientException("Invalid request, status response is missing or empty.");
            }
            processAndSendEvent(HCX_ONSTATUS, topic, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }


}
