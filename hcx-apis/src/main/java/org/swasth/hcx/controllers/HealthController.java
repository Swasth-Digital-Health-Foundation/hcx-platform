package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
        Response response = new Response(Constants.HEALTHY, true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(Constants.HEALTH)
    public ResponseEntity<Object> health() {
        return new ResponseEntity<>(healthCheckManager.checkAllSystemHealth(), HttpStatus.OK);
    }



}
