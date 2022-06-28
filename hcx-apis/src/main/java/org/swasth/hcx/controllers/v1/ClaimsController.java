package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
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
public class ClaimsController extends BaseController {

    @Value("${kafka.topic.claim}")
    private String kafkaTopic;

    @PostMapping(Constants.CLAIM_SUBMIT)
    public ResponseEntity<Object> claimSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.CLAIM_SUBMIT, kafkaTopic);
    }

    @PostMapping(Constants.CLAIM_ONSUBMIT)
    public ResponseEntity<Object> claimOnSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.CLAIM_ONSUBMIT, kafkaTopic);
    }

}
