package org.swasth.hcx.controllers.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.StringUtils;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ResponseCode;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.utils.Constants;

import java.util.Map;

@RestController()
@RequestMapping(value = "/v1/coverageeligibility")
public class CoverageEligibilityController extends BaseController {

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<Object> checkCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.COVERAGE_ELIGIBILITY_CHECK, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/on_check", method = RequestMethod.POST)
    public ResponseEntity<Object> onCheckCoverageEligibility(@RequestBody Map<String, Object> requestBody) throws JsonProcessingException {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.getOrDefault("protected","e30=")).getOrDefault("x-hcx-correlation_id","").toString();
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(Constants.COVERAGE_ELIGIBILITY_ONCHECK, requestBody);
            return new ResponseEntity<>(successResponse(correlationId), HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.CLIENT_ERROR, e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(errorResponse(correlationId, ResponseCode.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
