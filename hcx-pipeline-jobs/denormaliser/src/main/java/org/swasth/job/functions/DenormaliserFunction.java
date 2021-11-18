package org.swasth.job.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.job.data.ParticipantDetails;
import org.swasth.job.utils.JSONUtil;
import java.util.Map;

public class DenormaliserFunction extends ProcessFunction<String,String> {

    @Override
    public void processElement(String event, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
        Map<String,Object> eventMap = JSONUtil.deserializeToMap(event);
        Map<String,Object> senderDetails = JSONUtil.deserializeToMap(ParticipantDetails.senderDetails);
        Map<String,Object> receiverDetails = JSONUtil.deserializeToMap(ParticipantDetails.receiverDetails);
        eventMap.put("sender",senderDetails);
        eventMap.put("receiver",receiverDetails);
        collector.collect(JSONUtil.serialize(eventMap));
    }
}
