package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.utils.Constants;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/preauth")
public class PreAuthController extends BaseController {

    @Value("${kafka.topic.preauth}")
    private String kafkaTopic;

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.PRE_AUTH_SUBMIT, kafkaTopic);
    }

    @RequestMapping(value = "/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.PRE_AUTH_ONSUBMIT, kafkaTopic);
    }

}