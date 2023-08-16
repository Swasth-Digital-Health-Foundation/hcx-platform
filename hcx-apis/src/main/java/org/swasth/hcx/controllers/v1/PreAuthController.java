package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class PreAuthController extends BaseController {

    @Value("${kafka.topic.preauth}")
    private String kafkaTopic;

    @PostMapping(Constants.PRE_AUTH_SUBMIT)
    public ResponseEntity<Object> preAuthSubmit(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(headers, requestBody, Constants.PRE_AUTH_SUBMIT, kafkaTopic);
    }

    @PostMapping(Constants.PRE_AUTH_ONSUBMIT)
    public ResponseEntity<Object> preAuthOnSubmit(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(headers, requestBody, Constants.PRE_AUTH_ONSUBMIT, kafkaTopic);
    }

}