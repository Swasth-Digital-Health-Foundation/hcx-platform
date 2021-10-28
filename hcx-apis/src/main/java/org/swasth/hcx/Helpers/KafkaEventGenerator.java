package org.swasth.hcx.Helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public String generateMetadataEvent(String mid, String action, HttpHeaders header ,Map<String, Object> payload) throws JsonProcessingException {
        Map<String,Object> constructEvent = new LinkedHashMap<>();
        List<String> protocalHeaders = (List<String>) env.getProperty("protocal.mandatory.headers", List.class);
        protocalHeaders.addAll((List<String>) env.getProperty("protocal.optional.headers", List.class));
        List<String> domainHeaders = (List<String>) env.getProperty("domain.headers", List.class);
        Map<String,Object> filterProtocalHeaders = new HashMap<>();
        Map<String,Object> filterDomainHeaders = new HashMap<>();
        protocalHeaders.forEach(key -> {
            if (header.containsKey(key))
                filterProtocalHeaders.put(key,header.get(key));
        });
        domainHeaders.forEach(key -> {
            if (header.containsKey(key))
                filterDomainHeaders.put(key,header.get(key));
        });
        constructEvent.put("mid", mid);
        constructEvent.put("ets", System.currentTimeMillis());
        constructEvent.put("action", action);
        constructEvent.put("protocol_header", filterProtocalHeaders);
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
