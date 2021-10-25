package com.hcx.apis.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutionException;

@RestController
public class HealthController {

    @Autowired
    private KafkaAdminOperations kafkaAdminOperations;
    @Autowired
    Environment env;

    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = { "application/json", "text/json" })
    public ResponseEntity<JsonNode> health() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree("{\"id\":\"api.hcx.health\",\"ver\":\"1.0\",\"params\":{\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"healthy\":true}}");
        return ResponseEntity.ok(json);
    }

    @RequestMapping(value = "/service/health", method = RequestMethod.GET, produces = { "application/json", "text/json" })
    public ResponseEntity<JsonNode> serviceHealth() throws JsonProcessingException, ExecutionException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json;
        String topicName = env.getProperty("${kafka.topic}");
        try{
            NewTopic newTopic = new NewTopic(topicName, 1, (short)1);
            kafkaAdminOperations.createOrModifyTopics(newTopic);
            json = mapper.readTree("{\"id\":\"api.hcx.service.health\",\"ver\":\"1.0\",\"params\":{\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"service healthy\":true}}");
        }
        catch (Exception e) {
            json = mapper.readTree("{\"id\":\"api.hcx.service.health\",\"ver\":\"1.0\",\"params\":{\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"service healthy\":false}}");
        }
        return ResponseEntity.ok(json);
    }

}
