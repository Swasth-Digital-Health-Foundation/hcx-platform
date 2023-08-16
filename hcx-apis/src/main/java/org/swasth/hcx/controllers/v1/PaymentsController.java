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
public class PaymentsController extends BaseController {

    @Value("${kafka.topic.payment}")
    private String kafkaTopic;

    @PostMapping(Constants.PAYMENT_NOTICE_REQUEST)
    public ResponseEntity<Object> paymentNoticeRequest(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(headers, requestBody, Constants.PAYMENT_NOTICE_REQUEST, kafkaTopic);
    }

    @PostMapping(Constants.PAYMENT_NOTICE_ONREQUEST)
    public ResponseEntity<Object> paymentNoticeOnRequest(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(headers, requestBody, Constants.PAYMENT_NOTICE_ONREQUEST, kafkaTopic);
    }

}
