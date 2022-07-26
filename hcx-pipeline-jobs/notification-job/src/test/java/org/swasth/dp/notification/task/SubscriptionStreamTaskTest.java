package org.swasth.dp.notification.task;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.swasth.dp.core.job.BaseJobConfig;
import org.swasth.dp.core.job.FlinkKafkaConnector;
import org.swasth.dp.core.service.AuditService;
import org.swasth.dp.core.util.ElasticSearchUtil;
import org.swasth.fixture.EventFixture;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

public class SubscriptionStreamTaskTest {

    @ClassRule
    public static MiniClusterWithClientResource flinkCluster =
            new MiniClusterWithClientResource(
                    new MiniClusterResourceConfiguration.Builder()
                            .setNumberSlotsPerTaskManager(1)
                            .setNumberTaskManagers(1)
                            .build());

    Config config = ConfigFactory.load("notification-test.conf");
    NotificationConfig notificationConfig = new NotificationConfig(config, "SubscriptionNotificationTestJob");
    BaseJobConfig baseJobConfig = new BaseJobConfig(config, "SubscriptionNotificationTestJob");
    FlinkKafkaConnector mockKafkaUtil = mock(new FlinkKafkaConnector(baseJobConfig).getClass());
    AuditService mockAuditService = mock(new AuditService(baseJobConfig).getClass());
    ElasticSearchUtil mockEsUtil = mock(new ElasticSearchUtil(notificationConfig.esUrl(), notificationConfig.auditIndex(), notificationConfig.batchSize()).getClass());

    @Before
    public void beforeClass() throws Exception {
        when(mockKafkaUtil.kafkaMapSource(notificationConfig.subscriptionInputTopic)).thenReturn(new SubscriptionSource());
        doNothing().when(mockAuditService).indexAudit(anyMap());
        doNothing().when(mockEsUtil).addIndex(anyString(),anyString(),anyString(),anyString());
        doNothing().when(mockEsUtil).addDocumentWithIndex(anyString(),anyString(),anyString());
        flinkCluster.before();
    }

    @After
    public void afterClass() {
        flinkCluster.after();
    }

    @Test
    public void testSubscriptionStreamTask() throws Exception {
        SubscriptionStreamTask task = new SubscriptionStreamTask(notificationConfig, mockKafkaUtil);
        task.process(baseJobConfig);
    }

    private static class SubscriptionSource implements SourceFunction<Map<String,Object>> {

        @Override
        public void run(SourceContext<Map<String, Object>> sourceContext) {
            Gson gson = new Gson();
            Map<String,Object> eventMap = (HashMap<String,Object>) gson.fromJson(EventFixture.SUBSCRIPTION_TOPIC(),HashMap.class);
            sourceContext.collect(eventMap);
        }

        @Override
        public void cancel() {

        }
    }
}
