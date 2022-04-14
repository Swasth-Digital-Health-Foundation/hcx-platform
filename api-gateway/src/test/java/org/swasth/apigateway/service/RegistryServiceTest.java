package org.swasth.apigateway.service;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.swasth.apigateway.BaseSpec;
import org.swasth.apigateway.constants.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class RegistryServiceTest extends BaseSpec {

    @Test
    public void check_registry_service_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"test\":\"123\"}]")
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.getDetails(any(), any())).thenCallRealMethod();
        ReflectionTestUtils.setField(registryService, "registryUrl", "http://localhost:8080");
        Map<String, Object> result = registryService.getDetails("osid", "1-5e934f90-111d-4f0b-b016-c22d820674e1");
        assertFalse(result.isEmpty());
    }

    @Test
    public void check_registry_service_server_exception_scenario() throws Exception {
        Mockito.when(registryService.getDetails(any(), any())).thenCallRealMethod();
        ReflectionTestUtils.setField(registryService, "registryUrl", "http://localhost:8081");
        Exception exception = assertThrows(Exception.class, () -> {
            registryService.getDetails("osid", "1-5e934f90-111d-4f0b-b016-c22d820674e1");
        });
    }

}