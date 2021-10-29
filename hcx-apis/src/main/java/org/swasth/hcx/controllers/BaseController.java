package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.swasth.hcx.Helpers.KafkaEventGenerator;
import org.swasth.hcx.middleware.KafkaClient;
import org.swasth.hcx.pojos.Response;
import org.swasth.hcx.pojos.ResponseParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaseController {

    @Value("${kafka.denormaliser.topic}")
    private String topic;

    @Value("${kafka.denormaliser.topic.key}")
    private String key;

    @Autowired
    KafkaEventGenerator kafkaEventGenerator;

    @Autowired
    KafkaClient kafkaClient;

    @Autowired
    Environment env;

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }

    public void validatePayload(Map<String, Object> requestBody) {
        if(requestBody.isEmpty()) {
            throw new RestClientException("Request Body cannot be Empty.");
        } else {
            List<String> mandatoryPayloadProps = (List<String>) env.getProperty("payload.mandatory.properties", List.class, new ArrayList<String>());
            List<String> missingPayloadProps = mandatoryPayloadProps.stream().filter(key -> mandatoryPayloadProps.contains(key)).collect(Collectors.toList());
           if(!missingPayloadProps.isEmpty()) {
               throw new RestClientException("Payload mandatory properties are missing: " + missingPayloadProps);
           }
        }
    }

    public void validateHeaders(HttpHeaders header) {
        List<String> mandatoryProtocolHeaders = (List<String>) env.getProperty("protocol.mandatory.headers", List.class, new ArrayList<String>());
        List<String> mandatoryJoseHeaders = (List<String>) env.getProperty("jose.headers", List.class, new ArrayList<String>());
        List<String> missingJoseHeaders = mandatoryJoseHeaders.stream().filter(key -> !header.containsKey(key)).collect(Collectors.toList());
        if(!missingJoseHeaders.isEmpty()) {
            throw new RestClientException("Jose mandatory headers are missing: " + missingJoseHeaders);
        }
        List<String> missingProtocolHeaders = mandatoryProtocolHeaders.stream().filter(key -> !header.containsKey(key)).collect(Collectors.toList());
        if(!missingProtocolHeaders.isEmpty()) {
            throw new RestClientException("Protocol mandatory headers are missing: " + missingProtocolHeaders);
        }
    }

    public Response getResponse(String apiId){
        ResponseParams responseParams = new ResponseParams();
        Response response = new Response(apiId, "ACCEPTED", responseParams);
        return response;
    }

    public Response getErrorResponse(Response response, Exception e){
        ResponseParams responseParams = new ResponseParams();
        responseParams.setStatus(Response.Status.UNSUCCESSFUL);
        responseParams.setErrmsg(e.getMessage());
        response.setParams(responseParams);
        response.setResponseCode(HttpStatus.BAD_REQUEST.toString());
        return response;
    }

    public void processAndSendEvent(String apiId, HttpHeaders header, Map<String, Object> requestBody) throws JsonProcessingException {
        String mid = getUUID();
        String payloadEvent = kafkaEventGenerator.generatePayloadEvent(mid, requestBody);
        String metadataEvent = kafkaEventGenerator.generateMetadataEvent(mid, apiId, header);
        kafkaClient.send(topic, key, payloadEvent);
        kafkaClient.send(topic, key, metadataEvent);
    }
}
