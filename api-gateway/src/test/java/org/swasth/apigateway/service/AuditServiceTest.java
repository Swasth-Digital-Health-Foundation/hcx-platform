package org.swasth.apigateway.service;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.swasth.apigateway.BaseSpec;
import org.swasth.apigateway.constants.Constants;
import org.swasth.apigateway.exception.ErrorCodes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


public class AuditServiceTest extends BaseSpec {

    @Test
    public void check_audit_service_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"test\":\"123\"}]")
                .addHeader("Content-Type", "application/json"));

        Mockito.when(auditService.getAuditLogs(any())).thenCallRealMethod();
        ReflectionTestUtils.setField(auditService, "hcxApiUrl", "http://localhost:8080");
        List<Map<String, Object>> result = auditService.getAuditLogs(Collections.singletonMap("x-hcx-correlation_id", "5e934f90-111d-4f0b-b016-c22d820674e1"));
        assertFalse(result.isEmpty());
    }

    @Test
    public void check_audit_service_server_exception_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());
        Mockito.when(auditService.getAuditLogs(any())).thenCallRealMethod();
        ReflectionTestUtils.setField(auditService, "hcxApiUrl", "http://localhost:8081");

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
                    assertEquals(ErrorCodes.SERVICE_UNAVAILABLE.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_audit_service_internal_server_exception_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));
        AuditService audit = new AuditService();
        ReflectionTestUtils.setField(auditService, "hcxApiUrl", "http://localhost:8080");
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());
        Mockito.when(auditService.getAuditLogs(any())).thenCallRealMethod();

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
                });
    }

}