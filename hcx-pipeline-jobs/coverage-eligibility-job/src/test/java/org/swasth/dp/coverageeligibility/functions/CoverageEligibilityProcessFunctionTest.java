package org.swasth.dp.coverageeligibility.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.coverageeligiblity.functions.CoverageEligibilityProcessFunction;
import org.swasth.dp.coverageeligiblity.task.CoverageEligibilityConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

public class CoverageEligibilityProcessFunctionTest {

    Config config  = ConfigFactory.load("coverage-eligibility-test.conf");
    CoverageEligibilityConfig coverageEligibilityConfig = new CoverageEligibilityConfig(config,"CoverageEligibilityTestJob");

    //instantiate created user defined function
    CoverageEligibilityProcessFunction processFunction = new CoverageEligibilityProcessFunction(coverageEligibilityConfig);
    //ObjectMapper objMapper = new ObjectMapper();
    Gson gson = new Gson();


    @Test
    public void testCoverageEligibilityJob() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //StreamRecord object with the sample data
        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_EVENT(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);
        harness.close();
    }

}