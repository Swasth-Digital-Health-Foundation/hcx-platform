package org.swasth.job.functions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.swasth.job.helpers.RegistryFetcher;
import org.swasth.job.utils.JSONUtil;

import java.io.Serializable;
import java.util.Map;

public class Denormaliser implements RegistryFetcher, Serializable {

    public Map<String, Object> denormalise(String event) throws Exception {
        Map<String, Object> eventMap = JSONUtil.deserializeToMap(event);
        String senderCode = (String) ((Map<String, Object>) eventMap.get("sender")).getOrDefault("participant_code", "");
        String receiverCode = (String) ((Map<String, Object>) eventMap.get("receiver")).getOrDefault("participant_code", "");
        Map<String, Object> senderDetails = getParticipantDetails(senderCode);
        Map<String, Object> receiverDetails = getParticipantDetails(receiverCode);
        ((Map<String, Object>) eventMap.get("sender")).putAll(senderDetails);
        if(receiverDetails.containsKey("error")) {
            eventMap.put("status","request.invalid");
            eventMap.put("log_details",receiverDetails.get("error"));
        } else {
            ((Map<String, Object>) eventMap.get("receiver")).putAll(receiverDetails);
        }
        return eventMap;
    }
}
