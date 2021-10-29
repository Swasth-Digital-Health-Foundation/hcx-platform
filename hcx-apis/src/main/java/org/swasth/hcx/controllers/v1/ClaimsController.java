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
@RequestMapping(value = "/v1")
public class ClaimsController extends BaseController {

    @RequestMapping(value = "/preauth/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSubmit(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PRE_AUTH_SUBMIT);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PRE_AUTH_SUBMIT, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/preauth/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSubmit(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PRE_AUTH_ONSUBMIT);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PRE_AUTH_ONSUBMIT, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/preauth/search", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthSearch(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PRE_AUTH_SEARCH);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PRE_AUTH_SEARCH, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/preauth/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> preAuthOnSearch(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.PRE_AUTH_ONSEARCH);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.PRE_AUTH_ONSEARCH, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/claim/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> claimSubmit(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.CLAIM_SUBMIT);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.CLAIM_SUBMIT, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/claim/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> claimOnSubmit(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.CLAIM_ONSUBMIT);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.CLAIM_ONSUBMIT, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/claim/search", method = RequestMethod.POST)
    public ResponseEntity<Object> claimSearch(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.CLAIM_SEARCH);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.CLAIM_SEARCH, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/claim/on_search", method = RequestMethod.POST)
    public ResponseEntity<Object> claimOnSearch(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.CLAIM_ONSEARCH);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.CLAIM_ONSEARCH, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }
}
