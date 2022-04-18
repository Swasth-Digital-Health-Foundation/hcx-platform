package org.swasth.apigateway.filters;

import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.swasth.apigateway.BaseSpec;
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
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());
        Mockito.when(auditService.getAuditLogs(any()))
                .thenReturn(getAuditLogs())
                .thenReturn(new ArrayList<>())
                .thenReturn(getAuditLogs());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue(getOnRequestBody())
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
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());
        Mockito.when(auditService.getAuditLogs(any())).thenReturn(new ArrayList<>());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue(getErrorRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_request_invalid_api_access_scenario() {
        client.post().uri("/v1/paymentnotice/on_request")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatus());
                    assertEquals(ErrorCodes.ERR_ACCESS_DENIED.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_admin_access_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getHCXAdminDetails())
                .thenReturn(getPayorDetails());
        Mockito.when(auditService.getAuditLogs(any())).thenReturn(new ArrayList<>());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getHCXAdminToken())
                .header("X-jwt-sub", "f698b521-7409-432d-a5db-d13e51f029a9")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_request_missing_authorization_header_scenario() throws Exception {
        client.post().uri("/v1/coverageeligibility/check")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatus());
                    assertEquals(ErrorCodes.ERR_ACCESS_DENIED.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_malformat_authorization_header_scenario() throws Exception {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, "Bearer ")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatus());
                    assertEquals(ErrorCodes.ERR_ACCESS_DENIED.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_invalid_format_authorization_header_scenario() throws Exception {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, "test token")
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatus());
                    assertEquals(ErrorCodes.ERR_ACCESS_DENIED.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_expired_jwt_token_scenario() throws Exception {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getExpiredProviderToken())
                .bodyValue(getRequestBody())
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatus());
                    assertEquals(ErrorCodes.ERR_ACCESS_DENIED.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_audit_service_server_exception_scenario() throws Exception {
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
                    assertEquals(ErrorCodes.SERVICE_UNAVAILABLE.name(), getResponseErrorCode(result));
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
    public void check_hcx_request_invalid_jwe_payload_scenario() {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "null.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
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
                .bodyValue(Collections.singletonMap("payload", ".6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
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

    @Override
    protected Map<String, Object> getRequestBody() {
        return super.getRequestBody();
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

    @Test
    public void check_hcx_request_invalid_sender_scenario() {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS1jZTIzY2NkYy1lNjQ1LTRlMzUtOTdiOC0wYmQ4ZmVmNDNlY2QiLAoieC1oY3gtYXBpX2NhbGxfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTMiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMS0xMC0yN1QyMDozNTo1Mi42MzYrMDUzMCIsCiJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LnF1ZXVlZCIsCiJ4LWhjeC13b3JrZmxvd19pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5NCIKfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_SENDER.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_empty_recipient_details_scenario() throws Exception {
        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_RECIPIENT.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_blocked_recipient_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(new HashMap<>())
                .thenReturn(getBlockedPayorDetails());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_RECIPIENT.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_empty_debug_flag_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0IiwKIngtaGN4LWRlYnVnX2ZsYWciOiIiLAoieC1oY3gtZGVidWdfZGV0YWlscyI6eyJjb2RlIjoiRVJSX0lOVkFMSURfRU5DUllQVElPTiIsIm1lc3NhZ2UiOiJSZWNpcGllbnQgSW52YWxpZCBFbmNyeXB0aW9uIiwidHJhY2UiOiIifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_DEBUG_FLAG.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_invalid_debug_flag_range_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0IiwKIngtaGN4LWRlYnVnX2ZsYWciOiJ0ZXN0IiwKIngtaGN4LWRlYnVnX2RldGFpbHMiOnsiY29kZSI6IkVSUl9JTlZBTElEX0VOQ1JZUFRJT04iLCJtZXNzYWdlIjoiUmVjaXBpZW50IEludmFsaWQgRW5jcnlwdGlvbiIsInRyYWNlIjoiIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_DEBUG_FLAG.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_empty_error_details_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue("{ \"x-hcx-status\": \"response.error\", \"x-hcx-sender_code\": \"1-30b6ac38-bbfe-4012-9d9c-dcbe8420f68f\", \"x-hcx-recipient_code\": \"1-281cca66-55cd-4c1b-96bb-51813cdbbe17\", \"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e4\", \"x-hcx-api_call_id\": \"24aee489-d0d5-47b6-b3c1-559732c4b385\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-error_details\": { } }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_ERROR_DETAILS.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_missing_error_details_fields_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue("{ \"x-hcx-status\": \"response.error\", \"x-hcx-sender_code\": \"1-30b6ac38-bbfe-4012-9d9c-dcbe8420f68f\", \"x-hcx-recipient_code\": \"1-281cca66-55cd-4c1b-96bb-51813cdbbe17\", \"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e4\", \"x-hcx-api_call_id\": \"24aee489-d0d5-47b6-b3c1-559732c4b385\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-error_details\": {\"message\": \"\", \"trace\": \"Recipient Invalid Encryption\" } }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_ERROR_DETAILS.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_invalid_error_details_range_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue("{ \"x-hcx-status\": \"response.error\", \"x-hcx-sender_code\": \"1-30b6ac38-bbfe-4012-9d9c-dcbe8420f68f\", \"x-hcx-recipient_code\": \"1-281cca66-55cd-4c1b-96bb-51813cdbbe17\", \"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e4\", \"x-hcx-api_call_id\": \"24aee489-d0d5-47b6-b3c1-559732c4b385\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-error_details\": { \"code\": \"ERR_INVALID_ENCRYPTION\", \"message\": \"\", \"trace\": \"Recipient Invalid Encryption\", \"test\": \"\"} }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_ERROR_DETAILS.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_forward_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getPayor2Details());
        Mockito.when(auditService.getAuditLogs(any()))
                .thenReturn(getAuditLogs())
                .thenReturn(new ArrayList<>())
                .thenReturn(getAuditLogs());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS1jZTIzY2NkYy1lNjQ1LTRlMzUtOTdiOC0wYmQ4ZmVmNDNlY2QiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTg1ODRiYTY5LTZjNTAtNDUzNS04YWQ1LWMwMmI4YzMxODBhNiIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5NCIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_forward_request_invalid_correlation_id_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getPayor2Details());
        Mockito.when(auditService.getAuditLogs(any()))
                .thenReturn(new ArrayList<>());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS1jZTIzY2NkYy1lNjQ1LTRlMzUtOTdiOC0wYmQ4ZmVmNDNlY2QiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTg1ODRiYTY5LTZjNTAtNDUzNS04YWQ1LWMwMmI4YzMxODBhNiIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5NCIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_FORWARD_REQ.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_forward_request_invalid_recipient_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());
        Mockito.when(auditService.getAuditLogs(any()))
                .thenReturn(getAuditLogs())
                .thenReturn(new ArrayList<>())
                .thenReturn(getAuditLogs());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue(Collections.singletonMap("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS1jZTIzY2NkYy1lNjQ1LTRlMzUtOTdiOC0wYmQ4ZmVmNDNlY2QiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTg1ODRiYTY5LTZjNTAtNDUzNS04YWQ1LWMwMmI4YzMxODBhNiIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5NCIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0Igp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw"))
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                });
    }

    @Test
    public void check_hcx_redirect_request_success_scenario() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("Content-Type", "application/json"));

        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails())
                .thenReturn(getPayor2Details());
        Mockito.when(auditService.getAuditLogs(any()))
                .thenReturn(new ArrayList<>())
                .thenReturn(getOnActionAuditLogs());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue("{ \"x-hcx-recipient_code\": \"1-3a3bd68a-848a-4d52-9ec2-07a92d765fb4\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-sender_code\": \"1-2ff1f493-c4d4-4fc7-8d41-aaabb997af23\", \"x-hcx-correlation_id\": \"5e934f90-111d-4f0b-b016-c22d820674e1\", \"x-hcx-workflow_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5094\", \"x-hcx-api_call_id\": \"26b1060c-1e83-4600-9612-ea31e0ca5194\", \"x-hcx-status\": \"response.redirect\", \"x-hcx-redirect_to\": \"1-74f6cb29-4116-42d0-9fbb-adb65e6a64ac\" }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.getStatus());
                });
    }

    @Test
    public void check_hcx_redirect_request_invalid_api_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());

        client.post().uri("/v1/predetermination/on_submit")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue("{ \"x-hcx-recipient_code\": \"1-3a3bd68a-848a-4d52-9ec2-07a92d765fb4\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-sender_code\": \"1-2ff1f493-c4d4-4fc7-8d41-aaabb997af23\", \"x-hcx-correlation_id\": \"5e934f90-111d-4f0b-b016-c22d820674e1\", \"x-hcx-workflow_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5094\", \"x-hcx-api_call_id\": \"26b1060c-1e83-4600-9612-ea31e0ca5194\", \"x-hcx-status\": \"response.redirect\", \"x-hcx-redirect_to\": \"1-74f6cb29-4116-42d0-9fbb-adb65e6a64ac\" }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_REDIRECT_TO.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_redirect_request_invalid_status_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getPayorDetails())
                .thenReturn(getProviderDetails());

        client.post().uri("/v1/coverageeligibility/on_check")
                .header(Constants.AUTHORIZATION, getPayorToken())
                .header("X-jwt-sub", "20bd4228-a87f-4175-a30a-20fb28983afb")
                .bodyValue("{ \"x-hcx-recipient_code\": \"1-3a3bd68a-848a-4d52-9ec2-07a92d765fb4\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-sender_code\": \"1-2ff1f493-c4d4-4fc7-8d41-aaabb997af23\", \"x-hcx-correlation_id\": \"5e934f90-111d-4f0b-b016-c22d820674e1\", \"x-hcx-workflow_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5094\", \"x-hcx-api_call_id\": \"26b1060c-1e83-4600-9612-ea31e0ca5194\", \"x-hcx-status\": \"response.complete\", \"x-hcx-redirect_to\": \"1-74f6cb29-4116-42d0-9fbb-adb65e6a64ac\" }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_REDIRECT_TO.name(), getResponseErrorCode(result));
                });
    }

    @Test
    public void check_hcx_request_invalid_payload_action_scenario() throws Exception {
        Mockito.when(registryService.fetchDetails(anyString(), anyString()))
                .thenReturn(getProviderDetails())
                .thenReturn(getPayorDetails());

        client.post().uri("/v1/coverageeligibility/check")
                .header(Constants.AUTHORIZATION, getProviderToken())
                .header("X-jwt-sub", "f7c0e759-bec3-431b-8c4f-6b294d103a74")
                .bodyValue("{ \"x-hcx-status\": \"request.queued\", \"x-hcx-sender_code\": \"1-30b6ac38-bbfe-4012-9d9c-dcbe8420f68f\", \"x-hcx-recipient_code\": \"1-281cca66-55cd-4c1b-96bb-51813cdbbe17\", \"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e4\", \"x-hcx-api_call_id\": \"24aee489-d0d5-47b6-b3c1-559732c4b385\", \"x-hcx-timestamp\": \"2021-10-27T20:35:52.636+0530\", \"x-hcx-error_details\": { \"code\": \"ERR_INVALID_ENCRYPTION\", \"message\": \"\", \"trace\": \"Recipient Invalid Encryption\", \"test\": \"\"} }")
                .exchange()
                .expectBody(Map.class)
                .consumeWith(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
                    assertEquals(ErrorCodes.ERR_INVALID_PAYLOAD.name(), getResponseErrorCode(result));
                });
    }

}
