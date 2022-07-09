package org.swasth.hcx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.common.dto.Response;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.v1.*;
import org.swasth.hcx.handlers.EventHandler;
import org.swasth.hcx.managers.HealthCheckManager;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.hcx.service.NotificationService;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;
import org.swasth.redis.cache.RedisCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



@WebMvcTest({CoverageEligibilityController.class, PreAuthController.class, ClaimsController.class, PaymentsController.class, AuditController.class, StatusController.class, SearchController.class, CommunicationController.class, PredeterminationController.class, ParticipantController.class, NotificationController.class, NotificationService.class, EventHandler.class})
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class BaseSpec {

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @MockBean
    protected EventGenerator mockEventGenerator;

    @Mock
    protected Environment mockEnv;

    @MockBean
    protected IEventService mockKafkaClient;

    @MockBean
    protected IDatabaseService postgreSQLClient;

    @MockBean
    protected HealthCheckManager healthCheckManager;

    @MockBean
    protected HeaderAuditService headerAuditService;

    @MockBean
    protected AuditIndexer auditIndexer;

    @MockBean
    protected RedisCache redisCache;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected EventHandler eventHandler;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    public String getResponseErrorMessage(Map<String,Object> responseBody){
        return (String) ((Map<String,Object>) responseBody.get("error")).get("message");
    }

    public String getResponseErrorCode(Map<String,Object> responseBody){
        return (String) ((Map<String,Object>) responseBody.get("error")).get("code");
    }

    public String getRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS05ODc1Ni1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJyZXF1ZXN0X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MTAxIn0sCiJqd3NfaGVhZGVyIjp7InR5cCI6IkpXVCIsICJhbGciOiJSUzI1NiJ9LAoiandlX2hlYWRlciI6eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getErrorRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-status","response.error");
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-workflow_id","1e83-460a-4f0b-b016-c22d820674e1");
        obj.put("x-hcx-error_details", new HashMap<>() {{
            put("code","ERR_INVALID_ENCRYPTION");
            put("error","");
            put("trace","Recipient Invalid Encryption");
        }});
        return JSONUtils.serialize(obj);
    }

    public String getBadRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoieC1oY3gtc3RhdHVzX2ZpbHRlcnMiOnsicmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTEwMSJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getExceptionRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoxMjMsCiJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjEtN2JhMDdlMzEtY2ViYi00NzUxLWIyYjctZmUwNTBkOWQyYzAwIiwKIngtaGN4LWNvcnJlbGF0aW9uX2lkIjoiODU0ZmU0MWItMjEyZi00YTU1LWJlMmYtMTBiZGE4ZGFkYzk1IiwKIngtaGN4LXRpbWVzdGFtcCI6IjIwMjItMDUtMTJUMTU6MjY6MTkuNjI3KzA1MzAiLAoieC1oY3gtYXBpX2NhbGxfaWQiOiJhYTFlM2Y5Yi05MGE3LTRlZDktOTgyMS0wMzA2ZjFiY2I3NDYiCn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public String getHeadersMissingRequestBody() throws JsonProcessingException {
      Map<String,Object> obj = new HashMap<>();
      obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtcmVxdWVzdF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1kZWJ1Z19mbGFnIjoiSW5mbyIsCiJ4LWhjeC1lcnJvcl9kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCAidHJhY2UiOiAiIn0sCiJ4LWhjeC1kZWJ1Z19kZXRhaWxzIjp7ImVycm9yLmNvZGUiOiAiYmFkLmlucHV0IiwgImVycm9yLm1lc3NhZ2UiOiAiUHJvdmlkZXIgY29kZSBub3QgZm91bmQiLCJ0cmFjZSI6IiJ9LAoiandzX2hlYWRlciI6eyJ0eXAiOiJKV1QiLCAiYWxnIjoiUlMyNTYifSwKImp3ZV9oZWFkZXIiOnsiYWxnIjoiUlNBLU9BRVAiLCJlbmMiOiJBMjU2R0NNIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
      return JSONUtils.serialize(obj);
    }

    protected String getStatusRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiOTNmOTA4YmEiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLTI3OTliNmE0LWNmMmQtNDVmZS1hNWUxLTVmMWM4Mjk3OWUwZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MSIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIyLTAxLTA2VDA5OjUwOjIzKzAwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QuaW5pdGlhdGUiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIxZTgzLTQ2MGEtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC1zdGF0dXNfZmlsdGVycyI6eyJhcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTEwMSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    protected String getOnStatusRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiIiwKIngtaGN4LXJlY2lwaWVudF9jb2RlIjoiMS0yNzk5YjZhNC1jZjJkLTQ1ZmUtYTVlMS01ZjFjODI5NzllMGQiLAoieC1oY3gtYXBpX2NhbGxfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTEiLAoieC1oY3gtY29ycmVsYXRpb25faWQiOiI1ZTkzNGY5MC0xMTFkLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtdGltZXN0YW1wIjoiMjAyMi0wMS0wNlQwOTo1MDoyMyswMCIsCiJ4LWhjeC1zdGF0dXMiOiJyZXF1ZXN0LmluaXRpYXRlIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMWU4My00NjBhLTRmMGItYjAxNi1jMjJkODIwNjc0ZTEiLAoieC1oY3gtc3RhdHVzX3Jlc3BvbnNlIjp7ImVudGl0eV90eXBlIjoiY292ZXJhZ2VlbGlnaWJpbGl0eSJ9Cn0=.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.AxY8DCtDaGlsbGljb3RoZQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
        return JSONUtils.serialize(obj);
    }

    public Response validHealthResponse() {
        return new Response("healthy",true);
    }

    public String getParticipantCreateBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("participant_name","test user");
        obj.put("primary_mobile","9493347239");
        obj.put("primary_email","testuser@gmail.com");
        obj.put("roles",new ArrayList<>(Collections.singleton("provider")));
        obj.put("address", new HashMap<>() {{
            put("plot","5-4-199");
            put("street","road no 12");
            put("landmark","");
            put("village","Nampally");
            put("district","Hyd");
            put("state","Telangana");
            put("pincode","500805");
        }});
        obj.put("phone",new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status","Created");
        obj.put("endpoint_url","http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number","4707890099809809");
            put("ifsc_code","ICICLE");
        }});
        obj.put("signing_cert_path","urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes",new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert","urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantPayorBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("participant_name","New Teja Hospital888");
        obj.put("primary_mobile","9493347239");
        obj.put("primary_email","dharmateja888@gmail.com");
        obj.put("roles",new ArrayList<>(Collections.singleton("payor")));
        obj.put("scheme_code","default");
        obj.put("address", new HashMap<>() {{
            put("plot","5-4-199");
            put("street","road no 12");
            put("landmark","");
            put("village","Nampally");
            put("district","Hyd");
            put("state","Telangana");
            put("pincode","500805");
        }});
        obj.put("phone",new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status","Created");
        obj.put("endpoint_url","http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number","4707890099809809");
            put("ifsc_code","ICICLE");
        }});
        obj.put("signing_cert_path","urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes",new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert","urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantPayorSchemeBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("participant_name","New Teja Hospital888");
        obj.put("primary_mobile","9493347239");
        obj.put("primary_email","dharmateja888@gmail.com");
        obj.put("roles",new ArrayList<>(Collections.singleton("payor")));
        obj.put("address", new HashMap<>() {{
            put("plot","5-4-199");
            put("street","road no 12");
            put("landmark","");
            put("village","Nampally");
            put("district","Hyd");
            put("state","Telangana");
            put("pincode","500805");
        }});
        obj.put("phone",new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status","Created");
        obj.put("endpoint_url","http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number","4707890099809809");
            put("ifsc_code","ICICLE");
        }});
        obj.put("signing_cert_path","urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes",new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert","urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantPayorSchemeNotAllowedBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("participant_name","New Teja Hospital888");
        obj.put("primary_mobile","9493347239");
        obj.put("primary_email","dharmateja888@gmail.com");
        obj.put("roles",new ArrayList<>(Collections.singleton("provider")));
        obj.put("scheme_code","default");
        obj.put("address", new HashMap<>() {{
            put("plot","5-4-199");
            put("street","road no 12");
            put("village","Nampally");
            put("district","Hyd");
            put("state","Telangana");
            put("pincode","500805");
        }});
        obj.put("phone",new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status","Created");
        obj.put("endpoint_url","http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number","4707890099809809");
            put("ifsc_code","ICICLE");
        }});
        obj.put("signing_cert_path","urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes",new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert","urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantUrlNotAllowedBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("participant_name","New Teja Hospital888");
        obj.put("primary_mobile","9493347239");
        obj.put("primary_email","dharmateja888@gmail.com");
        obj.put("roles",new ArrayList<>(Collections.singleton("provider")));
        obj.put("address", new HashMap<>() {{
            put("plot","5-4-199");
            put("street","road no 12");
            put("landmark","");
            put("village","Nampally");
            put("district","Hyd");
            put("state","Telangana");
            put("pincode","500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-387658992")));
        obj.put("status","Created");
        obj.put("endpoint_url","http://localhost:8095");
        obj.put("payment_details", new HashMap<>() {{
            put("account_number","4707890099809809");
            put("ifsc_code","ICICLE");
        }});
        obj.put("signing_cert_path","urn:isbn:0-476-27557-4");
        obj.put("linked_registry_codes", new ArrayList<>(Collections.singleton("22344")));
        obj.put("encryption_cert","urn:isbn:0-4234");
        return JSONUtils.serialize(obj);
    }

    public String getParticipantUpdateBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("participant_name","New Teja Hospital888");
        obj.put("primary_mobile","9493347232");
        obj.put("primary_email","dharmateja888@gmail.com");
        obj.put("roles", new ArrayList<>(Collections.singleton("provider")));
        obj.put("participant_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("address", new HashMap<>() {{
            put("plot","5-4-199");
            put("street","road no 12");
            put("landmark","");
            put("village","Nampally");
            put("district","Hyd");
            put("state","Telangana");
            put("pincode","500805");
        }});
        obj.put("phone", new ArrayList<>(Collections.singleton("040-38765899")));
        obj.put("status","Created");
        obj.put("endpoint_url","http://a4a175528daf949a2af3cd141af93de2-1466580421.ap-south-1.elb.amazonaws.com:8080");
        return JSONUtils.serialize(obj);
    }

    public String getSearchFilter() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("filters", new HashMap<>() {{
            put("primary_email",new HashMap<>() {{
                put("eq","dharmateja888@gmail.com");
            }});
        }});
        return JSONUtils.serialize(obj);
    }

    public String getSearchNotFoundFilter() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("filters", new HashMap<>() {{
            put("participant_code",new HashMap<>() {{
                put("eq","1-d2d56996-1b77-4abb-b9e9-0e6e7343c72");
            }});
        }});
        return JSONUtils.serialize(obj);
    }

    public String getAuthorizationHeader() throws JsonProcessingException{
        String s = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMYU9HdVRrYVpsVEtzaERwUng1R25JaXUwV1A1S3VGUUoyb29WMEZnWGx3In0.eyJleHAiOjE2NDcwNzgwNjksImlhdCI6MTY0Njk5MTY2OSwianRpIjoiNDcyYzkwOTAtZWQ4YS00MDYxLTg5NDQtMzk4MjhmYzBjM2I4IiwiaXNzIjoiaHR0cDovL2E5ZGQ2M2RlOTFlZTk0ZDU5ODQ3YTEyMjVkYThiMTExLTI3Mzk1NDEzMC5hcC1zb3V0aC0xLmVsYi5hbWF6b25hd3MuY29tOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhlYWx0aC1jbGFpbS1leGNoYW5nZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIwYzU3NjNkZS03MzJkLTRmZDQtODU0Ny1iMzk2MGMxMzIwZjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiIxMThhMTRmMS04OTAxLTQxZTMtYWE5Zi1iNWFjMjYzNjkzMzIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwczovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cHM6Ly9uZGVhci54aXYuaW4iLCJodHRwOi8vbG9jYWxob3N0OjQyMDAiLCJodHRwOi8vbmRlYXIueGl2LmluIiwiaHR0cDovLzIwLjE5OC42NC4xMjgiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkhJRS9ISU8uSENYIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6ImhjeCBhZG1pbiIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeC1hZG1pbiIsImdpdmVuX25hbWUiOiJoY3ggYWRtaW4ifQ.SwDJNGkHOs7MrArqwdkArLkRDgPIU3SHwMdrppmG2JHQkpYRLqFpfmFPgIYNAyi_b_ZQnXKwuhT6ABNEV2-viJWTPLYe4z5JkeUGNurnrkSoMMObrd0s1tLYjdgu5j5kLaeUBeSeULTkdBfAM9KZX5Gn6Ri6AKs6uFq22hJOmhtw3RTyX-7kozG-SzSfIyN_-7mvJBZjBR73gaNJyEms4-aKULAnQ6pYkj4hzzlac2WCucq2zZnipeupBOJzx5z27MLdMs8lfNRTTqkQVhoUK0DhDxyj9N_TzbycPdykajhOrerKfpEnYcZpWfC-bJJSDagnP9D407OqoxoE3_niHw";
        return s;
    }

    public String getAuthorizationUpdateHeader() throws JsonProcessingException{
        String s = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE2NDcyNDM3NjgsImlhdCI6MTY0NjM3OTc2OCwianRpIjoiYTNhNjNlYmEtNmQ3MS00M2RhLWEwODEtMjVlN2Q2MjY2NDU4IiwiaXNzIjoiaHR0cDovL2FlZjgxMDFjNDMyZDA0YTY1OWU2MzE3YjNlNTAzMWNmLTE2NzQ1ODYwNjguYXAtc291dGgtMS5lbGIuYW1hem9uYXdzLmNvbTo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNjU4NWEwZWItNTY4ZS00MzBmLTkxMzgtNzg1ODUyZjg2NGM1IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiYzhiOWFiN2YtMDNhMS00MWQwLTlmYzItNmY2ZDI3OTUxMzg1IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJwYXlvciIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImRoYXJtYXRlamE4ODdAZ21haWwuY29tIiwiZW50aXR5IjpbIk9yZ2FuaXNhdGlvbiJdLCJlbWFpbCI6ImRoYXJtYXRlamE4ODdAZ21haWwuY29tIn0.Ujl6c0x5JXffoIdKt_JxoQgu5pItD8ZhohcEkLglfmuxSzhfJe41iDawEtUJgCgnx6W2tu5ZxcMg-O-SoFSO8f9AUT5BqRqATya4f1rKqXPZXZVzlpipSDvPY_wLgtSxiOXDISyeTP22Le1hjulrAZWlAUJ2RCFlk1n4q4GdAZjnKFQReGVWv3tH4Vsh13fKXkE2rpVmHlsIgYz6v-AGJ857jla5u_BlN1fC9AlAjB6VjwWwLSN9c9ktxGPBsq5snw7UzAjdpCqiu0MU1ed2-4niKtmYqYpN9Sjv7mq3YeVW-npG-uz2-hznCpjU4qF45csJhtmLVsFYTLccI5WX6Q";
        return s;
    }
}
