package org.swasth.hcx.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.managers.HealthCheckManager;

@RestController
public class HealthController extends BaseController {

    @Autowired
    private HealthCheckManager healthCheckManager;

    @GetMapping(Constants.SERVICE_HEALTH)
    public ResponseEntity<Object> serviceHealth() {
        Response response = getHealthResponse();
        response.put("healthy", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(Constants.HEALTH)
    public ResponseEntity<Object> health() {
        Response response = getHealthResponse();
        List<Map<String,Object>> allChecks = new ArrayList<>();
        List<Boolean> getAllHealth = new ArrayList<>();
        allChecks.add(checkKafkaHealth());
        allChecks.forEach(check -> getAllHealth.add((Boolean) check.get("healthy")));
        response.put("checks", allChecks);
        response.put("healthy", !getAllHealth.contains(false));
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
