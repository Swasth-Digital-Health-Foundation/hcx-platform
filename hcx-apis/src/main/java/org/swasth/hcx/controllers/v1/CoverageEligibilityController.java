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
@RequestMapping(value = "/v1/coverageeligibility")
public class CoverageEligibilityController extends BaseController {

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<Object> checkCoverageEligibility(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.COVERAGE_ELIGIBILITY_CHECK);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.COVERAGE_ELIGIBILITY_CHECK, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/on_check", method = RequestMethod.POST)
    public ResponseEntity<Object> onCheckCoverageEligibility(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        Response response = getResponse(ApiId.COVERAGE_ELIGIBILITY_ONCHECK);
        try {
            validateHeaders(header);
            validatePayload(requestBody);
            processAndSendEvent(ApiId.COVERAGE_ELIGIBILITY_ONCHECK, header, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(getErrorResponse(response, e), HttpStatus.BAD_REQUEST);
        }
    }
}
