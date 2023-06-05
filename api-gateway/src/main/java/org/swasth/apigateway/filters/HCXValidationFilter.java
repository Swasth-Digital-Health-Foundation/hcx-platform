package org.swasth.apigateway.filters;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;
import org.swasth.apigateway.handlers.ExceptionHandler;
import org.swasth.apigateway.handlers.RequestHandler;
import org.swasth.apigateway.models.BaseRequest;
import org.swasth.apigateway.models.JSONRequest;
import org.swasth.apigateway.models.JWERequest;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.common.utils.NotificationUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;
import static org.swasth.common.response.ResponseMessage.*;
import static org.swasth.common.utils.Constants.*;

@Component
public class HCXValidationFilter extends AbstractGatewayFilterFactory<HCXValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(HCXValidationFilter.class);

    @Autowired
    RegistryService registryService;

    @Autowired
    AuditService auditService;

    @Autowired
    ExceptionHandler exceptionHandler;

    @Autowired
    RequestHandler requestHandler;

    @Autowired
    private Environment env;

    @Autowired
    private JWTUtils jwtUtils;

    @Value("${timestamp.range}")
    private int timestampRange;

    @Value("${allowedEntitiesForForward}")
    private List<String> allowedEntitiesForForward;

    @Value("${allowedRolesForForward}")
    private List<String> allowedRolesForForward;

    @Value("${registry.hcxCode}")
    private String hcxCode;

    @Value("${registry.hcxRoles}")
    private String hcxRoles;

    @Value("${notify.network.allowedCodes}")
    private List<String> allowedNetworkCodes;

    @Value("${allowedParticipantStatus}")
    private List<String> allowedParticipantStatus;

    public HCXValidationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String correlationId = null;
            String apiCallId = null;
            BaseRequest requestObj = null;
            String path;
            Map<String, Object> requestBody;
            Mono<Void> modifiedReq;
            try {
                StringBuilder cachedBody = new StringBuilder(StandardCharsets.UTF_8.decode(((DataBuffer) exchange.getAttribute(CACHED_REQUEST_BODY_ATTR)).asByteBuffer()));
                ServerHttpRequest request = exchange.getRequest();
                path = request.getPath().value();
                String subject = request.getHeaders().getFirst("X-jwt-sub");
                requestBody = JSONUtils.deserialize(cachedBody.toString(), HashMap.class);
                if (path.contains(NOTIFICATION_NOTIFY)) { //for validating notify api request
                    JSONRequest jsonRequest = new JSONRequest(requestBody, false, path, hcxCode, hcxRoles);
                    jsonRequest.setHeaders(jsonRequest.getNotificationHeaders());
                    requestObj = jsonRequest;
                    Map<String, Object> senderDetails = registryService.fetchDetails(PARTICIPANT_CODE, jsonRequest.getSenderCode());
                    List<Map<String, Object>> recipientsDetails = new ArrayList<>();
                    if (jsonRequest.getRecipientType().equalsIgnoreCase(PARTICIPANT_CODE) && !jsonRequest.getRecipients().isEmpty()) {
                        String searchRequest = createSearchRequest(jsonRequest.getRecipients(), PARTICIPANT_CODE, false);
                        recipientsDetails = registryService.getDetails(searchRequest);
                    }
                    jsonRequest.validateNotificationReq(senderDetails, recipientsDetails, allowedNetworkCodes, allowedParticipantStatus);
                    jsonRequest.validateCondition(!jwtUtils.isValidSignature(jsonRequest.getNotificationPayload(), (String) senderDetails.get(SIGNING_CERT_PATH)), ErrorCodes.ERR_INVALID_SIGNATURE, INVALID_JWS);
                } else if (requestBody.containsKey(PAYLOAD)) {
                    JWERequest jweRequest = new JWERequest(requestBody, false, path, hcxCode, hcxRoles);
                    requestObj = jweRequest;
                    correlationId = jweRequest.getCorrelationId();
                    apiCallId = jweRequest.getApiCallId();
                    Map<String, Object> senderDetails = getDetails(jweRequest.getHcxSenderCode());
                    Map<String, Object> recipientDetails = getDetails(jweRequest.getHcxRecipientCode());
                    requestObj.getPayload().put(SENDERDETAILS, senderDetails);
                    requestObj.getPayload().put(RECIPIENTDETAILS, recipientDetails);
                    List<Map<String, Object>> participantCtxAuditDetails = getParticipantCtxAuditData(jweRequest.getHcxSenderCode(), jweRequest.getHcxRecipientCode(), jweRequest.getCorrelationId());
                    jweRequest.validate(getMandatoryHeaders(), subject, timestampRange, senderDetails, recipientDetails, allowedParticipantStatus);
                    jweRequest.validateUsingAuditData(allowedEntitiesForForward, allowedRolesForForward, senderDetails, recipientDetails, getCorrelationAuditData(jweRequest.getCorrelationId()), getCallAuditData(jweRequest.getApiCallId()), participantCtxAuditDetails, path);
                    validateParticipantCtxDetails(participantCtxAuditDetails, path);
                } else if (path.contains(NOTIFICATION_SUBSCRIBE) || path.contains(NOTIFICATION_UNSUBSCRIBE)) { //for validating /notification/subscribe, /notification/unsubscribe
                    JSONRequest jsonRequest = new JSONRequest(requestBody, true, path, hcxCode, hcxRoles);
                    requestObj = jsonRequest;
                    // Do not allow * in the request body for unsubscribe
                    if (jsonRequest.getSenderList().contains("*") && path.contains(NOTIFICATION_UNSUBSCRIBE)) {
                        throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, UNSUBSCRIBE_ERR_MSG);
                    }
                    Map<String, Object> recipientDetails = registryService.fetchDetails(OS_OWNER, subject);
                    // Do not allow requester to send his participant code in the sender_list
                    if (jsonRequest.getSenderList().contains(recipientDetails.get(PARTICIPANT_CODE))) {
                        throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, NOTIFICATION_SUBSCRIBE_ERR_MSG);
                    }
                    List<Map<String, Object>> senderListDetails = new ArrayList<>();
                    //Check for * in the request body in sendersList and send subscription for all valid participants
                    Map<String, Object> notification = NotificationUtils.getNotification(jsonRequest.getTopicCode());
                    if (jsonRequest.getSenderList().contains("*")) {
                        String searchRequest = createSearchRequest((List<String>) notification.get(Constants.ALLOWED_SENDERS), ROLES, true);
                        senderListDetails = registryService.getDetails(searchRequest);
                        List<String> fetchedCodes = senderListDetails.stream().map(obj -> obj.get(Constants.PARTICIPANT_CODE).toString()).collect(Collectors.toList());
                        requestBody.put(SENDER_LIST, fetchedCodes);
                    } else if (!jsonRequest.getSenderList().isEmpty()) {
                        String searchRequest = createSearchRequest(jsonRequest.getSenderList(), PARTICIPANT_CODE, false);
                        senderListDetails = registryService.getDetails(searchRequest);
                    } else {
                        throw new ClientException(ErrorCodes.ERR_INVALID_NOTIFICATION_REQ, EMPTY_SENDER_LIST_ERR_MSG);
                    }
                    jsonRequest.validateSubscriptionRequests(jsonRequest.getTopicCode(), senderListDetails, recipientDetails, getSubscriptionMandatoryHeaders(), notification, allowedParticipantStatus);
                    requestObj.getPayload().put(RECIPIENT_CODE, recipientDetails.get(PARTICIPANT_CODE));
                } else if (path.contains(NOTIFICATION_SUBSCRIPTION_LIST)) { //for validating /notification/subscription/list
                    JSONRequest jsonRequest = new JSONRequest(requestBody, true, path, hcxCode, hcxRoles);
                    requestObj = jsonRequest;
                    Map<String, Object> recipientDetails = registryService.fetchDetails(OS_OWNER, subject);
                    requestObj.getPayload().put(RECIPIENT_CODE, recipientDetails.get(PARTICIPANT_CODE));
                } else if (path.contains(NOTIFICATION_SUBSCRIPTION_UPDATE) || path.contains(NOTIFICATION_ON_SUBSCRIBE)) {
                    JSONRequest jsonRequest = new JSONRequest(requestBody, true, path, hcxCode, hcxRoles);
                    requestObj = jsonRequest;
                    Map<String, Object> senderDetails = registryService.fetchDetails(OS_OWNER, subject);
                    jsonRequest.validateNotificationParticipant(senderDetails, ErrorCodes.ERR_INVALID_SENDER, SENDER, allowedParticipantStatus);
                    requestObj.getPayload().put(SENDER_CODE, senderDetails.get(PARTICIPANT_CODE));
                } else { //for validating redirect and error plain JSON on_check calls
                    if (!path.contains("on_")) {
                        throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, INVALID_JWE_MSG);
                    }
                    JSONRequest jsonRequest = new JSONRequest(requestBody, true, path, hcxCode, hcxRoles);
                    requestObj = jsonRequest;
                    correlationId = jsonRequest.getCorrelationId();
                    apiCallId = jsonRequest.getApiCallId();
                    Map<String, Object> senderDetails = getDetails(jsonRequest.getHcxSenderCode());
                    Map<String, Object> recipientDetails = getDetails(jsonRequest.getHcxRecipientCode());
                    requestObj.getPayload().put(SENDERDETAILS,senderDetails);
                    requestObj.getPayload().put(RECIPIENTDETAILS,recipientDetails);
                    if (ERROR_RESPONSE.equalsIgnoreCase(jsonRequest.getStatus())) {
                        jsonRequest.validate(getErrorMandatoryHeaders(), subject, timestampRange, senderDetails, recipientDetails, allowedParticipantStatus);
                    } else {
                        jsonRequest.validate(getRedirectMandatoryHeaders(), subject, timestampRange, senderDetails, recipientDetails, allowedParticipantStatus);
                        if (getApisForRedirect().contains(path)) {
                            if (REDIRECT_STATUS.equalsIgnoreCase(jsonRequest.getStatus()))
                                jsonRequest.validateRedirect(getRolesForRedirect(), getDetails(jsonRequest.getRedirectTo()), getCallAuditData(jsonRequest.getApiCallId()), getCorrelationAuditData(jsonRequest.getCorrelationId()), allowedParticipantStatus);
                            else
                                throw new ClientException(ErrorCodes.ERR_INVALID_REDIRECT_TO, MessageFormat.format(INVALID_STATUS_REDIRECT, jsonRequest.getStatus(), REDIRECT_STATUS));
                        } else
                            throw new ClientException(ErrorCodes.ERR_INVALID_REDIRECT_TO, MessageFormat.format(INVALID_ACTION_REDIRECT, jsonRequest.getApiAction(), getApisForRedirect()));
                    }
                    validateParticipantCtxDetails(getParticipantCtxAuditData(jsonRequest.getHcxSenderCode(), jsonRequest.getHcxRecipientCode(), jsonRequest.getCorrelationId()), path);
                }
               modifiedReq = requestHandler.getUpdatedBody(exchange, chain, requestObj.getPayload());
            } catch (Exception e) {
                logger.error(MessageFormat.format(CORRELATION_ERR_MSG, correlationId,  e.getMessage()));
                return exceptionHandler.errorResponse(e, exchange, correlationId, apiCallId, requestObj);
            }
            return modifiedReq;
        };
    }

    private String createSearchRequest(List<String> jsonRequest, String filterKey, boolean isActive) {
        String wrappedList = jsonRequest.stream()
                .map(plain -> StringUtils.wrap(plain, "\""))
                .collect(Collectors.joining(", "));
        String searchRequest = null;
        if (isActive)
            searchRequest = "{\"filters\":{\"" + filterKey + "\":{\"or\":[" + wrappedList + "]},\"status\": {\"or\": [\"Created\", \"Active\"]}}}";
        else searchRequest = "{\"filters\":{\"" + filterKey + "\":{\"or\":[" + wrappedList + "]}}}";
        return searchRequest;
    }

    private List<Map<String, Object>> getCallAuditData(String apiCallId) throws Exception {
        return getAuditData(Collections.singletonMap(API_CALL_ID, apiCallId));
    }

    private List<Map<String, Object>> getCorrelationAuditData(String correlationId) throws Exception {
        return getAuditData(Collections.singletonMap(CORRELATION_ID, correlationId));
    }

    private List<Map<String, Object>> getParticipantCtxAuditData(String senderCode, String recipientCode, String correlationId) throws Exception {
        Map<String, String> filters = new HashMap<>();
        filters.put(HCX_SENDER_CODE, recipientCode);
        filters.put(HCX_RECIPIENT_CODE, senderCode);
        filters.put(CORRELATION_ID, correlationId);
        return getAuditData(filters);
    }

    private List<String> getMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty("protocol.headers.mandatory", List.class));
        mandatoryHeaders.addAll(env.getProperty("headers.jose", List.class));
        return mandatoryHeaders;
    }

    private void validateParticipantCtxDetails(List<Map<String, Object>> auditData, String path) throws Exception {
        if (!auditData.isEmpty() && path.contains("on_")) {
            Map<String, Object> result = auditData.get(0);
            if (result.get(STATUS).equals(QUEUED_STATUS)) {
                result.put(STATUS, DISPATCHED_STATUS);
                result.put(UPDATED_TIME, System.currentTimeMillis());
                result.put(ETS, System.currentTimeMillis());
                auditService.updateAuditLog(result);
            }
        }
    }

    private List<String> getErrorMandatoryHeaders() {
        List<String> mandatoryHeaders = new ArrayList<>();
        mandatoryHeaders.addAll(env.getProperty("plainrequest.headers.mandatory", List.class));
        return mandatoryHeaders;
    }

    private Map<String, Object> getDetails(String code) throws Exception {
        return registryService.fetchDetails(Constants.PARTICIPANT_CODE, code);
    }

    private List<Map<String, Object>> getAuditData(Map<String, String> filters) throws Exception {
        return auditService.getAuditLogs(filters);
    }

    public static class Config {
    }

    private List<String> getRedirectMandatoryHeaders() {
        List<String> plainMandatoryHeaders = new ArrayList<>();
        plainMandatoryHeaders.addAll(env.getProperty("redirect.headers.mandatory", List.class));
        return plainMandatoryHeaders;
    }

    private List<String> getApisForRedirect() {
        List<String> allowedApis = new ArrayList<>();
        allowedApis.addAll(env.getProperty("redirect.apis", List.class));
        return allowedApis;
    }

    private List<String> getRolesForRedirect() {
        List<String> allowedRoles = new ArrayList<>();
        allowedRoles.addAll(env.getProperty("redirect.roles", List.class));
        return allowedRoles;
    }

    private List<String> getSubscriptionMandatoryHeaders() {
        List<String> subscriptionMandatoryHeaders = new ArrayList<>();
        subscriptionMandatoryHeaders.addAll(env.getProperty("notification.subscription.headers.mandatory", List.class));
        return subscriptionMandatoryHeaders;
    }

}
