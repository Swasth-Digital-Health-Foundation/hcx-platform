package org.swasth.hcx.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.swasth.common.StringUtils;

import java.util.*;

@Component
public class KafkaEventGenerator {

    @Autowired
    Environment env;

    public String generatePayloadEvent(String mid, Map<String, Object> requestBody) throws JsonProcessingException {
        Map<String,Object> constructEvent = new HashMap<>();
        constructEvent.put("mid", mid);
        constructEvent.put("payload", requestBody);
        String apiEvent = new ObjectMapper().writeValueAsString(constructEvent);
        return apiEvent;
    }

    public String generateMetadataEvent(String mid, String apiAction, Map<String, Object> requestBody) throws JsonProcessingException {
        Map<String,Object> constructEvent = new HashMap<>();
        List<String> protocolHeaders = (List<String>) env.getProperty("protocol.mandatory.headers", List.class, new ArrayList<String>());
        protocolHeaders.addAll((List<String>) env.getProperty("protocol.optional.headers", List.class, new ArrayList<String>()));
        List<String> domainHeaders = (List<String>) env.getProperty("domain.headers", List.class, new ArrayList<String>());
        Map<String, Object> protectedHeaders = StringUtils.decodeBase64String((String) requestBody.get("protected"));
        Map<String,Object> filterProtocolHeaders = new HashMap<>();
        Map<String,Object> filterDomainHeaders = new HashMap<>();
        protocolHeaders.forEach(key -> {
            if (protectedHeaders.containsKey(key))
                filterProtocolHeaders.put(key, protectedHeaders.get(key));
        });
        domainHeaders.forEach(key -> {
            if (protectedHeaders.containsKey(key))
                filterDomainHeaders.put(key, protectedHeaders.get(key));
        });
        constructEvent.put("mid", mid);
        constructEvent.put("ets", System.currentTimeMillis());
        constructEvent.put("action", apiAction);
        constructEvent.put("protocol_header", filterProtocolHeaders);
        constructEvent.put("domain_header",  filterDomainHeaders);
        constructEvent.put("sender", new HashMap<String,Object>() {{
            put("participant_code", protectedHeaders.get("x-hcx-sender_code"));
        }});
        constructEvent.put("receiver", new HashMap<String,Object>() {{
            put("participant_code", protectedHeaders.get("x-hcx-recipient_code"));
        }});
        constructEvent.put("status", "New");
        constructEvent.put("log_details", "current status: info");
        String metadataEvent = new ObjectMapper().writeValueAsString(constructEvent);
        return metadataEvent;
    }
}
