package org.swasth.dp.communication.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.communication.task.CommunicationConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CommunicationProcessFunctionTest {

    Config config  = ConfigFactory.load("communication-test.conf");
    CommunicationConfig communicationConfig = new CommunicationConfig(config,"CommunicationTestJob");

    //instantiate created user defined function
    CommunicationProcessFunction processFunction = new CommunicationProcessFunction(communicationConfig);
    //ObjectMapper objMapper = new ObjectMapper();
    Gson gson = new Gson();


    @Test
    public void testCommunicationJob() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //StreamRecord object with the sample data
        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_COMMUNICATION_REQUEST(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);

        assertEquals(harness.getSideOutput(communicationConfig.auditOutputTag()).size(),1);
        assertEquals(harness.getSideOutput(communicationConfig.retryOutputTag()).size(),1);

        harness.close();

    }

}
