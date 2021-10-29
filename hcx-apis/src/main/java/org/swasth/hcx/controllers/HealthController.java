package org.swasth.hcx.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.exception.ResponseCode;
import org.swasth.hcx.pojos.Response;
import org.swasth.hcx.utils.ApiId;

import java.util.*;

@RestController
public class HealthController extends BaseController {

    @RequestMapping(value = "/service/health", method = RequestMethod.GET)
    public ResponseEntity<Object> serviceHealth() {
        Response response = getResponse(ApiId.APPLICATION_SERVICE_HEALTH);
        response.put("healthy",true);
        response.setResponseCode(ResponseCode.OK);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<Object> health() {
        Response response = getResponse(ApiId.APPLICATION_HEALTH);
        List<Map<String,Object>> allChecks = new ArrayList<>();
        List<Boolean> getAllHealth = new ArrayList<>();
        allChecks.add(checkKafkaHealth());
        allChecks.forEach(check -> getAllHealth.add((Boolean) check.get("healthy")));
        response.put("checks", allChecks);
        response.put("healthy", !getAllHealth.contains(false));
        response.setResponseCode(ResponseCode.OK);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Map checkKafkaHealth(){
        String serviceName = "kafka";
        if(kafkaClient.health()){
            return generateCheck(serviceName, true);
        } else {
            return generateCheck(serviceName, false);
        }
    }

    public Map generateCheck(String serviceName, Boolean health){
        return new LinkedHashMap(){{
           put("name", serviceName);
           put("healthy", health);
        }};
    }

}
