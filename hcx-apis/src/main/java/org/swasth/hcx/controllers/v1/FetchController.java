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
public class FetchController extends BaseController {

    @Value("${kafka.topic.fetch}")
    private String kafkaTopic;

    @PostMapping(Constants.EOB_FETCH)
    public ResponseEntity<Object> eobFetch(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.EOB_FETCH, kafkaTopic);
    }

    @PostMapping(Constants.EOB_ON_FETCH)
    public ResponseEntity<Object> eobOnFetch(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.EOB_ON_FETCH, kafkaTopic);
    }

}
