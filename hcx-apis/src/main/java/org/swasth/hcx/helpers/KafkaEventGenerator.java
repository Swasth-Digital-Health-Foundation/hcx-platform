package org.swasth.hcx.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KafkaEventGenerator {

    @Autowired
    Environment env;

    public String generatePayloadEvent(String mid, Map<String, Object> payload) throws JsonProcessingException {
        Map<String,Object> constructEvent = new LinkedHashMap<>();
        constructEvent.put("mid",mid);
        constructEvent.put("payload",payload);
        String apiEvent = new ObjectMapper().writeValueAsString(constructEvent);
        return apiEvent;
    }

    public String generateMetadataEvent(String mid, String action, HttpHeaders header) throws JsonProcessingException {
        Map<String,Object> constructEvent = new LinkedHashMap<>();
        List<String> protocolHeaders = (List<String>) env.getProperty("protocol.mandatory.headers", List.class, new ArrayList<String>());
        protocolHeaders.addAll((List<String>) env.getProperty("protocol.optional.headers", List.class, new ArrayList<String>()));
        List<String> domainHeaders = (List<String>) env.getProperty("domain.headers", List.class, new ArrayList<String>());
        Map<String,Object> filterProtocolHeaders = new HashMap<>();
        Map<String,Object> filterDomainHeaders = new HashMap<>();
        protocolHeaders.forEach(key -> {
            if (header.containsKey(key))
                filterProtocolHeaders.put(key,header.get(key));
        });
        domainHeaders.forEach(key -> {
            if (header.containsKey(key))
                filterDomainHeaders.put(key,header.get(key));
        });
        constructEvent.put("mid", mid);
        constructEvent.put("ets", System.currentTimeMillis());
        constructEvent.put("action", action);
        constructEvent.put("protocol_header", filterProtocolHeaders);
        constructEvent.put("domain_header",  filterDomainHeaders);
        constructEvent.put("sender", new HashMap<String,Object>() {{
            put("participant_code", header.get("x-hcx-sender_code"));
        }});
        constructEvent.put("receiver", new HashMap<String,Object>() {{
            put("participant_code", header.get("x-hcx-recipient_code"));
        }});
        constructEvent.put("status", "New");
        constructEvent.put("log_details", "current status: info");
        String metadataEvent = new ObjectMapper().writeValueAsString(constructEvent);
        return metadataEvent;
    }
}
