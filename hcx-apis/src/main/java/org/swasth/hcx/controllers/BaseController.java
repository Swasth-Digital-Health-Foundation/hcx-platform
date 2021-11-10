package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ResponseCode;
import org.swasth.hcx.helpers.KafkaEventGenerator;
import org.swasth.common.StringUtils;
import org.swasth.kafka.client.KafkaClient;

import java.util.*;
import java.util.stream.Collectors;

public class BaseController {

    @Value("${kafka.denormaliser.topic}")
    private String topic;

    @Value("${kafka.denormaliser.topic.key}")
    private String key;

    @Autowired
    KafkaEventGenerator kafkaEventGenerator;

    @Autowired
    Environment env;

    KafkaClient kafkaClient = new KafkaClient();

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }

    public void validateRequestBody(Map<String, Object> requestBody) throws ClientException, JsonProcessingException {
        if(requestBody.isEmpty()) {
            throw new ClientException("Request Body cannot be Empty.");
        } else {
            // validating payload properties
            List<String> mandatoryPayloadProps = (List<String>) env.getProperty("payload.mandatory.properties", List.class, new ArrayList<String>());
            List<String> missingPayloadProps = mandatoryPayloadProps.stream().filter(key -> !requestBody.keySet().contains(key)).collect(Collectors.toList());
            if (!missingPayloadProps.isEmpty()) {
                throw new ClientException("Payload mandatory properties are missing: " + missingPayloadProps);
            }
            //validating protected headers
            Map<String, Object> protectedHeaders = StringUtils.decodeBase64String((String) requestBody.get("protected"));
            List<String> mandatoryHeaders = new ArrayList<>();
            mandatoryHeaders.addAll((List<String>) env.getProperty("protocol.mandatory.headers", List.class, new ArrayList<String>()));
            mandatoryHeaders.addAll((List<String>) env.getProperty("domain.headers", List.class, new ArrayList<String>()));
            mandatoryHeaders.addAll((List<String>) env.getProperty("jose.headers", List.class, new ArrayList<String>()));
            List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !protectedHeaders.containsKey(key)).collect(Collectors.toList());
            if(!missingHeaders.isEmpty()) {
                throw new ClientException("Mandatory headers are missing: " + missingHeaders);
            }
        }
    }

    public Map<String, Object> successResponse(String correlationId){
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("correlation_id", correlationId);
        return response;
    }

    public Map<String, Object> errorResponse(String correlationId, ResponseCode code, Exception e){
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("correlation_id", correlationId);
        response.put("error", new LinkedHashMap() {{
            put("code", code);
            put("message", e.getMessage());
            put("trace", e.getCause());
        }});
        return response;
    }

    public void processAndSendEvent(String apiAction, Map<String, Object> requestBody) throws JsonProcessingException, ClientException {
        String mid = getUUID();
        String payloadEvent = kafkaEventGenerator.generatePayloadEvent(mid, requestBody);
        String metadataEvent = kafkaEventGenerator.generateMetadataEvent(mid, apiAction, requestBody);
        kafkaClient.send(topic, key, payloadEvent);
        kafkaClient.send(topic, key, metadataEvent);
    }
}
