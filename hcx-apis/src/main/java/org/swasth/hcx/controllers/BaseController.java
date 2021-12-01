package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.swasth.common.StringUtils;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ResponseCode;
import org.swasth.hcx.helpers.KafkaEventGenerator;
import org.swasth.kafka.client.KafkaClient;

import java.util.*;
import java.util.stream.Collectors;

public class BaseController {

    @Value("${kafka.topic.ingest}")
    private String ingestTopic;

    @Value("${kafka.topic.payload}")
    private String payloadTopic;

    @Value("${gateway.mode}")
    private Boolean gatewayMode;

    private String key="";

    @Autowired
    KafkaEventGenerator kafkaEventGenerator;

    @Autowired
    Environment env;

    @Autowired
    KafkaClient kafkaClient;

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }

    public void validateRequestBody(Map<String, Object> requestBody) throws Exception {
        if(requestBody.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_EMPTY_REQ_BODY, "Request Body cannot be Empty.");
        } else {
            // validating payload properties
            List<String> mandatoryPayloadProps = (List<String>) env.getProperty("payload.mandatory.properties", List.class, new ArrayList<String>());
            List<String> missingPayloadProps = mandatoryPayloadProps.stream().filter(key -> !requestBody.containsKey(key)).collect(Collectors.toList());
            if (!missingPayloadProps.isEmpty()) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "Payload mandatory properties are missing: " + missingPayloadProps);
            }
            //validating protected headers
            Map<String, Object> protectedHeaders = StringUtils.decodeBase64String((String) requestBody.get("protected"));
            List<String> mandatoryHeaders = new ArrayList<>();
            mandatoryHeaders.addAll((List<String>) env.getProperty("protocol.headers.mandatory", List.class, new ArrayList<String>()));
            mandatoryHeaders.addAll((List<String>) env.getProperty("headers.domain", List.class, new ArrayList<String>()));
            mandatoryHeaders.addAll((List<String>) env.getProperty("headers.jose", List.class, new ArrayList<String>()));
            List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !protectedHeaders.containsKey(key)).collect(Collectors.toList());
            if(!missingHeaders.isEmpty()) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_HEADER, "Mandatory headers are missing: " + missingHeaders);
            }
        }
    }

    public Response getHealthResponse(){
        Response response = new Response();
        Map<String, Object> result = new HashMap<>();
        response.setResult(result);
        return response;
    }

    public Response getResponse(String correlationId){
        return new Response(correlationId);
    }

    public Response errorResponse(Response response, ErrorCodes code, Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    public void processAndSendEvent(String apiAction, Map<String, Object> requestBody) throws Exception {
        String mid = getUUID();
        String payloadEvent = kafkaEventGenerator.generatePayloadEvent(mid, requestBody);
        String metadataEvent = kafkaEventGenerator.generateMetadataEvent(mid, apiAction, requestBody);
        if(gatewayMode) {
            kafkaClient.send(payloadTopic, "", payloadEvent);
            kafkaClient.send(ingestTopic, "", metadataEvent);
        }
    }
}
