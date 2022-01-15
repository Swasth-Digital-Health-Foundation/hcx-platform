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

    public BaseSearchController() {

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

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> body, String apiAction, String kafkaTopic) {
        try {
            if (!HealthCheckManager.allSystemHealthResult)
                throw new ServiceUnavailbleException("Service is unavailable");
            RequestBody requestBody = new SearchRequestBody(body);
            requestBody.validate(getMandatoryHeaders(),getPayloadProperties());
            processAndSendEvent(apiAction, kafkaTopic , requestBody);
            return new ResponseEntity<>(new Response(requestBody.getWorkflowId(), requestBody.getRequestId()), HttpStatus.ACCEPTED);
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

    private List<String> getPayloadProperties(){
        return env.getProperty(Constants.PAYLOAD_MANDATORY_PROPERTIES, List.class);
    }
}
