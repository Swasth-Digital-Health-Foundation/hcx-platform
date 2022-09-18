package org.swasth.dp.preauth.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.preauth.task.PreauthConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

public class PreauthProcessFunctionTest {

    Config config  = ConfigFactory.load("preauth-test.conf");
    PreauthConfig preauthConfig = new PreauthConfig(config,"PreauthTestJob");

    //instantiate created user defined function
    PreauthProcessFunction processFunction = new PreauthProcessFunction(preauthConfig);
    //ObjectMapper objMapper = new ObjectMapper();
    Gson gson = new Gson();


    @Test
    public void testPreauthJob() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //StreamRecord object with the sample data
        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_EVENT(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);
        harness.close();
    }

}
