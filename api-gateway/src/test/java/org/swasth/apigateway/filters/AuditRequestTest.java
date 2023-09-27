package org.swasth.apigateway.filters;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.swasth.apigateway.BaseSpec;
import org.swasth.common.utils.Constants;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

public class AuditRequestTest extends BaseSpec {

    @Test
    public void check_audit_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.getDetails(anyString()))
                .thenReturn(Collections.singletonList(getProviderDetails()));

        client.post().uri(versionPrefix + Constants.AUDIT_SEARCH)
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "8527853c-b442-44db-aeda-dbbdcf472d9b")
                .bodyValue(getAuditRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(result.getStatus(), HttpStatus.ACCEPTED);
                });
    }

    @Test
    public void check_audit_request_invalid_sender_scenario() throws Exception {
        Mockito.when(registryService.getDetails(anyString()))
                .thenReturn(Collections.EMPTY_LIST);

        client.post().uri(versionPrefix + Constants.AUDIT_SEARCH)
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "test")
                .bodyValue(getAuditRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(result.getStatus(), HttpStatus.BAD_REQUEST);
                });
    }

}