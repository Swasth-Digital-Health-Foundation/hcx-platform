package org.swasth.hcx.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.common.JsonUtils;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.hcx.utils.Constants;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.util.*;
import java.util.stream.Collectors;

public class BaseController {

    @Autowired
    private EventGenerator eventGenerator;

    @Autowired
    protected Environment env;

    @Autowired
    private IEventService kafkaClient;

    @Autowired
    private IDatabaseService postgreSQLClient;

    private void validateRequestBody(Map<String, Object> requestBody) throws Exception {
        // validating payload properties
        List<String> mandatoryPayloadProps = env.getProperty(Constants.PAYLOAD_MANDATORY_PROPERTIES, List.class);
        List<String> missingPayloadProps = mandatoryPayloadProps.stream().filter(key -> !requestBody.containsKey(key)).collect(Collectors.toList());
        if (!missingPayloadProps.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "Payload mandatory properties are missing: " + missingPayloadProps);
        }
        //validating protected headers
        Map<String, Object> protectedHeaders = JsonUtils.decodeBase64String((String) requestBody.get("protected"), HashMap.class);
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty(Constants.PROTOCOL_HEADERS_MANDATORY, List.class));
        mandatoryHeaders.addAll(env.getProperty(Constants.JOSE_HEADERS, List.class));
        List<String> missingHeaders = mandatoryHeaders.stream().filter(key -> !protectedHeaders.containsKey(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_PROTECTED, "Mandatory headers are missing: " + missingHeaders);
        }
        validateProtocolHeadersFormat(protectedHeaders);
    }

    private void validateProtocolHeadersFormat(Map<String, Object> protectedHeaders) throws ClientException {
        if (StringUtils.isEmpty((String) protectedHeaders.get(Constants.SENDER_CODE))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender code cannot be null or empty!");
        }
        else if (StringUtils.isEmpty((String) protectedHeaders.get(Constants.RECIPIENT_CODE))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipent code cannot be null or empty!");
        }
        else if (StringUtils.isEmpty((String) protectedHeaders.get(Constants.REQUEST_ID))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_ID, "Request id cannot be null or empty!");
        }
        else if (StringUtils.isEmpty((String) protectedHeaders.get(Constants.CORRELATION_ID))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CORREL_ID, "Correlation id cannot be null or empty!");
        }
        else if (protectedHeaders.containsKey(Constants.WORKFLOW_ID) && StringUtils.isEmpty((String) protectedHeaders.get(Constants.WORKFLOW_ID))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null or empty!");
        }
        else if (StringUtils.isEmpty((String) protectedHeaders.get(Constants.TIMESTAMP))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Timestamp cannot be null or empty!");
        }
        else if (protectedHeaders.containsKey(Constants.DEBUG_FLAG) && (StringUtils.isEmpty((String) protectedHeaders.get(Constants.DEBUG_FLAG)) || !Constants.DEBUG_FLAG_VALUES.contains((String) protectedHeaders.get(Constants.DEBUG_FLAG)))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_DEBUG_ID, "Debug flag cannot be null, empty or other than Error, Info and Debug");
        }
        else if (StringUtils.isEmpty((String) protectedHeaders.get(Constants.STATUS))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_STATUS, "Status cannot be null or empty!");
        }
        else if (protectedHeaders.containsKey(Constants.ERROR_DETAILS) && StringUtils.isEmpty((String) protectedHeaders.get(Constants.ERROR_DETAILS))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_ERROR_DETAILS, "Error details cannot be null or empty!");
        }
        else if (protectedHeaders.containsKey(Constants.DEBUG_DETAILS) && StringUtils.isEmpty((String) protectedHeaders.get(Constants.DEBUG_DETAILS))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_DEBUG_DETAILS, "Debug details cannot be null or empty!");
        }
    }

    private Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    private void processAndSendEvent(String apiAction, String metadataTopic, Map<String, Object> requestBody) throws Exception {
        String mid = UUID.randomUUID().toString();
        String serviceMode = env.getProperty(Constants.SERVICE_MODE);
        String payloadTopic = env.getProperty(Constants.KAFKA_TOPIC_PAYLOAD);
        String key = JsonUtils.decodeBase64String((String) requestBody.get(Constants.PROTECTED), HashMap.class).get(Constants.SENDER_CODE).toString();
        String payloadEvent = eventGenerator.generatePayloadEvent(mid, requestBody);
        String metadataEvent = eventGenerator.generateMetadataEvent(mid, apiAction, requestBody);
        System.out.println("Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + metadataEvent);
        if(serviceMode.equals(Constants.GATEWAY)) {
            kafkaClient.send(payloadTopic, key, payloadEvent);
            kafkaClient.send(metadataTopic, key, metadataEvent);
            postgreSQLClient.insert(mid, JsonUtils.serialize(requestBody));
        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String kafkaTopic) throws Exception {
        String correlationId = JsonUtils.decodeBase64String((String) requestBody.get(Constants.PROTECTED), HashMap.class).get(Constants.CORRELATION_ID).toString();
        Response response = new Response(correlationId);
        try {
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException("Service is unavailable");
            validateRequestBody(requestBody);
            processAndSendEvent(apiAction, kafkaTopic , requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (ServiceUnavailbleException e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVICE_UNAVAILABLE , e), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (ServerException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
