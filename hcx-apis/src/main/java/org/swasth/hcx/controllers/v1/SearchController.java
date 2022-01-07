package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.utils.Constants;

import java.util.Map;

@RestController
@RequestMapping(value = "/v1/hcx")
public class SearchController extends BaseController {

    @Value("${kafka.topic.search}")
    private String topic;

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Object> search (@RequestBody Map<String, Object> body) throws Exception {
        return validateReqAndPushToKafka(body, Constants.HCX_SEARCH, topic);
    }

    @RequestMapping(value = "/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> onSearch (@RequestBody Map<String, Object> body) throws Exception {
        return validateReqAndPushToKafka(body, Constants.HCX_SEARCH, topic);
    }
}
