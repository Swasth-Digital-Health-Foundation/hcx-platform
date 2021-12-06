package org.swasth.hcx.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Response;
import org.swasth.hcx.utils.Constants;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class HealthController extends BaseController {

    @RequestMapping(value = "/service/health", method = RequestMethod.GET)
    public ResponseEntity<Object> serviceHealth() {
        Response response = new Response(Constants.HEALTHY, true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<Object> health() {
        List<Map<String,Object>> allChecks = new ArrayList<>();
        boolean allServiceHealthResult = true;
        allChecks.add(generateCheck(Constants.KAFKA, kafkaClient.health()));
        for(Map<String,Object> check:allChecks) {
            if(!(boolean)check.get(Constants.HEALTHY))
                allServiceHealthResult = false;
        }
        Response response = new Response(Constants.CHECKS, allChecks);
        response.put(Constants.HEALTHY, allServiceHealthResult);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Map<String,Object> generateCheck(String serviceName, boolean health){
        return new HashMap<>() {{
            put(Constants.NAME, serviceName);
            put(Constants.HEALTHY, health);
        }};
    }

}
