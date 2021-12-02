package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.common.StringUtils;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.utils.Constants;
import org.swasth.kafka.client.KafkaClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaseController {

    @Autowired
    EventGenerator eventGenerator;

    @Autowired
    Environment env;

    @Autowired
    KafkaClient kafkaClient;

    private String getUUID() {
        return UUID.randomUUID().toString();
    }

    private void validateRequestBody(Map<String, Object> requestBody) throws Exception {
        // validating payload properties
        List<String> mandatoryPayloadProps = env.getProperty(Constants.PAYLOAD_MANDATORY_PROPERTIES, List.class);
        List<String> missingPayloadProps = mandatoryPayloadProps.stream().filter(key -> !requestBody.containsKey(key)).collect(Collectors.toList());
        if (!missingPayloadProps.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "Payload mandatory properties are missing: " + missingPayloadProps);
        }
        //validating protected headers
        Map<String, Object> protectedHeaders = StringUtils.decodeBase64String((String) requestBody.get("protected"));
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty(Constants.PROTOCOL_HEADERS_MANDATORY, List.class));
        mandatoryHeaders.addAll(env.getProperty(Constants.DOMAIN_HEADERS, List.class));
        mandatoryHeaders.addAll(env.getProperty(Constants.JOSE_HEADERS, List.class));
        List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !protectedHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_HEADER, "Mandatory headers are missing: " + missingHeaders);
        }

    }

    private Response errorResponse(Response response, ErrorCodes code, Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    private void processAndSendEvent(String apiAction, Map<String, Object> requestBody) throws Exception {
        String mid = getUUID();
        String serviceMode = env.getProperty(Constants.SERVICE_MODE);
        String payloadTopic = env.getProperty(Constants.KAFKA_TOPIC_PAYLOAD);
        String eligibilityCheckTopic = env.getProperty(Constants.KAFKA_TOPIC_ELIGIBILITY_CHECK);
        String ingestTopic;
        String payloadEvent = eventGenerator.generatePayloadEvent(mid, requestBody);
        String metadataEvent = eventGenerator.generateMetadataEvent(mid, apiAction, requestBody);
        if(serviceMode.equals(Constants.GATEWAY)) {
            kafkaClient.send(payloadTopic, "", payloadEvent);
            switch(apiAction){
                case Constants.COVERAGE_ELIGIBILITY_CHECK:
                case Constants.COVERAGE_ELIGIBILITY_ONCHECK:
                    ingestTopic = eligibilityCheckTopic;
                    break;
                default: ingestTopic = "Invalid";
            }
            kafkaClient.send(ingestTopic, "", metadataEvent);
        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction) throws Exception {
        String correlationId = StringUtils.decodeBase64String((String) requestBody.get(Constants.PROTECTED)).get(Constants.HEADER_CORRELATION).toString();
        Response response = new Response(correlationId);
        try {
            validateRequestBody(requestBody);
            processAndSendEvent(apiAction, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
