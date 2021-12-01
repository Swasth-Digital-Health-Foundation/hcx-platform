package org.swasth.job.helpers;

import org.swasth.job.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public interface RegistryFetcher {

    default Map<String,Object> getParticipantDetails(String entityId) throws Exception {
        /*String details = RedisCache.get(entityId);
        if(details == null || details.isEmpty()) {
            String url = Platform.getString("service.registry.basePath") + "/api/v1/Organisation/" + entityId;
            HttpResponse response = HttpUtils.get(url);
            if (response != null && response.getStatus() == 200) {
                String body = response.getBody().toString();
                RedisCache.set(entityId,body);
                details = body;
            } else {
                String msg = "Unable to fetch details for :" + entityId + " | Response Code :" + response.getStatus();
                return Collections.singletonMap("error", msg);
            }
        }
        return JSONUtil.deserialize(details, HashMap.class);*/
        //Returning sample registry response
        return StringUtils.deserialize("{ \"participantCode\": \"123456\", \"hfrCode\": \"0001\", \"participantName\": \"Test Provider\", \"roles\": \"admin\", \"address\": { \"plot\": \"2-340\", \"street\": \"Baker street\", \"landmark\": \"clock tower\", \"locality\": \"Nagar\", \"village\": \"Miyapur\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500045\" }, \"email\": [ \"testprovider@gmail.com\" ], \"phone\": [ \"040-98765345\" ], \"mobile\": [ \"987654321\" ], \"status\": \"Created\", \"signingCertPath\": \"urn:isbn:0-476-27557-4\", \"encryptionCert\": \"urn:isbn:0-4234\", \"endpointUrl\": \"/testurl\", \"paymentDetails\": { \"accountNumber\":\"123465\", \"ifscCode\":\"HDFC\" } }", HashMap.class);
    }
}

