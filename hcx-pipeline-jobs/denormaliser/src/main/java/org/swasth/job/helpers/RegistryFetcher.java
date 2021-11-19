package org.swasth.job.helpers;

import kong.unirest.HttpResponse;
import org.swasth.job.Platform;
import org.swasth.job.data.ParticipantDetails;
import org.swasth.job.utils.HttpUtil;
import org.swasth.job.utils.JSONUtil;

import java.util.Map;

public interface RegistryFetcher {

    default Map<String,Object> getParticipantDetails(String entityId) throws Exception {
        /*String url = Platform.getString("service.registry.basePath") + "api/v1/Organisation/" + entityId;
        HttpResponse response = HttpUtil.get(url);
        if (response != null && response.getStatus() == 200) {
            return JSONUtil.deserializeToMap(response.getBody().toString());
        } else {
           String msg = "Unable to fetch details for :" + entityId + " | Response Code :" + response.getStatus();
           throw new Exception(msg);
        }*/
        return JSONUtil.deserializeToMap(ParticipantDetails.details);
    }
}

