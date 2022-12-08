package org.swasth.hcx.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.swasth.common.dto.Request;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;

import java.util.*;

import static org.swasth.common.utils.Constants.*;


public class EventGenerator {

    public EventGenerator() {
    }

    public Map<String,Object> createAuditLog(String id, String objectType, Map<String,Object> cdata, Map<String,Object> edata) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(ETS, System.currentTimeMillis());
        event.put(MID, UUID.randomUUID().toString());
        Map<String,Object> objectMap = new HashMap<>();
        objectMap.put(ID, id);
        objectMap.put(TYPE, objectType);
        event.put(OBJECT, objectMap);
        event.put(CDATA, cdata);
        event.put(EDATA, edata);
        return event;
    }

    public Map<String,Object> generateOnSubscriptionAuditEvent(Request request, String recipientCode, String subscriptionId, String status, String subscriptionStatus) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(MID, request.getMid());
        event.put(ACTION, request.getApiAction());
        event.put(SUBSCRIPTION_ID, subscriptionId);
        event.put(SUBSCRIPTION_STATUS, subscriptionStatus);
        event.put(HCX_SENDER_CODE, request.getSenderCode());
        event.put(HCX_RECIPIENT_CODE, recipientCode);
        event.put(ETS, System.currentTimeMillis());
        event.put(STATUS, status);
        return  event;
    }


    public Map<String,Object> generateSubscriptionAuditEvent(Request request,String status,List<String> senderList) {
        Map<String,Object> event = new HashMap<>();
        event.put(EID, AUDIT);
        event.put(MID, request.getMid());
        event.put(ACTION, request.getApiAction());
        event.put(TOPIC_CODE,request.getTopicCode() == null ? "" : request.getTopicCode());
        event.put(SENDER_LIST,senderList);
        event.put(HCX_RECIPIENT_CODE,request.getRecipientCode());
        event.put(ETS,System.currentTimeMillis());
        event.put(STATUS, status);
        return  event;
    }

}
