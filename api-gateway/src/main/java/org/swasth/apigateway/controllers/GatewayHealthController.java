package org.swasth.apigateway.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;


@RestController
public class GatewayHealthController {

    private final RestTemplate restTemplate;

    public GatewayHealthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(Constants.GATEWAY_SERVICE_HEALTH)
    public ResponseEntity<Object> serviceHealth() {
        Response response = new Response(Constants.HEALTHY, true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(Constants.GATEWAY_HEALTH)
    public ResponseEntity<Object> health() {
        try {
            ResponseEntity<String> healthResponse = restTemplate.getForEntity(Constants.HCX_APIs_HEALTH, String.class);
            if (healthResponse.getStatusCode() == HttpStatus.OK) {
                return new ResponseEntity<>(new Response(Constants.HCX_APIs, true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new Response("Dependency service is down", false), HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(Constants.HCX_APIs, false), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}