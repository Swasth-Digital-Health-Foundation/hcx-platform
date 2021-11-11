package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.StringUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ResponseCode;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.utils.Constants;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1")
public class ClaimsController extends BaseController {

   @RequestMapping(value = "/preauth/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSubmit(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
       String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
       try {
           validateRequestBody(requestBody);
           processAndSendEvent(Constants.PRE_AUTH_SUBMIT, requestBody);
           return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
       } catch (ClientException e) {
           return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
       } catch (Exception e) {
           return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @RequestMapping(value = "/preauth/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSubmit(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PRE_AUTH_ONSUBMIT, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/preauth/search", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSearch(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PRE_AUTH_SEARCH, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/preauth/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSearch(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.PRE_AUTH_ONSEARCH, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/claim/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> claimSubmit(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.CLAIM_SUBMIT, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/claim/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> claimOnSubmit(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.CLAIM_ONSUBMIT, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/claim/search", method = RequestMethod.POST)
    public ResponseEntity<Object> claimSearch(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.CLAIM_SEARCH, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/claim/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> claimOnSearch(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.CLAIM_ONSEARCH, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
