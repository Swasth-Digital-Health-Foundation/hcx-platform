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
public class PredeterminationController extends BaseController {

    @Value("${kafka.topic.predetermination}")
    private String kafkaTopic;

    @PostMapping(Constants.PREDETERMINATION_SUBMIT)
    public ResponseEntity<Object> submitPredetermination(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(headers, requestBody, Constants.PREDETERMINATION_SUBMIT, kafkaTopic);
    }

    @PostMapping(Constants.PREDETERMINATION_ONSUBMIT)
    public ResponseEntity<Object> onSubmitPredetermination(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(headers, requestBody, Constants.PREDETERMINATION_ONSUBMIT, kafkaTopic);
    }
}
