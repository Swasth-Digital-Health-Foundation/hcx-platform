package org.swasth.job.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class DenormaliserFunction extends ProcessFunction<String,String> {

    @Override
    public void processElement(String s, ProcessFunction<String, String>.Context context, Collector<String> collector) throws Exception {

    }
}
