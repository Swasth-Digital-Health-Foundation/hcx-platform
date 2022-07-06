package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.*;
import org.swasth.common.exception.ClientException;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.BaseController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class StatusController extends BaseController {

    @Value("${kafka.topic.status}")
    private String topic;

    @Value("${allowedEntitiesForStatusSearch}")
    private List<String> allowedEntitiesForStatusSearch;

    @PostMapping(HCX_STATUS)
    public ResponseEntity<Object> status(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, HCX_STATUS);
        Response response = new Response(request);
        try {
            Map<String,String> auditFilters = new HashMap<>();
            auditFilters.put(HCX_SENDER_CODE, request.getHcxSenderCode());
            auditFilters.put(CORRELATION_ID, request.getCorrelationId());
            List<Map<String,Object>> auditResponse = auditService.search(new SearchRequestDTO(auditFilters));
            if(auditResponse.isEmpty()){
                throw new ClientException("Invalid correlation id, details do not exist");
            }
            Map<String,Object> auditData = auditResponse.get(auditResponse.size()-1);
            String entityType = ((String) auditData.get(ACTION)).split("/")[1];
            if (!allowedEntitiesForStatusSearch.contains(entityType)) {
                throw new ClientException("Invalid entity, status search allowed only for entities: " + allowedEntitiesForStatusSearch);
            }
            StatusResponse statusResponse = new StatusResponse(entityType, (String) auditData.get(HCX_SENDER_CODE), (String) auditData.get(RECIPIENT_CODE), (String) auditData.get(STATUS));
            Map<String,Object> statusResponseMap = JSONUtils.convert(statusResponse, HashMap.class);
            if (auditData.get(STATUS).equals(QUEUED_STATUS)) {
                response.setResult(statusResponseMap);
            } else if (auditData.get(STATUS).equals(DISPATCHED_STATUS)) {
                response.setResult(statusResponseMap);
                eventHandler.processAndSendEvent(topic, request);
            }
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

    @PostMapping(HCX_ONSTATUS)
    public ResponseEntity<Object> onStatus(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, HCX_ONSTATUS);
        Response response = new Response(request);
        try {
            eventHandler.processAndSendEvent(topic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(request, response, e);
        }
    }

}
