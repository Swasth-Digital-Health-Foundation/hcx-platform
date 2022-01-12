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
import org.swasth.hcx.pojo.RequestBody;
import org.swasth.hcx.pojo.SearchRequestBody;
import org.swasth.hcx.utils.Constants;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;

import java.util.*;

public class BaseSearchController {

    @Autowired
    private EventGenerator eventGenerator;

    @Autowired
    protected Environment env;

    @Autowired
    private IEventService kafkaClient;

    @Autowired
    private IDatabaseService postgreSQLClient;

    private Map<String, ClientException> clientErrors = new HashMap<>();

    public BaseSearchController() {
        clientErrors.put(Constants.SENDER_CODE, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_SENDER, "Sender code cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.RECIPIENT_CODE, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_RECIPIENT, "Recipient code cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.WORKFLOW_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.REQUEST_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_ID, "Request id cannot be null, empty and other than 'String'"));
        clientErrors.put(Constants.TIMESTAMP, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_TIMESTAMP, "Invalid timestamp"));
//        clientErrors.put(Constants.CASE_ID, new ClientException(ErrorCodes.CLIENT_ERR_INVALID_CASE_ID, "Case id cannot be null, empty and other than 'String'"));
    }

    private void validateHeader(Map<String, Object> headers, String key, ClientException ex) throws ClientException {
        if (headers.containsKey(key)) {
            Object value = headers.get(key);
            if (!(value instanceof String) || StringUtils.isEmpty((String) value))
                throw ex;
        } else throw ex;
    }

    private void validateProtocolHeadersFormat(Map<String, Object> protectedHeaders) throws ClientException {
        for (Map.Entry<String, ClientException> entry : clientErrors.entrySet()) {
            validateHeader(protectedHeaders, entry.getKey(), entry.getValue());
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

    private Response errorResponse(ErrorCodes code, java.lang.Exception e) {
        Response response = new Response();
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    private void processAndSendEvent(String apiAction, String metadataTopic, RequestBody requestBody) throws Exception {
        String mid = UUID.randomUUID().toString();
        String serviceMode = env.getProperty(Constants.SERVICE_MODE);
        String payloadTopic = env.getProperty(Constants.KAFKA_TOPIC_PAYLOAD);

        String payloadEvent = eventGenerator.generatePayloadEvent(mid, requestBody.getPayload());
        String metadataEvent = eventGenerator.generateMetadataEvent(mid, apiAction, requestBody.getPayload());
        System.out.println("Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + metadataEvent);
        if(StringUtils.equalsIgnoreCase(serviceMode, Constants.GATEWAY)) {
            kafkaClient.send(payloadTopic, requestBody.getSenderCode(), payloadEvent);
            kafkaClient.send(metadataTopic, requestBody.getSenderCode(), metadataEvent);
            postgreSQLClient.insert(mid, JsonUtils.serialize(requestBody.getPayload()));
        }
    }

    // TODO rename it to get Response.
    private Response setResponseParams(RequestBody requestBody) throws Exception {
        String workflowId = requestBody.getWorkflowId();
        // TODO move this logic to RequestBody.validate method.
        if (!requestBody.getHCXHeaders().containsKey(Constants.WORKFLOW_ID) || !(requestBody.getHCXHeaders().get(Constants.WORKFLOW_ID) instanceof String) || ((String) requestBody.getHCXHeaders().get(Constants.WORKFLOW_ID)).isEmpty())
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_WORKFLOW_ID, "Workflow id cannot be null, empty and other than 'String'");
        // TODO move this logic to RequestBody.validate method.
        if (!requestBody.getHCXHeaders().containsKey(Constants.REQUEST_ID) || !(requestBody.getHCXHeaders().get(Constants.REQUEST_ID) instanceof String) || ((String) requestBody.getHCXHeaders().get(Constants.REQUEST_ID)).isEmpty()) {
            throw new ClientException(ErrorCodes.CLIENT_ERR_INVALID_REQ_ID, "Request id cannot be null, empty and other than 'String'");
        }

        return new Response(requestBody.getWorkflowId(), requestBody.getRequestId());
    }


    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> body, String apiAction, String kafkaTopic) throws ClientException {
        try {
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException("Service is unavailable");
            RequestBody requestBody = new SearchRequestBody(body);
            requestBody.validate(getMandatoryHeaders());
            // TODO move below method logic to "validate" method in pojo.
            validateProtocolHeadersFormat(requestBody.getHCXHeaders());
            // TODO construct to response object here only and remove the below method.
            Response response = setResponseParams(requestBody);
            processAndSendEvent(apiAction, kafkaTopic , requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (ClientException e) {
            return new ResponseEntity<>(errorResponse(e.getErrCode(), e), HttpStatus.BAD_REQUEST);
        } catch (ServiceUnavailbleException e) {
            return new ResponseEntity<>(errorResponse(ErrorCodes.SERVICE_UNAVAILABLE , e), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (ServerException e) {
            return new ResponseEntity<>(errorResponse(e.getErrCode(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(errorResponse(ErrorCodes.SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> getMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty(Constants.PROTOCOL_HEADERS_MANDATORY, List.class));
        mandatoryHeaders.addAll(env.getProperty(Constants.JOSE_HEADERS, List.class));
        return mandatoryHeaders;
    }
}
