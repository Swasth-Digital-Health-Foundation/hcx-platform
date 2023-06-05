package org.swasth.dp.fetch.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.fetch.task.FetchConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

public class FetchProcessFunctionTest {

    Config config  = ConfigFactory.load("fetch-test.conf");
    FetchConfig fetchConfig = new FetchConfig(config,"FetchTestJob");

    //instantiate created user defined function
    FetchProcessFunction processFunction = new FetchProcessFunction(fetchConfig);
    //ObjectMapper objMapper = new ObjectMapper();
    Gson gson = new Gson();


    @Test
    public void testFetchJob() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //StreamRecord object with the sample data
        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_EVENT(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);
        harness.close();

    }

}
