package org.swasth.hcx.controllers.v1;

import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;

@RestController()
@RequestMapping(value = "/v1/paymentnotice")
public class PaymentsController extends BaseController {

    /*@RequestMapping(value = "/request", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PAYMENT_NOTICE_REQUEST, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/on_request", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeOnRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PAYMENT_NOTICE_ONREQUEST, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PAYMENT_NOTICE_SEARCH, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentNoticeOnSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PAYMENT_NOTICE_ONSEARCH, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
}
