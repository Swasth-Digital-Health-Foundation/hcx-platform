package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonParseException;
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
import java.util.HashMap;

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
            throw new ClientException(ErrorCodes.CLIENT_ERR_MANDATORY_HEADERFIELD_MISSING, "Mandatory headers are missing: " + missingHeaders);
        }
        validateProtocolHeadersFormat(protectedHeaders);
    }

    private void validateProtocolHeadersFormat(Map<String, Object> protectedHeaders) throws ClientException {
        if (!(protectedHeaders.get(Constants.SENDER_CODE) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.SENDER_CODE))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender code cannot be null, empty and other than 'String'");
        }
        if (!(protectedHeaders.get(Constants.RECIPIENT_CODE) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.RECIPIENT_CODE))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient code cannot be null, empty and other than 'String'");
        }
        if (!(protectedHeaders.get(Constants.REQUEST_ID) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.REQUEST_ID))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_ID, "Request id cannot be null, empty and other than 'String'");
        }
        if (protectedHeaders.containsKey(Constants.WORKFLOW_ID) && (!(protectedHeaders.get(Constants.WORKFLOW_ID) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.WORKFLOW_ID)))) {

            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null, empty and other than 'String'");
        }
        if (!(protectedHeaders.get(Constants.TIMESTAMP) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.TIMESTAMP))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Invalid timestamp");
        }
        if (protectedHeaders.containsKey(Constants.DEBUG_FLAG)) {
            if (!(protectedHeaders.get(Constants.DEBUG_FLAG) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.DEBUG_FLAG))) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_DEBUG_ID, "Debug flag cannot be null, empty and other than 'String'");
            } else if (!Constants.DEBUG_FLAG_VALUES.contains((String) protectedHeaders.get(Constants.DEBUG_FLAG))) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_DEBUG_ID_OUTOFRANGE, "Debug flag cannot be other than Error, Info or Debug");
            }
        }
        if (!(protectedHeaders.get(Constants.STATUS) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.STATUS))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_STATUS, "Status cannot be null, empty and other than 'String'");
        } else if (!Constants.STATUS_VALUES.contains((String) protectedHeaders.get(Constants.STATUS))){
            throw new ClientException(ErrorCodes.CLIENT_ERR_STATUS_OUTOFRANGE, "Status value can be only: " + Constants.STATUS_VALUES );
        }
        if (protectedHeaders.containsKey(Constants.ERROR_DETAILS)){
            if (!(protectedHeaders.get(Constants.ERROR_DETAILS) instanceof Map) || ((Map<String,Object>) protectedHeaders.get(Constants.ERROR_DETAILS)).isEmpty()) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_ERROR_DETAILS, "Error details cannot be null, empty and other than 'JSON Object'");
            } else if (!((Map<String,Object>) protectedHeaders.get(Constants.ERROR_DETAILS)).keySet().containsAll(Constants.ERROR_DETAILS_VALUES)){
                throw new ClientException(ErrorCodes.CLIENT_ERR_ERROR_DETAILS_OUTOFRANGE, "Error details should contain only: " + Constants.ERROR_DETAILS_VALUES);
            }
        }
        if (protectedHeaders.containsKey(Constants.DEBUG_DETAILS)){
            if (!(protectedHeaders.get(Constants.DEBUG_DETAILS) instanceof Map) || ((Map<String,Object>) protectedHeaders.get(Constants.DEBUG_DETAILS)).isEmpty()) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_DEBUG_DETAILS, "Debug details cannot be null, empty and other than 'JSON Object'");
            } else if (!((Map<String,Object>) protectedHeaders.get(Constants.DEBUG_DETAILS)).keySet().containsAll(Constants.ERROR_DETAILS_VALUES)){
                throw new ClientException(ErrorCodes.CLIENT_ERR_DEBUG_DETAILS_OUTOFRANGE, "Debug details should contain only: " + Constants.ERROR_DETAILS_VALUES);
            }
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
        if(StringUtils.equalsIgnoreCase(serviceMode, Constants.GATEWAY)) {
            kafkaClient.send(payloadTopic, key, payloadEvent);
            kafkaClient.send(metadataTopic, key, metadataEvent);
            postgreSQLClient.insert(mid, JsonUtils.serialize(requestBody));
        }
    }

    private String getCorrelationId(Map<String, Object> requestBody) throws Exception {
        try {
            Map protectedMap = JsonUtils.decodeBase64String((String) requestBody.get(Constants.PROTECTED), HashMap.class);
            if (!protectedMap.containsKey(Constants.CORRELATION_ID) || !(protectedMap.get(Constants.CORRELATION_ID) instanceof String) || ((String) protectedMap.get(Constants.CORRELATION_ID)).isEmpty()) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CORREL_ID, "Correlation id cannot be null, empty and other than 'String'");
            }
            return protectedMap.get(Constants.CORRELATION_ID).toString();
        } catch (JsonParseException e) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_PROTECTED, "Error while parsing protected headers");
        }
    }


    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> reqBody, String apiAction, String kafkaTopic) throws ClientException {
        Response response = new Response();
        try {
            Map<String, Object> requestBody = formatRequestBody(reqBody);
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException("Service is unavailable");
            String correlationId = getCorrelationId(requestBody);
            response.setCorrelationId(correlationId);
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

    public Map<String, Object> formatRequestBody(Map<String, Object> requestBody) throws ClientException {
        Map<String, Object> event = new HashMap<>();
        try {
            String str = (String) requestBody.get("payload");

            String[] strArray = str.split("\\.");
            System.out.println("test"+strArray.length);
            if (strArray.length > 0 && strArray.length == Constants.PAYLOAD_LENGTH) {
                event.put("protected", strArray[0] );
                event.put("encrypted_key", strArray[1]);
                event.put("aad", strArray[2]);
                event.put("iv", strArray[3]);
                event.put("ciphertext", strArray[4]);
                event.put("tag", strArray[5]);
            }

        }catch (Exception e){
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }
        return event;
    }
}
