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
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(value = "/v1/hcx")
public class StatusController extends BaseController {

    @Value("${kafka.topic.status}")
    private String topic;

    @Value("${allowedEntitiesForStatusSearch}")
    private List<String> allowedEntitiesForStatusSearch;

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public ResponseEntity<Object> status(@RequestBody Map<String, Object> requestBody) throws Exception {
        Response response = new Response();
        try {
            checkSystemHealth();
            Request request = new Request(requestBody);
            setResponseParams(request, response);
            Map<String,String> auditFilters = new HashMap<>();
            auditFilters.put(SENDER_CODE, request.getSenderCode());
            auditFilters.put(CORRELATION_ID, request.getCorrelationId());
            List<HeaderAudit> auditResponse = auditService.search(new SearchRequestDTO(auditFilters));
            if(auditResponse.isEmpty()){
                throw new ClientException("Invalid correlation id, details do not exist");
            }
            HeaderAudit auditData = auditResponse.get(auditResponse.size()-1);
            String entityType = auditData.getAction().split("/")[2];
            if (!allowedEntitiesForStatusSearch.contains(entityType)) {
                throw new ClientException("Invalid entity, status search allowed only for entities: " + allowedEntitiesForStatusSearch);
            }
            StatusResponse statusResponse = new StatusResponse(entityType, auditData.getSender_code(), auditData.getRecipient_code(), auditData.getStatus());
            Map<String,Object> statusResponseMap = JSONUtils.convert(statusResponse, HashMap.class);
            if (auditData.getStatus().equals(QUEUED_STATUS)) {
                response.setResult(statusResponseMap);
            } else if (auditData.getStatus().equals(DISPATCHED_STATUS)) {
                response.setResult(statusResponseMap);
                processAndSendEvent(HCX_STATUS, topic, request);
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
            checkSystemHealth();
            Request request = new Request(requestBody);
            setResponseParams(request, response);
            processAndSendEvent(HCX_ONSTATUS, topic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

}
