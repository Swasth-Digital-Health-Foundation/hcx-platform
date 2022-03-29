package org.swasth.apigateway;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.swasth.apigateway.constants.Constants;
import org.swasth.apigateway.exception.ErrorCodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class HCXRequestTest extends BaseSpec {

    @Test
    public void check_health_request_success_scenario() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        client.get().uri("/health")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.OK, result.getStatus());
                });
    }

    @Test
    public void check_hcx_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());
        Mockito.when(auditService.getAuditLogs(any())).thenReturn(new ArrayList<>());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_on_action_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());
        Mockito.when(auditService.getAuditLogs(any()))
                .thenReturn(getAuditLogs())
                .thenReturn(new ArrayList<>())
                .thenReturn(getAuditLogs());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_json_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());
        Mockito.when(auditService.getAuditLogs(any())).thenReturn(new ArrayList<>());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(getErrorRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_request_invalid_payload_scenario() {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "test"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_PAYLOAD.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_payload_parsing_exception_scenario() {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "test.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_PAYLOAD.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_missing_mandatory_headers_scenario() throws Exception {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC13b3JrZmxvd19pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5NCIKfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_MANDATORY_HEADERFIELD_MISSING.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_invalid_api_call_id_scenario() {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_API_CALL_ID.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_invalid_timestamp_scenario() {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYrIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_TIMESTAMP.name(), getResponseErrorCode(result));
                });
    }

}
