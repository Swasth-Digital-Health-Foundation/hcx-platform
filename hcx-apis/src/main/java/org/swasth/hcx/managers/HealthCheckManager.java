package org.swasth.hcx.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.swasth.common.dto.Response;
import org.swasth.common.utils.Constants;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HealthCheckManager {

    @Autowired
    private IEventService kafkaClient;
    @Autowired
    private IDatabaseService postgreSQLClient;
    @Autowired
    private RedisCache redisClient;
    public static boolean allSystemHealthResult = true;

    @PostConstruct
    public void init() {
        checkAllSystemHealth();
    }

    public Response checkAllSystemHealth() {
        List<Map<String,Object>> allChecks = new ArrayList<>();
        allChecks.add(generateCheck(Constants.KAFKA, kafkaClient.isHealthy()));
        allChecks.add(generateCheck(Constants.POSTGRESQL, postgreSQLClient.isHealthy()));
        allChecks.add(generateCheck(Constants.REDIS, redisClient.isHealthy()));
        for(Map<String,Object> check:allChecks) {
            if((boolean)check.get(Constants.HEALTHY)) {
                allSystemHealthResult = true;
            } else {
                allSystemHealthResult = false;
                break;
            }
        }
        Response response = new Response(Constants.CHECKS, allChecks);
        response.put(Constants.HEALTHY, allSystemHealthResult);
        return response;
    }

    private Map<String,Object> generateCheck(String serviceName, boolean health){
        return new HashMap<>() {{
            put(Constants.NAME, serviceName);
            put(Constants.HEALTHY, health);
        }};
    }
}