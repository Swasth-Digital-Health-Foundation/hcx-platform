package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.utils.Constants;

import java.util.Map;

@RestController()
public class PaymentsController extends BaseController {

    @Value("${kafka.topic.payment}")
    private String kafkaTopic;

    @PostMapping(Constants.PAYMENT_NOTICE_REQUEST)
    public ResponseEntity<Object> paymentNoticeRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.PAYMENT_NOTICE_REQUEST, kafkaTopic);
    }

    @PostMapping(Constants.PAYMENT_NOTICE_ONREQUEST)
    public ResponseEntity<Object> paymentNoticeOnRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return validateReqAndPushToKafka(requestBody, Constants.PAYMENT_NOTICE_ONREQUEST, kafkaTopic);
    }

}
