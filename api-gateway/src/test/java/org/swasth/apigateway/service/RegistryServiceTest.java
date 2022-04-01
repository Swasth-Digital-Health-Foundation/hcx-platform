package org.swasth.apigateway.service;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.swasth.apigateway.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class RegistryServiceTest extends BaseSpec {


    @Test
    public void check_registry_service_server_exception_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"test\":\"123\"}]")
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.getDetails(any(), any())).thenCallRealMethod();
        ReflectionTestUtils.setField(registryService, "registryUrl", "http://localhost:8080");
        Exception exception = assertThrows(Exception.class, () -> {
            registryService.getDetails("osid", "1-5e934f90-111d-4f0b-b016-c22d820674e1");
        });
        assertEquals("Error connecting to registry service: org.apache.http.NoHttpResponseException: localhost:8080 failed to respond", exception.getMessage());
    }

    @Test
    public void check_registry_service_internal_server_exception_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.getDetails(any(), any())).thenCallRealMethod();
        ReflectionTestUtils.setField(registryService, "registryUrl", "http://localhost:8080");
        Exception exception = assertThrows(Exception.class, () -> {
            registryService.getDetails("osid", "1-5e934f90-111d-4f0b-b016-c22d820674e1");
        });
        assertEquals("Error in fetching the participant details400", exception.getMessage());
    }
}
