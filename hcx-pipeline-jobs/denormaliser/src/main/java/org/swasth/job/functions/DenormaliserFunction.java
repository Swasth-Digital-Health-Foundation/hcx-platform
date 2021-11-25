package org.swasth.job.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.swasth.job.task.DenormaliserConfig;
import org.swasth.job.utils.JSONUtil;

import java.util.Map;

public class DenormaliserFunction extends ProcessFunction<String,String> {

    Denormaliser denormaliser = new Denormaliser();

    @Override
    public void processElement(String event, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {
        Map<String, Object> result = denormaliser.denormalise(event);
        if(result.get("status").equals("request.invalid")) {
            context.output(DenormaliserConfig.invalidOutTag, JSONUtil.serialize(result));
        } else {
            result.put("status", "request.denorm");
            collector.collect(JSONUtil.serialize(result));
        }
    }
}
