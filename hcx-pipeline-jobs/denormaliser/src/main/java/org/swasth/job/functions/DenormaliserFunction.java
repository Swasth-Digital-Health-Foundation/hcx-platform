package org.swasth.job.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.job.helpers.RegistryFetcher;
import org.swasth.job.task.DenormaliserConfig;
import org.swasth.job.utils.JSONUtil;
import java.util.Map;
import java.util.HashMap;

public class DenormaliserFunction extends ProcessFunction<String,String> implements RegistryFetcher {

    @Override
    public void processElement(String event, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
        Map<String, Object> eventMap = JSONUtil.deserializeToMap(event);
        String senderCode = (String) ((Map<String, Object>) eventMap.get("sender")).getOrDefault("participant_code", "");
        String receiverCode = (String) ((Map<String, Object>) eventMap.get("receiver")).getOrDefault("participant_code", "");
        Map<String, Object> senderDetails = getParticipantDetails(senderCode);
        Map<String, Object> receiverDetails = getParticipantDetails(receiverCode);
        ((Map<String, Object>) eventMap.get("sender")).putAll(senderDetails);
        Map<String, Object> audit = new HashMap<String, Object>();
        if(receiverDetails.containsKey("error")) {
            eventMap.put("status","request.invalid");
            eventMap.put("log_details",receiverDetails.get("error"));
            audit.put("timestamp", System.currentTimeMillis());
            audit.put("status", "error");
            context.output(DenormaliserConfig.invalidOutTag, JSONUtil.serialize(eventMap));
            context.output(DenormaliserConfig.auditOutTag, JSONUtil.serialize(audit));
        } else {
            ((Map<String, Object>) eventMap.get("receiver")).putAll(receiverDetails);
            eventMap.put("status", "request.denorm");
            audit.put("timestamp", System.currentTimeMillis());
            audit.put("status", "success");
            context.output(DenormaliserConfig.auditOutTag, JSONUtil.serialize(audit));
            collector.collect(JSONUtil.serialize(eventMap));
        }
    }
}
