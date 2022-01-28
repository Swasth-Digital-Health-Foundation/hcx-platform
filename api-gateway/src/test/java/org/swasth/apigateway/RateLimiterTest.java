//package com.addverb.apigateway;
//
//import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
//import com.carrotsearch.junitbenchmarks.BenchmarkRule;
//import org.junit.BeforeClass;
//import org.junit.ClassRule;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TestRule;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.MockServerContainer;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@RunWith(SpringRunner.class)
//public class RateLimiterTest {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterTest.class);
//
//    @Rule
//    public TestRule benchmarkRun = new BenchmarkRule();
//
//    @ClassRule
//    public static MockServerContainer mockServer = new MockServerContainer();
//    @ClassRule
//    public static GenericContainer redis = new GenericContainer("redis:5.0.6").withExposedPorts(6379);
//
//    @Autowired
//    TestRestTemplate template;
//
//    @BeforeClass
//    public static void init() {
//        System.setProperty("spring.cloud.gateway.routes[0].id", "http_bin_anything_route");
//        System.setProperty("spring.cloud.gateway.routes[0].uri", "http://httpbin.org/anything");
//        System.setProperty("spring.cloud.gateway.routes[0].predicates[0]", "After=2017-01-20T17:42:47" +
//                ".789-07:00[America/Denver]");
//        System.setProperty("spring.cloud.gateway.routes[0].filters[0].name", "RequestRateLimiter");
//        System.setProperty("spring.cloud.gateway.routes[0].filters[0].args.redis-rate-limiter.replenishRate", "10");
//        System.setProperty("spring.cloud.gateway.routes[0].filters[0].args.redis-rate-limiter.burstCapacity", "20");
//        System.setProperty("spring.redis.host", "127.0.0.1");
//        System.setProperty("spring.redis.port", "" + redis.getMappedPort(6379));
//    }
//
//    @Test
//    @BenchmarkOptions(warmupRounds = 0, concurrency = 6, benchmarkRounds = 50)
//    public void testAccountService() {
//        HttpHeaders headers = new HttpHeaders();
////        headers.add("user", UUID.randomUUID().toString());
//        headers.add("user", "1");
//
//        HttpEntity<String> entity = new HttpEntity<>("body", headers);
//        ResponseEntity<String> r = template.exchange("http://localhost:8080/get", HttpMethod.GET, entity, String.class);
//        LOGGER.info("Received: status->{},  remaining->{}", r.getStatusCodeValue(), r.getHeaders().get("X-RateLimit-Remaining"));
//    }
//
//}
