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
