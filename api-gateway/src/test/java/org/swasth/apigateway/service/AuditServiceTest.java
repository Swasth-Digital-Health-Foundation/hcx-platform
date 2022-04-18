package org.swasth.apigateway.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AuditService.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuditServiceTest{

    private MockWebServer auditServer =  new MockWebServer();

    @Autowired
    private AuditService auditService;

    @BeforeAll
    public void setup() throws IOException {
        auditServer.start(8080);
    }

    @AfterAll
    public void teardown() throws IOException {
        auditServer.shutdown();
    }

    @Test
    public void check_audit_server_success_scenario() throws Exception {
        auditServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"test\":\"123\"}]")
                .addHeader("Content-Type", "application/json"));

        List<Map<String, Object>> result = auditService.getAuditLogs(Collections.singletonMap("x-hcx-correlation_id", "5e934f90-111d-4f0b-b016-c22d820674e1"));
        assertFalse(result.isEmpty());
    }

}