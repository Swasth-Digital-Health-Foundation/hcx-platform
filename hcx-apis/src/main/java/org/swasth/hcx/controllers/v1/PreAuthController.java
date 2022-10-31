package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class PreAuthController extends BaseController {

    @Value("${kafka.topic.preauth}")
    private String kafkaTopic;

    @PostMapping(Constants.PRE_AUTH_SUBMIT)
    public ResponseEntity<Object> preAuthSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.PRE_AUTH_SUBMIT, kafkaTopic);
    }

    @PostMapping(Constants.PRE_AUTH_ONSUBMIT)
    public ResponseEntity<Object> preAuthOnSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.PRE_AUTH_ONSUBMIT, kafkaTopic);
    }

}