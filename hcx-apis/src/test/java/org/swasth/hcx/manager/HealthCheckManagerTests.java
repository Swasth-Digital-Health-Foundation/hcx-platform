package org.swasth.hcx.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.swasth.auditindexer.utils.ElasticSearchUtil;
import org.swasth.common.dto.Response;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = { HealthCheckManager.class })
public class HealthCheckManagerTests {

  @MockBean
  IEventService kafkaClient;

  @MockBean
  IDatabaseService postgreSQLClient;

  @MockBean
  RedisCache redisClient;

  @MockBean
  ElasticSearchUtil elasticSearchUtil;
  @Autowired
  HealthCheckManager healthCheckManager;

  @Test
  public void checkAllSystemHealth_test() throws Exception {
    when(kafkaClient.isHealthy()).thenReturn(true);
    when(postgreSQLClient.isHealthy()).thenReturn(true);
    when(redisClient.isHealthy()).thenReturn(true);
    when(elasticSearchUtil.isHealthy()).thenReturn(true);
    Response resp = healthCheckManager.checkAllSystemHealth();
    assertEquals(true, resp.get("healthy"));
  }

}