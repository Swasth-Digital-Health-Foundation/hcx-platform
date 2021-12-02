package org.swasth.hcx.controllers.v1;

import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;

@RestController()
@RequestMapping(value = "/v1/preauth")
public class PreAuthController extends BaseController {

    /*@RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
       String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
       Response response = getResponse(correlationId);
       try {
           validateRequestBody(requestBody);
           processAndSendEvent(Constants.PRE_AUTH_SUBMIT, requestBody);
           return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
       } catch (ClientException e) {
           return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
       } catch (Exception e) {
           return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @RequestMapping(value = "/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PRE_AUTH_ONSUBMIT, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PRE_AUTH_SEARCH, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSearch(@RequestBody Map<String, Object> requestBody) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        Response response = getResponse(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PRE_AUTH_ONSEARCH, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
}