package org.swasth.hcx.controllers.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

@RestController()
@RequestMapping(Constants.VERSION_PREFIX)
public class RetryController extends BaseController {

    @PostMapping(Constants.REQUEST_RETRY)
    public ResponseEntity<Object> retryRequest(@PathVariable(Constants.MID) String mid) {
        Response response = new Response();
        try{
            eventHandler.createRetryEvent(mid);
            response.setStatus(Constants.SUCCESSFUL);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

}
