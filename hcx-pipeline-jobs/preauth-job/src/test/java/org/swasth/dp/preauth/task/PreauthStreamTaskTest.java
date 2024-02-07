package org.swasth.dp.preauth.task;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

public class PreauthStreamTaskTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(1)
                            .setNumberTaskManagers(1)
                            .build());

    Config config = ConfigFactory.load("preauth-test.conf");
    PreauthConfig preauthConfig = new PreauthConfig(config, "PreauthTestJob");
    BaseJobConfig baseJobConfig = new BaseJobConfig(config, "PreauthTestJob");
    FlinkKafkaConnector mockKafkaUtil = mock(new FlinkKafkaConnector(baseJobConfig).getClass());

    @Before
    public void beforeClass() throws Exception {
        when(mockKafkaUtil.kafkaMapSource(preauthConfig.kafkaInputTopic)).thenReturn(new PreauthSource());
        flinkCluster.before();
    }

    @After
    public void afterClass() {
        flinkCluster.after();
    }

    @Test
    public void testPreauthTask() throws Exception {
        PreauthStreamTask task = new PreauthStreamTask(preauthConfig, mockKafkaUtil);
        task.process(baseJobConfig);

        assertEquals(AuditEventsSink.values.size(),1);
        assertEquals(RetrySink.values.size(), 1);

    }

    private static class PreauthSource implements SourceFunction<Map<String,Object>> {

        @Override
        public void run(SourceContext<Map<String, Object>> sourceContext) throws Exception {
            Gson gson = new Gson();
            Map<String,Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SAMPLE_EVENT(),HashMap.class);
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
        public void invoke(String value) throws Exception {
            values.add(value);
        }
    }

    private static class RetrySink implements SinkFunction<String> {
        // must be static
        public static final List<String> values = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void invoke(String value) throws Exception {
            values.add(value);
        }
    }
}
