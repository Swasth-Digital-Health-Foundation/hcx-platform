package org.swasth.dp.retry.task;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.fixture.EventFixture;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryStreamTaskTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(1)
                            .setNumberTaskManagers(1)
                            .build());

    Config config = ConfigFactory.load("retry-test.conf");
    RetryConfig retryConfig = new RetryConfig(config, "RetryTestJob");
    BaseJobConfig baseJobConfig = new BaseJobConfig(config, "RetryTestJob");
    FlinkKafkaConnector mockKafkaUtil = mock(new FlinkKafkaConnector(baseJobConfig).getClass());

    @Before
    public void beforeClass() throws Exception {
        KafkaSource mockKafkaSource = mock(KafkaSource.class);
        when(mockKafkaUtil.kafkaMapSource(retryConfig.kafkaInputTopic)).thenReturn(mockKafkaSource);
        flinkCluster.before();
    }

    @After
    public void afterClass() {
        flinkCluster.after();
    }

    @Test
    public void testRetryTask() throws Exception {
        RetryStreamTask task = new RetryStreamTask(retryConfig, mockKafkaUtil);
        task.process(baseJobConfig);

        assertEquals(AuditEventsSink.values.size(),1);
        assertEquals(RetrySink.values.size(), 1);

    }

    private static class RetrySource implements SourceFunction<Map<String,Object>> {

        @Override
        public void run(SourceContext<Map<String, Object>> sourceContext) {
            Gson gson = new Gson();
            Map<String,Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_VALID_RETRY_REQUEST(),HashMap.class);
            sourceContext.collect(eventMap);
        }

        @Override
        public void cancel() {

        }
    }

    private static class AuditEventsSink implements SinkFunction<String> {
        // must be static
        public static final List<String> values = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void invoke(String value){
            values.add(value);
        }
    }

    private static class RetrySink implements SinkFunction<String> {
        // must be static
        public static final List<String> values = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void invoke(String value) {
            values.add(value);
        }
    }
    
}
