package org.swasth.job.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.job.helpers.RegistryFetcher;
import org.swasth.job.utils.JSONUtil;

import java.util.Map;

public class DenormaliserFunction extends ProcessFunction<String,String> implements RegistryFetcher {

    @Override
    public void processElement(String event, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
        Map<String,Object> eventMap = JSONUtil.deserializeToMap(event);
        String senderCode = (String) ((Map<String,Object>)eventMap.get("sender")).getOrDefault("participant_code","");
        String receiverCode = (String) ((Map<String,Object>)eventMap.get("receiver")).getOrDefault("participant_code","");
        Map<String,Object> senderDetails = getParticipantDetails(senderCode);
        Map<String,Object> receiverDetails = getParticipantDetails(receiverCode);
        ((Map<String,Object>) eventMap.get("sender")).putAll(senderDetails);
        ((Map<String,Object>) eventMap.get("receiver")).putAll(receiverDetails);
        collector.collect(JSONUtil.serialize(eventMap));
    }
}
