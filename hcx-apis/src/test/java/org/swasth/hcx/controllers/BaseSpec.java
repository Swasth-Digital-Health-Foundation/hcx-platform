package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.swasth.ICloudService;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Response;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.service.RegistryService;
import org.swasth.common.utils.Constants;
import org.swasth.common.utils.JSONUtils;
import org.swasth.common.utils.JWTUtils;
import org.swasth.hcx.config.GenericConfiguration;
import org.swasth.hcx.controllers.v1.*;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.hcx.helpers.ParticipantHelper;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.hcx.service.*;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


@WebMvcTest({CoverageEligibilityController.class, PreAuthController.class, ClaimsController.class, PaymentsController.class, StatusController.class, SearchController.class, CommunicationController.class, PredeterminationController.class, ParticipantController.class, NotificationController.class, AuditService.class, NotificationService.class, EventHandler.class, EventGenerator.class, ParticipantService.class , RetryController.class , UserController.class , UserService.class , BaseRegistryService.class, ParticipantHelper.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Import(GenericConfiguration.class)
public class BaseSpec {

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Mock
    protected Environment mockEnv;

    @MockBean
    protected IEventService mockKafkaClient;

    @MockBean
    protected IDatabaseService postgreSQLClient;

    @MockBean
    protected HealthCheckManager healthCheckManager;

    @MockBean
    protected AuditIndexer auditIndexer;

    @MockBean
    protected RedisCache redisCache;

    @MockBean
    protected AuditService auditService;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected EventHandler eventHandler;

    @MockBean
    protected EventGenerator mockEventGenerator;

    @MockBean
    protected RegistryService mockRegistryService;

    @MockBean
    protected RestHighLevelClient restHighLevelClient;

    @MockBean
    protected JWTUtils jwtUtils;

    @MockBean
    protected ICloudService cloudStorageClient;

    @Autowired
    protected ParticipantService participantService;

    @Autowired
    protected ParticipantHelper participantHelper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    public String getResponseErrorMessage(Map<String, Object> responseBody) {
        return (String) ((Map<String, Object>) responseBody.get("error")).get("message");
    }

    public String getResponseErrorCode(Map<String, Object> responseBody) {
        return (String) ((Map<String, Object>) responseBody.get("error")).get("code");
    }

    public String getRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS05ODc1Ni1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJyZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MTAxIn0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getCommunicationRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS05ODc1Ni1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMiIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJyZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MTAxIn0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getErrorRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("x-hcx-status", "response.error");
        obj.put("x-hcx-sender_code", "1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code", "1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id", "5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-workflow_id", "1e83-460a-4f0b-b016-c22d820674e1");
        obj.put("x-hcx-error_details", new HashMap<>() {{
            put("code", "ERR_INVALID_ENCRYPTION");
            put("error", "");
            put("trace", "Recipient Invalid Encryption");
        }});
        return JSONUtils.serialize(obj);
    }

    public String getBadRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoieC1oY3gtc3RhdHVzX2ZpbHRlcnMiOnsicmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTEwMSJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getExceptionRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoxMjMsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjEtN2JhMDdlMzEtY2ViYi00NzUxLWIyYjctZmUwNTBkOWQyYzAwIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoiODU0ZmU0MWItMjEyZi00YTU1LWJlMmYtMTBiZGE4ZGFkYzk1IiwKIngtaGN4LXRpbWVzdGFtcCI6IjIwMjItMDUtMTJUMTU6MjY6MTkuNjI3KzA1MzAiLAoieC1oY3gtYXBpX2NhbGxfaWQiOiJhYTFlM2Y5Yi05MGE3LTRlZDktOTgyMS0wMzA2ZjFiY2I3NDYiCn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getHeadersMissingRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    protected String getStatusRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiOTNmOTA4YmEiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJhcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTEwMSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    protected String getOnStatusRequestBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("payload", "eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtYXBpX2NhbGxfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0wNlQwOTo1MDoyMyswMCIsCiJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LmluaXRpYXRlIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMWU4My00NjBhLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtc3RhdHVzX3Jlc3BvbnNlIjp7ImVudGl0eV90eXBlIjoiY292ZXJhZ2VlbGlnaWJpbGl0eSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public Response validHealthResponse() {
        return new Response("healthy", true);
    }

    protected Map<String, Object> getAuditData(String action, String status) {
        Map<String, Object> obj = new HashMap();
        obj.put(Constants.EID, "AUDIT");
        obj.put(Constants.ERROR_DETAILS, new Object());
        obj.put(Constants.DEBUG_DETAILS, new Object());
        obj.put(Constants.HCX_RECIPIENT_CODE, "1-2799b6a4-cf2d-45fe-a5e1-5f1c82979e0d");
        obj.put(Constants.HCX_SENDER_CODE, "93f908ba");
        obj.put(Constants.API_CALL_ID, "26b1060c-1e83-4600-9612-ea31e0ca5091");
        obj.put(Constants.WORKFLOW_ID, "1e83-460a-4f0b-b016-c22d820674e1");
        obj.put(Constants.CORRELATION_ID, "5e934f90-111d-4f0b-b016-c22d820674e1");
        obj.put(Constants.TIMESTAMP, "2022-01-06T09:50:23+00");
        obj.put(Constants.REQUEST_TIME, "1642781095099");
        obj.put(Constants.AUDIT_TIMESTAMP, "1642781095099");
        obj.put(Constants.UPDATED_TIME, "1642781095099");
        obj.put(Constants.ACTION, action);
        obj.put(Constants.MID, "59cefda2-a4cc-4795-95f3-fb9e82e21cef");
        obj.put(Constants.STATUS, status);
        obj.put(Constants.SENDER_ROLE, Arrays.asList("provider"));
        obj.put(Constants.RECIPIENT_ROLE, Arrays.asList("payor"));
        obj.put(Constants.PAYLOAD, "test_payload");
        return obj;
    }

    public String getParticipantCreateBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "test user");
        obj.put("primary_mobile", "9493347239");
        obj.put("primary_email", "testuser@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("provider")));
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("landmark", "");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number", "4707890099809809");
            put("ifsc_code", "ICICLE");
        }});
        obj.put("signing_cert_path", "urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert", "-----BEGIN CERTIFICATE-----\nMIIC6jCCAdKgAwIBAgIGAX4A5QsDMA0GCSqGSIb3DQEBCwUAMDYxNDAyBgNVBAMM K0hpYmx5bXRVSXl2ZVU4cDVSODZZdzVsMVVYdjQ2SU1GYjByMjkyMENWdHcwHhcN MjExMjI4MTE1NTE3WhcNMjIxMDI0MTE1NTE3WjA2MTQwMgYDVQQDDCtIaWJseW10 VUl5dmVVOHA1Ujg2WXc1bDFVWHY0NklNRmIwcjI5MjBDVnR3MIIBIjANBgkqhkiG 9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhvlyz2Igsa2au9SF/sO7qAxsRiO6su12g2y9 wSRN947u643az+8LADlkbzPASMlrkWWYZfcRvru5f+zrGQMxqKiO6ft9sfq/SCVc 26Cw2o5OrafzT4NXLHO39jOQbuq5CxL6yi893YMt0PdvKzLA960pswS3pXyk6Pmg 17wjosNXTFrGWXZBKQkycR9/TW9iuEufZDv0dhrUlP0DC6uuZt+F3DGaQ7WrQNbt UvCXWTTXjAjvjbhSgcyH711AkPI5H+4etdvlD9QGIaMgjBac1GfW+5YkBfU2KV9T Mq/7U++VirkZZBZXAC1K7VbJC/CgNMOrANJ1+XzkVbLlXoVXgwIDAQABMA0GCSqG SIb3DQEBCwUAA4IBAQBnLAUhz56DZC28byQz0GS/GdgGMiobkxFvtHNCutb9cFOp PXc3mX4/69B8vfu0dncjLKiMOv/S+IzoUjqSiJpackA84oODZb7baBH/Ogqa9ZkY vxA2O1DsbANQrbfiBjKRIiCGCTzWCCD2vPJdjoJiActL3gbGaENKM6Ft0FO5D7sp kaFOBvIjfXvxvfFrS/BwivoKWESpD4ZmlcafQweGfSJVECRZ5Oc+T0lg+0S20NlP vmlozyceiVTXAqpPngK8Jc3VuDeG4xRLI9J0DwDV0rtUbPzdVBzAO9KgrnsxYzRQ 8LmmugWhh4L+QwIJe+1NWmULmPG+he+/lBiFD0Jg\n-----END CERTIFICATE-----");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantPayorBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "New Teja Hospital888");
        obj.put("primary_mobile", "9493347239");
        obj.put("primary_email", "dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("payor")));
        obj.put("scheme_code", "default");
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("landmark", "");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number", "4707890099809809");
            put("ifsc_code", "ICICLE");
        }});
        obj.put("signing_cert_path", "urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert", "urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantPayorSchemeBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "New Teja Hospital888");
        obj.put("primary_mobile", "9493347239");
        obj.put("primary_email", "dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("payor")));
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("landmark", "");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number", "4707890099809809");
            put("ifsc_code", "ICICLE");
        }});
        obj.put("signing_cert_path", "urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert", "urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantPayorSchemeNotAllowedBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "New Teja Hospital888");
        obj.put("primary_mobile", "9493347239");
        obj.put("primary_email", "dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("provider")));
        obj.put("scheme_code", "default");
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number", "4707890099809809");
            put("ifsc_code", "ICICLE");
        }});
        obj.put("signing_cert_path", "urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert", "urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantUrlNotAllowedBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "New Teja Hospital888");
        obj.put("primary_mobile", "9493347239");
        obj.put("primary_email", "dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("provider")));
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("landmark", "");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number", "4707890099809809");
            put("ifsc_code", "ICICLE");
        }});
        obj.put("signing_cert_path", "urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert", "urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantUpdateBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "New Teja Hospital888");
        obj.put("primary_mobile", "9493347232");
        obj.put("primary_email", "dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("provider")));
        obj.put("participant_code", "1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("landmark", "");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-38765899")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080");
        obj.put("encryption_cert", "-----BEGIN CERTIFICATE-----\nMIIC6jCCAdKgAwIBAgIGAX4A5QsDMA0GCSqGSIb3DQEBCwUAMDYxNDAyBgNVBAMM K0hpYmx5bXRVSXl2ZVU4cDVSODZZdzVsMVVYdjQ2SU1GYjByMjkyMENWdHcwHhcN MjExMjI4MTE1NTE3WhcNMjIxMDI0MTE1NTE3WjA2MTQwMgYDVQQDDCtIaWJseW10 VUl5dmVVOHA1Ujg2WXc1bDFVWHY0NklNRmIwcjI5MjBDVnR3MIIBIjANBgkqhkiG 9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhvlyz2Igsa2au9SF/sO7qAxsRiO6su12g2y9 wSRN947u643az+8LADlkbzPASMlrkWWYZfcRvru5f+zrGQMxqKiO6ft9sfq/SCVc 26Cw2o5OrafzT4NXLHO39jOQbuq5CxL6yi893YMt0PdvKzLA960pswS3pXyk6Pmg 17wjosNXTFrGWXZBKQkycR9/TW9iuEufZDv0dhrUlP0DC6uuZt+F3DGaQ7WrQNbt UvCXWTTXjAjvjbhSgcyH711AkPI5H+4etdvlD9QGIaMgjBac1GfW+5YkBfU2KV9T Mq/7U++VirkZZBZXAC1K7VbJC/CgNMOrANJ1+XzkVbLlXoVXgwIDAQABMA0GCSqG SIb3DQEBCwUAA4IBAQBnLAUhz56DZC28byQz0GS/GdgGMiobkxFvtHNCutb9cFOp PXc3mX4/69B8vfu0dncjLKiMOv/S+IzoUjqSiJpackA84oODZb7baBH/Ogqa9ZkY vxA2O1DsbANQrbfiBjKRIiCGCTzWCCD2vPJdjoJiActL3gbGaENKM6Ft0FO5D7sp kaFOBvIjfXvxvfFrS/BwivoKWESpD4ZmlcafQweGfSJVECRZ5Oc+T0lg+0S20NlP vmlozyceiVTXAqpPngK8Jc3VuDeG4xRLI9J0DwDV0rtUbPzdVBzAO9KgrnsxYzRQ 8LmmugWhh4L+QwIJe+1NWmULmPG+he+/lBiFD0Jg\n-----END CERTIFICATE-----");
        return JSONUtils.serialize(obj);
    }

    public String getEmptyBody() {
        return "{}";
    }

    public String getSearchFilter() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("filters", new HashMap<>() {{
            put("primary_email", new HashMap<>() {{
                put("eq", "dharmateja888@gmail.com");
            }});
        }});
        return JSONUtils.serialize(obj);
    }

    public String getSearchNotFoundFilter() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("filters", new HashMap<>() {{
            put("participant_code", new HashMap<>() {{
                put("eq", "1-d2d56996-1b77-4abb-b9e9-0e6e7343c72");
            }});
        }});
        return JSONUtils.serialize(obj);
    }

    public Map<String, Object> getParticipantCreateAuditLog() throws Exception {
        return JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"edata\":{\"prevStatus\":\"\",\"status\":\"Created\"},\"ets\":1659434908868,\"mid\":\"5ee2b9e1-ded6-4b56-afa8-3380107632e0\",\"object\":{\"id\":\"097e0185-eeb1-48f1-b2b0-b68774d02c6d\",\"type\":\"participant\"},\"cdata\":{\"action\":\"/participant/create\"}}", Map.class);
    }

    public Map<String, Object> getParticipantDeleteAuditLog() throws Exception {
        return JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"edata\":{\"prevStatus\":\"Created\",\"status\":\"Inactive\"},\"ets\":1659434908868,\"mid\":\"5ee2b9e1-ded6-4b56-afa8-3380107632e0\",\"object\":{\"id\":\"097e0185-eeb1-48f1-b2b0-b68774d02c6d\",\"type\":\"participant\"},\"cdata\":{\"action\":\"/participant/delete\"}}", Map.class);
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/jwe-helper/main/src/test/resources/x509-self-signed-certificate.pem");
    }

    public String getAuthorizationHeader() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMYU9HdVRrYVpsVEtzaERwUng1R25JaXUwV1A1S3VGUUoyb29WMEZnWGx3In0.eyJleHAiOjE2NDcwNzgwNjksImlhdCI6MTY0Njk5MTY2OSwianRpIjoiNDcyYzkwOTAtZWQ4YS00MDYxLTg5NDQtMzk4MjhmYzBjM2I4IiwiaXNzIjoiaHR0cDovL2E5ZGQ2M2RlOTFlZTk0ZDU5ODQ3YTEyMjVkYThiMTExLTI3Mzk1NDEzMC5hcC1zb3V0aC0xLmVsYi5hbWF6b25hd3MuY29tOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhlYWx0aC1jbGFpbS1leGNoYW5nZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIwYzU3NjNkZS03MzJkLTRmZDQtODU0Ny1iMzk2MGMxMzIwZjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiIxMThhMTRmMS04OTAxLTQxZTMtYWE5Zi1iNWFjMjYzNjkzMzIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwczovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cHM6Ly9uZGVhci54aXYuaW4iLCJodHRwOi8vbG9jYWxob3N0OjQyMDAiLCJodHRwOi8vbmRlYXIueGl2LmluIiwiaHR0cDovLzIwLjE5OC42NC4xMjgiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkhJRS9ISU8uSENYIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeCBhZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3ggYWRtaW4ifQ.SwDJNGkHOs7MrArqwdkArLkRDgPIU3SHwMdrppmG2JHQkpYRLqFpfmFPgIYNAyi_b_ZQnXKwuhT6ABNEV2-viJWTPLYe4z5JkeUGNurnrkSoMMObrd0s1tLYjdgu5j5kLaeUBeSeULTkdBfAM9KZX5Gn6Ri6AKs6uFq22hJOmhtw3RTyX-7kozG-SzSfIyN_-7mvJBZjBR73gaNJyEms4-aKULAnQ6pYkj4hzzlac2WCucq2zZnipeupBOJzx5z27MLdMs8lfNRTTqkQVhoUK0DhDxyj9N_TzbycPdykajhOrerKfpEnYcZpWfC-bJJSDagnP9D407OqoxoE3_niHw";
    }

    public String getUserToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE2ODY1Njc5NzcsImlhdCI6MTY4NDgzOTk3NywianRpIjoiZWY3NzMwNGMtZWQ2NC00NDBkLWFmZmUtNDJiNDM2ZTkzNDUwIiwiaXNzIjoiaHR0cDovL2Rldi1oY3guc3dhc3RoLmFwcC9hdXRoL3JlYWxtcy9zd2FzdGgtaGVhbHRoLWNsYWltLWV4Y2hhbmdlIiwic3ViIjoiODUyNzg1M2MtYjQ0Mi00NGRiLWFlZGEtZGJiZGNmNDcyZDliIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiNTMzOTEyYjUtMjRhYS00MzMyLTg3ZGEtYTM3ODRiNDI4ZWJkIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW5kZWFyIl19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3Rwcm92aWRlcjFAYXBvbGxvLmNvbSIsImVudGl0eSI6WyJPcmdhbmlzYXRpb24iXSwiZW1haWwiOiJ0ZXN0cHJvdmlkZXIxQGFwb2xsby5jb20ifQ.SMrKfkWeKjzfep9giWpBOmMwokj3owjPYU6UvKEznODmf5ewgYB55bEIkVWoK171WUeGkrXulLD8szvAUSRuIkIAAVs9YwPK6PUtpc0xZ-0j6prt5FctF5v5oS5tQNJnkHsR9_iw_cgob-4fOO2azGg25_l13jfExfjd2o1JHa6fBsc6wWUV98NQRK6dkvDzG7TETkH1Z47WxndYuY32as54m7wTG4_kb1RF2UR9UomYUn2Uze5tSsE2jSZ9MSo--KeN4u6yuLRbFw9M7vvIaE-530jPP0bAxhmhTsvrZMSk25hsiAmDEWJgeI7Ul3P27RN6Yeb_ek4KViJbd6MlFw";
    }

    public Map<String, Object> getHIUParticipant() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("participant_name", "New Teja Hospital888");
        obj.put("primary_mobile", "9493347239");
        obj.put("primary_email", "dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("member.isnp")));
        obj.put("scheme_code", "default");
        obj.put("address", new HashMap<>() {{
            put("plot", "5-4-199");
            put("street", "road no 12");
            put("landmark", "");
            put("village", "Nampally");
            put("district", "Hyd");
            put("state", "Telangana");
            put("pincode", "500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status", "Created");
        obj.put("endpoint_url", "http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number", "4707890099809809");
            put("ifsc_code", "ICICLE");
        }});
        obj.put("signing_cert_path", "urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert", "urn:isbn:0-4234");
        return obj;
    }

    public String getUserCreateBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("email","test-user-89@gmail.com");
        obj.put("user_name", "user-01");
        obj.put("created_by", "pcpt@hcx");
        obj.put("pin", "1234");
        obj.put("linked_user_id", "1234");
        return JSONUtils.serialize(obj);
    }
    public String getUserCreateBodyWithMobile() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("mobile", "9632492134");
        obj.put("user_name", "user-01");
        obj.put("created_by", "pcpt@hcx");
        obj.put("pin", "1234");
        obj.put("linked_user_id", "1234");
        return JSONUtils.serialize(obj);
    }
    public String getEmailPhoneNotfound() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("user_name", "user-01");
        obj.put("created_by", "pcpt@hcx");
        obj.put("pin", "1234");
        obj.put("linked_user_id", "1234");
        return JSONUtils.serialize(obj);
    }
    public Map<String, Object> getUserCreateAuditLog() throws Exception {
        return JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"edata\":{\"prevStatus\":\"\",\"status\":\"Created\"},\"ets\":1659434908868,\"mid\":\"5ee2b9e1-ded6-4b56-afa8-3380107632e0\",\"object\":{\"id\":\"097e0185-eeb1-48f1-b2b0-b68774d02c6d\",\"type\":\"User\"},\"cdata\":{\"action\":\"/user/create\"}}", Map.class);
    }

    public Map<String, Object> getUserUpdateAuditLog() throws Exception {
        return JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"edata\":{\"prevStatus\":\"\",\"status\":\"Created\"},\"ets\":1659434908868,\"mid\":\"5ee2b9e1-ded6-4b56-afa8-3380107632e0\",\"object\":{\"id\":\"097e0185-eeb1-48f1-b2b0-b68774d02c6d\",\"type\":\"User\"},\"cdata\":{\"action\":\"/user/update\"}}", Map.class);
    }
    public Map<String, Object> getUserDeleteAuditLog() throws Exception {
        return JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"edata\":{\"prevStatus\":\"Created\",\"status\":\"Inactive\"},\"ets\":1659434908868,\"mid\":\"5ee2b9e1-ded6-4b56-afa8-3380107632e0\",\"object\":{\"id\":\"097e0185-eeb1-48f1-b2b0-b68774d02c6d\",\"type\":\"User\"},\"cdata\":{\"action\":\"/user/delete\"}}", Map.class);
    }

    public String getUserSearchFilter() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("filters", new HashMap<>() {{
            put("email", new HashMap<>() {{
                put("eq", "test-user-89@gmail.com");
            }});
        }});
        return JSONUtils.serialize(obj);
    }

    public String getUserSearchNotfoundFilter() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("filters", new HashMap<>() {{
            put("email", new HashMap<>() {{
                put("eq", "test-user");
            }});
        }});
        return JSONUtils.serialize(obj);
    }

    public String getUserUpdateBody() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put("user_id", "test-user-89.gmail@swasth-hcx");
        obj.put("user_name", "user-01");
        obj.put("created_by", "pcpt@hcx");
        obj.put("pin", "1234");
        obj.put("linked_user_id", "1234");
        return JSONUtils.serialize(obj);
    }

}