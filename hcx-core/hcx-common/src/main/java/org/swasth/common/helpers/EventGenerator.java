package org.swasth.common.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.swasth.common.dto.Request;
import org.swasth.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.*;


public class EventGenerator {

    private List<String> protocolHeaders;
    private List<String> joseHeaders;
    private List<String> redirectHeaders;
    private List<String> errorHeaders;

    public EventGenerator(List<String> protocolHeaders, List<String> joseHeaders, List<String> redirectHeaders, List<String> errorHeaders) {
        this.protocolHeaders = protocolHeaders;
        this.joseHeaders = joseHeaders;
        this.redirectHeaders = redirectHeaders;
        this.errorHeaders = errorHeaders;
    }

    public String generatePayloadEvent(String mid, Request request) throws JsonProcessingException {
        Map<String,Object> event = new HashMap<>();
        event.put(MID, mid);
        event.put(PAYLOAD, request.getPayload());
        return JSONUtils.serialize(event);
    }

    public String generateMetadataEvent(String mid, String apiAction, Request request) throws Exception {
        Map<String,Object> event = new HashMap<>();
        if (request.getPayload().containsKey(PAYLOAD)) {
            Map<String,Object> protectedHeaders = request.getHcxHeaders();
            Map<String,Object> filterJoseHeaders = new HashMap<>();
            Map<String,Object> filterProtocolHeaders = new HashMap<>();
            joseHeaders.forEach(key -> {
                if (protectedHeaders.containsKey(key))
                    filterJoseHeaders.put(key, protectedHeaders.get(key));
            });
            protocolHeaders.forEach(key -> {
                if (protectedHeaders.containsKey(key))
                    filterProtocolHeaders.put(key, protectedHeaders.get(key));
            });
            event.put(MID, mid);
            event.put(ETS, System.currentTimeMillis());
            event.put(ACTION, apiAction);
            Map<String,Object> headers = new HashMap<>();
            headers.put(JOSE, filterJoseHeaders);
            headers.put(PROTOCOL, filterProtocolHeaders);
            event.put(HEADERS, headers);
            event.put("status", "request.queued");
        } else {
            List<String> headers;
            if(REDIRECT_STATUS.equalsIgnoreCase(request.getStatus())) {
                headers = redirectHeaders;
            }else {
                headers = errorHeaders;
            }
            Map<String, Object> protectedHeaders = request.getHcxHeaders();
            Map<String, Object> filterHeaders = new HashMap<>();
            if(headers != null) {
                headers.forEach(key -> {
                    if (protectedHeaders.containsKey(key))
                        filterHeaders.put(key, protectedHeaders.get(key));
                });
                Map<String, Object> headerMap = new HashMap<>();
                headerMap.put(PROTOCOL, filterHeaders);
                event.put(HEADERS, headerMap);
            }
            event.put(MID, mid);
            event.put(ETS, System.currentTimeMillis());
            event.put(ACTION, apiAction);
            event.put("status", "request.queued");
        }
        return JSONUtils.serialize(event);
    }
}
