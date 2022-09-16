package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.AuditSearchRequest;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class CommunicationController extends BaseController {

    @Value("${kafka.topic.communication}")
    private String kafkaTopic;

    @PostMapping(COMMUNICATION_REQUEST)
    public ResponseEntity<Object> communicationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, COMMUNICATION_REQUEST);
        Response response = new Response(request);
        try {
            Map<String, Object> hcxHeaders = request.getHcxHeaders();
            Map<String, String> filters = new HashMap<>();
            filters.put(CORRELATION_ID,request.getCorrelationId());
            List<Map<String,Object>> auditResponse = auditService.search(new AuditSearchRequest(filters), COMMUNICATION_REQUEST);
            if(auditResponse.isEmpty()){
                throw new ClientException(ErrorCodes.ERR_INVALID_CORRELATION_ID,"Invalid Correlation Id");
            }
            Map<String,Object> auditData = auditResponse.get(0);
            if (auditData.get(WORKFLOW_ID) != null && (!hcxHeaders.containsKey(WORKFLOW_ID) || !request.getWorkflowId().equals(auditData.get(WORKFLOW_ID)))) {
                throw new ClientException(ErrorCodes.ERR_INVALID_WORKFLOW_ID, "The request contains invalid workflow id");
            }
            eventHandler.processAndSendEvent(kafkaTopic, request);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandlerWithAudit(request, response, e);
        }
    }

    @PostMapping(COMMUNICATION_ONREQUEST)
    public ResponseEntity<Object> communicationOnRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, COMMUNICATION_ONREQUEST, kafkaTopic);
    }
}
