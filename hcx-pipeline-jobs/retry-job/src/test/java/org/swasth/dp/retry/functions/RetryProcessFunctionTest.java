package org.swasth.dp.retry.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.retry.task.RetryConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RetryProcessFunctionTest {

    Config config  = ConfigFactory.load("retry-test.conf");
    RetryConfig retryConfig = new RetryConfig(config,"RetryTestJob");
    //instantiate created user defined function
    RetryProcessFunction processFunction = new RetryProcessFunction(retryConfig);
    Gson gson = new Gson();


    @Test
    public void testRetryJob() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //StreamRecord object with the sample data
        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_RETRY_REQUEST(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);

        assertEquals(harness.getSideOutput(retryConfig.auditOutputTag()).size(),1);

        harness.close();

    }

}
