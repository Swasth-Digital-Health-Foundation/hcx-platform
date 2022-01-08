package org.swasth.hcx.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.common.dto.Response;
import org.swasth.common.dto.ResponseError;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.common.utils.DateTimeUtils;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.Utils;
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

    @Value("${timestamp.range}")
    private int timestampRange;

    private void validateRequestBody(Map<String, Object> requestBody) throws Exception {
        // validating payload properties
        List<String> mandatoryPayloadProps = env.getProperty(Constants.PAYLOAD_MANDATORY_PROPERTIES, List.class);
        List<String> missingPayloadProps = mandatoryPayloadProps.stream().filter(key -> !requestBody.containsKey(key)).collect(Collectors.toList());
        if (!missingPayloadProps.isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "Payload mandatory properties are missing: " + missingPayloadProps);
        }
        //validating protected headers
        Map<String, Object> protectedHeaders = JSONUtils.decodeBase64String((String) requestBody.get("protected"), HashMap.class);
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
        if (!(protectedHeaders.get(Constants.TIMESTAMP) instanceof String) || StringUtils.isEmpty((String) protectedHeaders.get(Constants.TIMESTAMP)) ) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Timestamp cannot be null or empty");
        } else if (!DateTimeUtils.validTimestamp(timestampRange, (String) protectedHeaders.get(Constants.TIMESTAMP))) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Timestamp cannot be more than " + timestampRange + " hours in the past or future time");
        }

        if (protectedHeaders.containsKey(Constants.CORRELATION_ID)) {
            if (!(protectedHeaders.get(Constants.CORRELATION_ID) instanceof String) || ((String) protectedHeaders.get(Constants.CORRELATION_ID)).isEmpty() || !Utils.isUUID((String) protectedHeaders.get(Constants.CORRELATION_ID))) {
                throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CORREL_ID, "Correlation id should be a valid UUID");
            }
        }
        if (protectedHeaders.containsKey(Constants.CASE_ID)) {
          if (!(protectedHeaders.get(Constants.CASE_ID) instanceof String) || ((String) protectedHeaders.get(Constants.CASE_ID)).isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CASE_ID, "Case id cannot be null, empty and other than 'String'");
          }
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
        String key = JSONUtils.decodeBase64String((String) requestBody.get(Constants.PROTECTED), HashMap.class).get(Constants.SENDER_CODE).toString();
        String payloadEvent = eventGenerator.generatePayloadEvent(mid, requestBody);
        String metadataEvent = eventGenerator.generateMetadataEvent(mid, apiAction, requestBody);
        System.out.println("Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + metadataEvent);
        if(StringUtils.equalsIgnoreCase(serviceMode, Constants.GATEWAY)) {
            kafkaClient.send(payloadTopic, key, payloadEvent);
            kafkaClient.send(metadataTopic, key, metadataEvent);
            postgreSQLClient.insert(mid, JSONUtils.serialize(requestBody));
        }
    }

    private void setResponseParams(Response response, Map<String, Object> requestBody) throws Exception {
        Map protectedMap = JSONUtils.decodeBase64String((String) requestBody.get(Constants.PROTECTED), HashMap.class);
        response.setWorkflowId(protectedMap.get(Constants.WORKFLOW_ID).toString());
        response.setRequestId(protectedMap.get(Constants.REQUEST_ID).toString());
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> reqBody, String apiAction, String kafkaTopic) throws ClientException {
        Response response = new Response();
        try {
            Map<String, Object> requestBody = formatRequestBody(reqBody);
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException(ErrorCodes.SERVICE_UNAVAILABLE, "Service is unavailable");
            setResponseParams(response, requestBody);
            validateRequestBody(requestBody);
            processAndSendEvent(apiAction, kafkaTopic , requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (ServiceUnavailbleException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (ServerException e) {
            return new ResponseEntity<>(errorResponse(response, e.getErrCode(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> formatRequestBody(Map<String, Object> requestBody) throws ClientException {
        Map<String, Object> event = new HashMap<>();
        String str = (String) requestBody.get("payload");

        String[] strArray = str.split("\\.");
        if (strArray.length > 0 && strArray.length == Constants.PAYLOAD_LENGTH) {
            event.put("protected", strArray[0] );
            event.put("encrypted_key", strArray[1]);
            event.put("aad", strArray[2]);
            event.put("iv", strArray[3]);
            event.put("ciphertext", strArray[4]);
            event.put("tag", strArray[5]);
        } else {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_PAYLOAD, "invalid payload");
        }

        return event;
    }
}
