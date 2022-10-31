package org.swasth.dp.notification.functions;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.ProcessFunctionTestHarnesses;
import org.junit.Test;
import org.swasth.dp.notification.task.NotificationConfig;

import java.util.HashMap;
import java.util.Map;

public class NotificationFilterFunctionTest {

    Config config  = ConfigFactory.load("notification-test.conf");
    NotificationConfig notificationConfig = new NotificationConfig(config,"NotificationTestJob");
    NotificationFilterFunction processFunction = new NotificationFilterFunction(notificationConfig);
    Gson gson = new Gson();


    @Test
    public void testNotificationProcess() throws Exception {
        // wrap user defined function into the corresponding operator
        OneInputStreamOperatorTestHarness<Map<String, Object>, Map<String, Object>> harness = ProcessFunctionTestHarnesses.forProcessFunction(processFunction);

        //TODO StreamRecord object with the sample data
        Map<String, Object> eventMap = new HashMap<>();//(HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_RETRY_REQUEST(),HashMap.class);
        StreamRecord<Map<String, Object>> testData  = new StreamRecord<>(eventMap);
        harness.processElement(testData);
        harness.close();
    }
}
