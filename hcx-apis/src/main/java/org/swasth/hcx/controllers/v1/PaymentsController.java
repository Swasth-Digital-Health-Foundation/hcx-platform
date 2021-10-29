package org.swasth.hcx.controllers.v1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.pojos.Response;
import org.swasth.hcx.utils.ApiId;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/paymentnotice")
public class PaymentsController extends BaseController {

    @RequestMapping(value = "/request", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeRequest(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PAYMENT_NOTICE_REQUEST);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PAYMENT_NOTICE_REQUEST, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/on_request", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeOnRequest(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PAYMENT_NOTICE_ONREQUEST);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PAYMENT_NOTICE_ONREQUEST, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeSearch(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PAYMENT_NOTICE_SEARCH);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PAYMENT_NOTICE_SEARCH, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeOnSearch(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PAYMENT_NOTICE_ONSEARCH);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PAYMENT_NOTICE_ONSEARCH, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }
}
