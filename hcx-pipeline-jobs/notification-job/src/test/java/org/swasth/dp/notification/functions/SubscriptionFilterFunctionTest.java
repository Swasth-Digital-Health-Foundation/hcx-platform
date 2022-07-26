package org.swasth.dp.notification.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.core.util.JSONUtil;
import org.swasth.dp.notification.task.NotificationConfig;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SubscriptionFilterFunctionTest {

    Config config  = ConfigFactory.load("notification-test.conf");
    NotificationConfig notificationConfig = new NotificationConfig(config,"SubscriptionFilterTestJob");
    SubscriptionFilterFunction processFunction = new SubscriptionFilterFunction(notificationConfig);
    Gson gson = new Gson();


    @Test
    public void testOnSubscriptionProcess() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_ON_SUBSCRIBE_EVENT(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);

        assertEquals(1,harness.getSideOutput(notificationConfig.onSubscribeOutputTag()).size());
        harness.close();
    }

    @Test
    public void testSubscriptionProcess() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        Map<String, Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_SUBSCRIBE_EVENT(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);

        assertEquals(3,harness.getSideOutput(notificationConfig.subscribeOutputTag()).size());
        harness.close();
    }
}
