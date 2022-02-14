package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/communication")
public class CommunicationController extends BaseController {

    @Value("${kafka.topic.communication}")
    private String kafkaTopic;

    @RequestMapping(value = "/request", method = RequestMethod.POST)
    public ResponseEntity<Object> communicationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.COMMUNICATION_REQUEST, kafkaTopic);
    }

    @RequestMapping(value = "/on_request", method = RequestMethod.POST)
    public ResponseEntity<Object> communicationOnRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.COMMUNICATION_ONREQUEST, kafkaTopic);
    }
}
