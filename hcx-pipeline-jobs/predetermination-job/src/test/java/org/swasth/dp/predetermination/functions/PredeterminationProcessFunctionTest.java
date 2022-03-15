package org.swasth.dp.predetermination.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.predetermination.task.PredeterminationConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PredeterminationProcessFunctionTest {

    Config config  = ConfigFactory.load("predetermination-test.conf");
    PredeterminationConfig predeterminationConfig = new PredeterminationConfig(config,"PredeterminationTestJob");
    //instantiate created user defined function
    PredeterminationProcessFunction processFunction = new PredeterminationProcessFunction(predeterminationConfig);
    Gson gson = new Gson();


    @Test
    public void testPredeterminationJob() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //StreamRecord object with the sample data
        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_PREDETERMINATION_REQUEST(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);

        assertEquals(harness.getSideOutput(predeterminationConfig.auditOutputTag()).size(),1);

        harness.close();

    }

}
