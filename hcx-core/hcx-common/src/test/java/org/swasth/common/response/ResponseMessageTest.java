package org.swasth.common.response;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResponseMessageTest {

    @Test
    public void testInvalidTopic(){
        String invalidTopicCode = ResponseMessage.INVALID_TOPIC_CODE;
        assertTrue(invalidTopicCode.contains("invalid"));
    }

    @Test
    public void testNothing(){
        String invalidTopicCode = ResponseMessage.NOTHING_TO_UPDATE;
        assertTrue(invalidTopicCode.contains("Nothing"));
    }

    @Test
    public void testInvalidNotificationFilters(){
        String invalidTopicCode = ResponseMessage.INVALID_NOTIFICATION_FILTERS;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testExpiry(){
        String invalidTopicCode = ResponseMessage.EXPIRY_CANNOT_BE_PAST_DATE;
        assertTrue(invalidTopicCode.contains("date"));
    }

    @Test
    public void testInvalidSubscriptionStatus(){
        String invalidTopicCode = ResponseMessage.SUBSCRIPTION_STATUS_VALUE_IS_INVALID;
        assertTrue(invalidTopicCode.contains("invalid"));
    }

    @Test
    public void testInvalidDelegated(){
        String invalidTopicCode = ResponseMessage.IS_DELEGATED_VALUE_IS_INVALID;
        assertTrue(invalidTopicCode.contains("invalid"));
    }

    @Test
    public void testSubscriptionDoesNotExist(){
        String invalidTopicCode = ResponseMessage.SUBSCRIPTION_DOES_NOT_EXIST;
        assertTrue(invalidTopicCode.contains("Subscription"));
    }

    @Test
    public void testInvalidSubscriptionFilters(){
        String invalidTopicCode = ResponseMessage.INVALID_SUBSCRIPTION_FILTERS;
        assertTrue(invalidTopicCode.contains("subscription"));
    }

    @Test
    public void testUpdateMessageSubscriptionId(){
        String invalidTopicCode = ResponseMessage.UPDATE_MESSAGE_SUBSCRIPTION_ID;
        assertTrue(invalidTopicCode.contains("subscription"));
    }

    @Test
    public void testInvalidSubscriptionId(){
        String invalidTopicCode = ResponseMessage.INVALID_SUBSCRIPTION_ID;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testInvalidSubscriptionList(){
        String invalidTopicCode = ResponseMessage.INVALID_SUBSCRIPTION_LIST;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testInvalidStatusSearch(){
        String invalidTopicCode = ResponseMessage.INVALID_STATUS_SEARCH_ENTITY;
        assertTrue(invalidTopicCode.contains("entity"));
    }
    @Test
    public void testCorrelationIdMissing(){
        String invalidTopicCode = ResponseMessage.CORRELATION_ID_MISSING;
        assertTrue(invalidTopicCode.contains("correlation"));
    }

    @Test
    public void testServiceUnavailable(){
        String invalidTopicCode = ResponseMessage.SERVICE_UNAVAILABLE;
        assertTrue(invalidTopicCode.contains("service"));
    }
    @Test
    public void testInvalidCorrelationIdError(){
        String invalidTopicCode = ResponseMessage.INVALID_CORRELATION_ID;
        assertTrue(invalidTopicCode.contains("Correlation"));
    }

    @Test
    public void testInvalidWorkflowIdError(){
        String invalidTopicCode = ResponseMessage.INVALID_WORKFLOW_ID;
        assertTrue(invalidTopicCode.contains("invalid"));
    }
    @Test
    public void testInvalidParticipantCode(){
        String invalidTopicCode = ResponseMessage.INVALID_PARTICIPANT_CODE;
        assertTrue(invalidTopicCode.contains("participant"));
    }

    @Test
    public void testParticipantCode(){
        String invalidTopicCode = ResponseMessage.PARTICIPANT_CODE_MSG;
        assertTrue(invalidTopicCode.contains("participant"));
    }
    @Test
    public void testParticipantError(){
        String invalidTopicCode = ResponseMessage.PARTICIPANT_ERROR_MSG;
        assertTrue(invalidTopicCode.contains("participant"));
    }

    @Test
    public void testInvalidRegistry(){
        String invalidTopicCode = ResponseMessage.INVALID_REGISTRY_RESPONSE;
        assertTrue(invalidTopicCode.contains("invalid"));
    }
    @Test
    public void testInvalidRolesProperty(){
        String invalidTopicCode = ResponseMessage.INVALID_ROLES_PROPERTY;
        assertTrue(invalidTopicCode.contains("Roles"));
    }

    @Test
    public void testMissingSchemeCode(){
        String invalidTopicCode = ResponseMessage.MISSING_SCHEME_CODE;
        assertTrue(invalidTopicCode.contains("missing"));
    }
    @Test
    public void testUnknownProperty(){
        String invalidTopicCode = ResponseMessage.UNKNOWN_PROPERTY;
        assertTrue(invalidTopicCode.contains("Unknown"));
    }

    @Test
    public void testInvalidEndPoint(){
        String invalidTopicCode = ResponseMessage.INVALID_END_POINT;
        assertTrue(invalidTopicCode.contains("point"));
    }
    @Test
    public void testInvalidEmail(){
        String invalidTopicCode = ResponseMessage.INVALID_EMAIL;
        assertTrue(invalidTopicCode.contains("invalid"));
    }

    @Test
    public void testInvalidEncryptionCert(){
        String invalidTopicCode = ResponseMessage.INVALID_ENCRYPTION_CERT;
        assertTrue(invalidTopicCode.contains("invalid"));
    }
    @Test
    public void testValidSearchObj(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_OBJ_MSG;
        assertTrue(invalidTopicCode.contains("details"));
    }

    @Test
    public void testValidSearchMessage(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_MSG;
        assertTrue(invalidTopicCode.contains("Search"));
    }
    @Test
    public void testValidSearchResponseObj(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_RESP_OBJ_MSG;
        assertTrue(invalidTopicCode.contains("details"));
    }

    @Test
    public void testValidSearchResponse(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_RESP_MSG;
        assertTrue(invalidTopicCode.contains("details"));
    }
    @Test
    public void testValidSearchRegistryCode(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_REGISTRY_CODE;
        assertTrue(invalidTopicCode.contains("code"));
    }

    @Test
    public void testValidSearchFilters(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_FILTERS_MSG;
        assertTrue(invalidTopicCode.contains("Search"));
    }
    @Test
    public void testValidSearchFiltersObj(){
        String invalidTopicCode = ResponseMessage.VALID_SEARCH_FILTERS_OBJ_MSG;
        assertTrue(invalidTopicCode.contains("filters"));
    }

    @Test
    public void testInvalidSender(){
        String invalidTopicCode = ResponseMessage.INVALID_SENDER;
        assertTrue(invalidTopicCode.contains("Sender"));
    }
    @Test
    public void testAccessDenied(){
        String invalidTopicCode = ResponseMessage.ACCESS_DENIED_MSG;
        assertTrue(invalidTopicCode.contains("access"));
    }

    @Test
    public void testAuthHeaderMissing(){
        String invalidTopicCode = ResponseMessage.AUTH_HEADER_MISSING;
        assertTrue(invalidTopicCode.contains("header"));
    }
    @Test
    public void testAuthHeaderEmpty(){
        String invalidTopicCode = ResponseMessage.AUTH_HEADER_EMPTY;
        assertTrue(invalidTopicCode.contains("header"));
    }

    @Test
    public void testAuthMalformed(){
        String invalidTopicCode = ResponseMessage.AUTH_MALFORMED;
        assertTrue(invalidTopicCode.contains("Malformed"));
    }

    @Test
    public void testInvalidBearer(){
        String invalidTopicCode = ResponseMessage.BEARER_MISSING;
        assertTrue(invalidTopicCode.contains("Bearer"));
    }

    @Test
    public void testAuditLog(){
        String invalidTopicCode = ResponseMessage.AUDIT_LOG_MSG;
        assertTrue(invalidTopicCode.contains("log"));
    }
    @Test
    public void testInvalidKeyHSMessage(){
        String invalidTopicCode = ResponseMessage.INVALID_KEY_HS_MSG;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testInvalidKeyRSMessage(){
        String invalidTopicCode = ResponseMessage.INVALID_KEY_RS_MSG;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }
    @Test
    public void testInvalidJwt(){
        String invalidTopicCode = ResponseMessage.INVALID_JWT_MSG;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testAuditServiceError(){
        String invalidTopicCode = ResponseMessage.AUDIT_SERVICE_ERROR;
        assertTrue(invalidTopicCode.contains("service"));
    }
    @Test
    public void testAuditLogFetch(){
        String invalidTopicCode = ResponseMessage.AUDIT_LOG_FETCH_MSG;
        assertTrue(invalidTopicCode.contains("log"));
    }

    @Test
    public void testRegstryServiceError(){
        String invalidTopicCode = ResponseMessage.REGISTRY_SERVICE_ERROR;
        assertTrue(invalidTopicCode.contains("service"));
    }
    @Test
    public void testRegistryServiceFetchMessage(){
        String invalidTopicCode = ResponseMessage.REGISTRY_SERVICE_FETCH_MSG;
        assertTrue(invalidTopicCode.contains("details"));
    }

    @Test
    public void testInvalidTimestampMessage(){
        String invalidTopicCode = ResponseMessage.INVALID_TIMESTAMP_MSG;
        assertTrue(invalidTopicCode.contains("Timestamp"));
    }
    @Test
    public void testInvalidJweMessage(){
        String invalidTopicCode = ResponseMessage.INVALID_JWE_MSG;
        assertTrue(invalidTopicCode.contains("JWE"));
    }

    @Test
    public void testInvalidStatusRedirect(){
        String invalidTopicCode = ResponseMessage.INVALID_STATUS_REDIRECT;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }
    @Test
    public void testInvalidActionRedirect(){
        String invalidTopicCode = ResponseMessage.INVALID_ACTION_REDIRECT;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testCorrelationErrorMessage(){
        String invalidTopicCode = ResponseMessage.CORRELATION_ERR_MSG;
        assertTrue(invalidTopicCode.contains("correlation"));
    }
    @Test
    public void testPayloadParseError(){
        String invalidTopicCode = ResponseMessage.PAYLOAD_PARSE_ERR;
        assertTrue(invalidTopicCode.contains("payload"));
    }

    @Test
    public void testInvalidApiCallUuid(){
        String invalidTopicCode = ResponseMessage.INVALID_API_CALL_UUID;
        assertTrue(invalidTopicCode.contains("valid"));
    }
    @Test
    public void testInvalidCorrelationUuid(){
        String invalidTopicCode = ResponseMessage.INVALID_CORRELATION_UUID;
        assertTrue(invalidTopicCode.contains("valid"));
    }

    @Test
    public void testMissingMandatoryHeaders(){
        String invalidTopicCode = ResponseMessage.MISSING_MANDATORY_HEADERS;
        assertTrue(invalidTopicCode.contains("missing"));
    }
    @Test
    public void testTimestampFutureMessage(){
        String invalidTopicCode = ResponseMessage.TIMESTAMP_FUTURE_MSG;
        assertTrue(invalidTopicCode.contains("Timestamp"));
    }

    @Test
    public void testInvalidWorkflowId(){
        String invalidTopicCode = ResponseMessage.INVALID_WORKFLOW_UUID;
        assertTrue(invalidTopicCode.contains("valid"));
    }
    @Test
    public void testSenderRecipientSame(){
        String invalidTopicCode = ResponseMessage.SENDER_RECIPIENT_SAME_MSG;
        assertTrue(invalidTopicCode.contains("Sender"));
    }

    @Test
    public void testCallerMismatchMessage(){
        String invalidTopicCode = ResponseMessage.CALLER_MISMATCH_MSG;
        assertTrue(invalidTopicCode.contains("matched"));
    }
    @Test
    public void testDebugFlagError(){
        String invalidTopicCode = ResponseMessage.DEBUG_FLAG_ERR;
        assertTrue(invalidTopicCode.contains("Debug"));
    }

    @Test
    public void testDebugFlagValues(){
        String invalidTopicCode = ResponseMessage.DEBUG_FLAG_VALUES_ERR;
        assertTrue(invalidTopicCode.contains("Debug"));
    }
    @Test
    public void testMandatoryStatusMissing(){
        String invalidTopicCode = ResponseMessage.MANDATORY_STATUS_MISSING;
        assertTrue(invalidTopicCode.contains("headers"));
    }

    @Test
    public void testStatusError(){
        String invalidTopicCode = ResponseMessage.STATUS_ERR;
        assertTrue(invalidTopicCode.contains("Status"));
    }
    @Test
    public void testStatusValues(){
        String invalidTopicCode = ResponseMessage.STATUS_VALUES;
        assertTrue(invalidTopicCode.contains("Status"));
    }

    @Test
    public void testStatusOnActionValues(){
        String invalidTopicCode = ResponseMessage.STATUS_ON_ACTION_VALUES;
        assertTrue(invalidTopicCode.contains("action"));
    }
    @Test
    public void testErrorDetailsMessage(){
        String invalidTopicCode = ResponseMessage.ERROR_DETAILS_MSG;
        assertTrue(invalidTopicCode.contains("Error"));
    }

    @Test
    public void testErrorValues(){
        String invalidTopicCode = ResponseMessage.ERROR_VALUES;
        assertTrue(invalidTopicCode.contains("Error"));
    }
    @Test
    public void testInvalidErrorCode(){
        String invalidTopicCode = ResponseMessage.INVALID_ERROR_CODE;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testDebugDetails(){
        String invalidTopicCode = ResponseMessage.DEBUG_DETAILS_ERR;
        assertTrue(invalidTopicCode.contains("Debug"));
    }

    @Test
    public void testDebugDetailsValues(){
        String invalidTopicCode = ResponseMessage.DEBUG_DETAILS_VALUES_ERR;
        assertTrue(invalidTopicCode.contains("details"));
    }

    @Test
    public void testMissingParticipant(){
        String invalidTopicCode = ResponseMessage.MISSING_PARTICIPANT;
        assertTrue(invalidTopicCode.contains("exist"));
    }
    @Test
    public void testInvalidRegistryStatus(){
        String invalidTopicCode = ResponseMessage.INVALID_REGISTRY_STATUS;
        assertTrue(invalidTopicCode.contains("registry"));
    }

    @Test
    public void testHcxCode(){
        String invalidTopicCode = ResponseMessage.HCX_CODE_ERR;
        assertTrue(invalidTopicCode.contains("not"));
    }
    @Test
    public void testHcxRoles(){
        String invalidTopicCode = ResponseMessage.HCX_ROLES_ERR;
        assertTrue(invalidTopicCode.contains("role"));
    }

    @Test
    public void testMandatoryCodeMessage(){
        String invalidTopicCode = ResponseMessage.MANDATORY_CODE_MSG;
        assertTrue(invalidTopicCode.contains("Mandatory"));
    }
    @Test
    public void testSenderCodeErrorMessage(){
        String invalidTopicCode = ResponseMessage.SENDER_CODE_ERR_MSG;
        assertTrue(invalidTopicCode.contains("Sender"));
    }

    @Test
    public void testRecipientCodeErrorMessage(){
        String invalidTopicCode = ResponseMessage.RECIPIENT_CODE_ERR_MSG;
        assertTrue(invalidTopicCode.contains("code"));
    }
    @Test
    public void testTimestampInvalid(){
        String invalidTopicCode = ResponseMessage.TIMESTAMP_INVALID_MSG;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testInvalidWorkflowIdErrorMessage(){
        String invalidTopicCode = ResponseMessage.INVALID_WORKFLOW_ID_ERR_MSG;
        assertTrue(invalidTopicCode.contains("Workflow"));
    } @Test
    public void testInvalidCorrelationId(){
        String invalidTopicCode = ResponseMessage.INVALID_CORRELATION_ID_ERR_MSG;
        assertTrue(invalidTopicCode.contains("Correlation"));
    }

    @Test
    public void testInvalidApiCallId(){
        String invalidTopicCode = ResponseMessage.INVALID_API_CALL_ID_ERR_MSG;
        assertTrue(invalidTopicCode.contains("call"));
    }
    @Test
    public void testApiCallSameMessage(){
        String invalidTopicCode = ResponseMessage.API_CALL_SAME_MSG;
        assertTrue(invalidTopicCode.contains("call"));
    }

    @Test
    public void testClosedCycleMessage(){
        String invalidTopicCode = ResponseMessage.CLOSED_CYCLE_MSG;
        assertTrue(invalidTopicCode.contains("cycle"));
    }
    @Test
    public void testInvalidOnAction(){
        String invalidTopicCode = ResponseMessage.INVALID_ON_ACTION;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testInvalidForward(){
        String invalidTopicCode = ResponseMessage.INVALID_FORWARD;
        assertTrue(invalidTopicCode.contains("not"));
    }
    @Test
    public void testForwardReqErrorMessage(){
        String invalidTopicCode = ResponseMessage.FORWARD_REQ_ERR_MSG;
        assertTrue(invalidTopicCode.contains("forward"));
    }

    @Test
    public void testInvalidRecipient(){
        String invalidTopicCode = ResponseMessage.INVALID_RECIPIENT;
        assertTrue(invalidTopicCode.contains("Invalid"));
    }

    @Test
    public void testMalformedPayload(){
        String invalidTopicCode = ResponseMessage.MALFORMED_PAYLOAD;
        assertTrue(invalidTopicCode.contains("payload"));
    }

    @Test
    public void testInvalidPayload(){
        String invalidTopicCode = ResponseMessage.INVALID_PAYLOAD;
        assertTrue(invalidTopicCode.contains("Payload"));
    }

    @Test
    public void testInvalidForwardCorrelationId(){
        String invalidTopicCode = ResponseMessage.INVALID_FWD_CORRELATION_ID;
        assertTrue(invalidTopicCode.contains("invalid"));
    }

    @Test
    public void testInvalidRedirectMessage(){
        String invalidTopicCode = ResponseMessage.INVALID_REDIRECT_MSG;
        assertTrue(invalidTopicCode.contains("Redirect"));
    }

    @Test
    public void testInvalidRedirectSelf(){
        String invalidTopicCode = ResponseMessage.INVALID_REDIRECT_SELF;
        assertTrue(invalidTopicCode.contains("Sender"));
    }

    @Test
    public void testInvalidRedirectParticipant(){
        String invalidTopicCode = ResponseMessage.INVALID_REDIRECT_PARTICIPANT;
        assertTrue(invalidTopicCode.contains("access"));
    }

    @Test
    public void testInvalidApiCall(){
        String invalidTopicCode = ResponseMessage.INVALID_API_CALL;
        assertTrue(invalidTopicCode.contains("call"));
    }

    @Test
    public void testOnActionCorrelationErrorMessage(){
        String invalidTopicCode = ResponseMessage.ON_ACTION_CORRELATION_ERR_MSG;
        assertTrue(invalidTopicCode.contains("action"));
    }

    @Test
    public void testOnActionWorkflowId(){
        String invalidTopicCode = ResponseMessage.ON_ACTION_WORKFLOW_ID;
        assertTrue(invalidTopicCode.contains("workflow"));
    }

    @Test
    public void testClosedRedirectMessage(){
        String invalidTopicCode = ResponseMessage.CLOSED_REDIRECT_MSG;
        assertTrue(invalidTopicCode.contains("redirect"));
    }

    @Test
    public void testRedirectInitiatorMessage(){
        String invalidTopicCode = ResponseMessage.REDIRECT_INITIATOR_MSG;
        assertTrue(invalidTopicCode.contains("redirect"));
    }

    @Test
    public void testInvalidAlgo(){
        String invalidTopicCode = ResponseMessage.INVALID_ALGO;
        assertTrue(invalidTopicCode.contains("invalid"));
    }

    @Test
    public void testInvalidJws(){
        String invalidTopicCode = ResponseMessage.INVALID_JWS;
        assertTrue(invalidTopicCode.contains("payload"));
    }

    @Test
    public void testNotificationTimeStampMessage(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_TS_MSG;
        assertTrue(invalidTopicCode.contains("timestamp"));
    }

    @Test
    public void testNotificationHeaderErrorMessage(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_HEADER_ERR_MSG;
        assertTrue(invalidTopicCode.contains("Notification"));
    }
    @Test
    public void testNotificationExpiry(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_EXPIRY;
        assertTrue(invalidTopicCode.contains("expiry"));
    }

    @Test
    public void testNotificationRecipientErrorMessage(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_RECIPIENT_ERR_MSG;
        assertTrue(invalidTopicCode.contains("empty"));
    }
    @Test
    public void testNotificationRecipientList(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_RECIPIENT_LIST;
        assertTrue(invalidTopicCode.contains("empty"));
    }

    @Test
    public void testNotificationMessageError(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_MESSAGE_ERR;
        assertTrue(invalidTopicCode.contains("Notification"));
    }

    @Test
    public void testNotificationTopicError(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_TOPIC_ERR;
        assertTrue(invalidTopicCode.contains("topic"));
    }

    @Test
    public void testNotificationInvalidTopic(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_INVALID_TOPIC;
        assertTrue(invalidTopicCode.contains("topic"));
    }
    @Test
    public void testNotificationStatus(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_STATUS;
        assertTrue(invalidTopicCode.contains("status"));
    }

    @Test
    public void testNotificationTriggerError(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_TRIGGER_ERR;
        assertTrue(invalidTopicCode.contains("notification"));
    }
    @Test
    public void testNotificationRecipientError(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_RECIPIENT_ERR;
        assertTrue(invalidTopicCode.contains("recipient"));
    }

    @Test
    public void testNotificationNotAllowedRecipients(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_NOT_ALLOWED_RECIPIENTS;
        assertTrue(invalidTopicCode.contains("notification"));
    }
    @Test
    public void testNotificationMandatoryHeaders(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_MANDATORY_HEADERS;
        assertTrue(invalidTopicCode.contains("mandatory"));
    }

    @Test
    public void testNotificationNotAllowed(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_NOT_ALLOWED;
        assertTrue(invalidTopicCode.contains("notification"));
    }
    @Test
    public void testNotificationTriggerMsg(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_TRIGGER_ERR_MSG;
        assertTrue(invalidTopicCode.contains("notification"));
    }

    @Test
    public void testRecipientNotAllowed(){
        String invalidTopicCode = ResponseMessage.RECIPIENT_NOT_ALLOWED;
        assertTrue(invalidTopicCode.contains("Recipient"));
    }
    @Test
    public void testUnsubscribeMsg(){
        String invalidTopicCode = ResponseMessage.UNSUBSCRIBE_ERR_MSG;
        assertTrue(invalidTopicCode.contains("unsubscribe"));
    }

    @Test
    public void testEmptySenderList(){
        String invalidTopicCode = ResponseMessage.EMPTY_SENDER_LIST_ERR_MSG;
        assertTrue(invalidTopicCode.contains("participant"));
    }
    @Test
    public void testNotificationSubscribeMessage(){
        String invalidTopicCode = ResponseMessage.NOTIFICATION_SUBSCRIBE_ERR_MSG;
        assertTrue(invalidTopicCode.contains("participant"));
    }

}
