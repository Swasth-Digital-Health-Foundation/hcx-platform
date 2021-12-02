package org.swasth.hcx.controllers.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.utils.Constants;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/coverageeligibility")
public class CoverageEligibilityController extends BaseController {

    String kafkaTopic = env.getProperty(Constants.KAFKA_TOPIC_COVERAGE_ELIGIBILITY);

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<Object> checkCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.COVERAGE_ELIGIBILITY_CHECK, kafkaTopic);
    }

    @RequestMapping(value = "/on_check", method = RequestMethod.POST)
    public ResponseEntity<Object> onCheckCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.COVERAGE_ELIGIBILITY_ONCHECK, kafkaTopic);
    }
}
