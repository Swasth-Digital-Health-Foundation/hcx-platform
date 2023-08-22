package org.swasth.hcx.controllers.v1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Objects;

import static org.swasth.common.utils.Constants.AUTHORIZATION;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class RetryController extends BaseController {

    @PostMapping(Constants.REQUEST_RETRY)
    public ResponseEntity<Object> retryRequest(@RequestHeader HttpHeaders headers, @PathVariable(Constants.MID) String mid) {
        Response response = new Response();
        try{
            eventHandler.createRetryEvent(Objects.requireNonNull(headers.get(AUTHORIZATION)).get(0), mid);
            response.setStatus(Constants.SUCCESSFUL);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

}
