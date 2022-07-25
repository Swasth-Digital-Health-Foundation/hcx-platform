package org.swasth.apigateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.swasth.apigateway.config.GenericConfiguration;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.apigateway.utils.JSONUtils;
import org.swasth.auditindexer.function.AuditIndexer;
import org.swasth.redis.cache.RedisCache;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(GenericConfiguration.class)
public class BaseSpec {

    protected MockWebServer server =  new MockWebServer();

    public final String versionPrefix = "/v0.7";

    @MockBean
    protected RegistryService registryService;

    @MockBean
    protected AuditService auditService;

    @MockBean
    protected RedisCache redisCache;

    @MockBean
    protected AuditIndexer auditIndexer;

    @LocalServerPort
    protected int port;

    protected WebTestClient client;

    @BeforeEach
    public void setup() throws IOException {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofSeconds(30)).build();
        server.start(8080);
    }

    @AfterEach
    public void teardown() throws IOException {
        server.shutdown();
    }

    protected String getResponseErrorCode(EntityExchangeResult<Map> result){
        return (String) ((Map<String,Object>) result.getResponseBody().get("error")).get("code");
    }

    protected String getResponseErrorMessage(EntityExchangeResult<Map> result){
        return (String) ((Map<String,Object>) result.getResponseBody().get("error")).get("message");
    }

    protected Map<String,Object> getRequestBody() {
        return Collections.singletonMap("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0IiwKIngtaGN4LWRlYnVnX2ZsYWciOiJJbmZvIiwKIngtaGN4LWRlYnVnX2RldGFpbHMiOnsiY29kZSI6IkVSUl9JTlZBTElEX0VOQ1JZUFRJT04iLCJtZXNzYWdlIjoiUmVjaXBpZW50IEludmFsaWQgRW5jcnlwdGlvbiIsInRyYWNlIjoiIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
    }

    protected Map<String,Object> getOnRequestBody(){
        return Collections.singletonMap("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlc3BvbnNlLnBhcnRpYWwiLAoieC1oY3gtd29ya2Zsb3dfaWQiOiIyNmIxMDYwYy0xZTgzLTQ2MDAtOTYxMi1lYTMxZTBjYTUwOTQiLAoieC1oY3gtZGVidWdfZmxhZyI6IkluZm8iLAoieC1oY3gtZGVidWdfZGV0YWlscyI6eyJjb2RlIjoiRVJSX0lOVkFMSURfRU5DUllQVElPTiIsIm1lc3NhZ2UiOiJSZWNpcGllbnQgSW52YWxpZCBFbmNyeXB0aW9uIiwidHJhY2UiOiIifQp9.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
    }

    protected Map<String,Object> getAuditRequestBody() {
        Map<String,Object> obj = new HashMap<>();
        obj.put("filters",new HashMap<String,Object>(){{
            put("x-hcx-correlation_id", "5e934f90-111d-4f0b-b016-c22d820674e1");
        }});
        return obj;
    }

    public String getErrorRequestBody() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put("x-hcx-status","response.error");
        obj.put("x-hcx-sender_code","1-ce23ccdc-e645-4e35-97b8-0bd8fef43ecd");
        obj.put("x-hcx-recipient_code","1-68c5deca-8299-4feb-b441-923bb649a9a3");
        obj.put("x-hcx-api_call_id","26b1060c-1e83-4600-9612-ea31e0ca5098");
        obj.put("x-hcx-correlation_id","5e934f90-111d-4f0b-b016-c22d820674e4");
        obj.put("x-hcx-timestamp","2021-10-27T20:35:52.636+0530");
        obj.put("x-hcx-error_details", new HashMap<String,Object>() {{
            put("code","ERR_INVALID_ENCRYPTION");
            put("message","Recipient Invalid Encryption");
            put("trace","");
        }});
        return JSONUtils.serialize(obj);
    }

    protected String getNotificationRequest(String topicCode, List<String> recipientRoles, List<String> recipientCodes, List<String> subscriptions) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, topicCode);
        obj.put(RECIPIENT_ROLES, recipientRoles);
        obj.put(RECIPIENT_CODES, recipientCodes);
        obj.put(SUBSCRIPTIONS, subscriptions);
        Map<String,Object> notificationData = new HashMap<>();
        notificationData.put("message","Payor system down for sometime");
        notificationData.put("duration","2hrs");
        notificationData.put("startTime","9PM");
        notificationData.put("date","26th April 2022 IST");
        obj.put(NOTIFICATION_DATA,notificationData);
        return JSONUtils.serialize(obj);
    }

    protected String getInvalidNotificationRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE, "be0e578d-b391-42f9-96f7-1e6bacd91c20");
        obj.put(RECIPIENT_ROLES, Arrays.asList("provider","payor"));
        obj.put(RECIPIENT_CODES, Arrays.asList("test-user@hcx"));
        obj.put(SUBSCRIPTIONS, Collections.EMPTY_LIST);
        obj.put("invalidProperty", "test-123");
        return JSONUtils.serialize(obj);
    }

    protected Map<String,Object> getProviderDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_code\":\"f7c0e759-bec3-431b-8c4f-6b294d103a74\",\"participant_name\": \"New Teja Hospital888\", \"primary_mobile\": \"9493347239\", \"primary_email\": \"dharmateja888@gmail.com\", \"roles\": [ \"provider\" ], \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"f901ba37-e09f-4d84-a75f-5e203f8ad4da\", \"@type\": \"address\", \"locality\": \"Nampally\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"https://677e6fd9-57cc-466c-80f6-ae0462762872.mock.pstmn.io\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"da192c1e-5ad4-47bc-b425-de4f7bbc9bd0\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f7c0e759-bec3-431b-8c4f-6b294d103a74\" ], \"osid\": \"68c5deca-8299-4feb-b441-923bb649a9a3\", \"@type\": \"Organisation\", \"payment\": { \"ifsc_code\": \"ICICI\", \"account_number\": \"4707890099809809\", \"@type\": \"payment_details\", \"osid\": \"3a3bd68a-848a-4d52-9ec2-07a92d765fb4\" } }", Map.class);
    }

    protected Map<String,Object> getBlockedProviderDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New Teja Hospital888\", \"primary_mobile\": \"9493347239\", \"primary_email\": \"dharmateja888@gmail.com\", \"roles\": [ \"provider\" ], \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"f901ba37-e09f-4d84-a75f-5e203f8ad4da\", \"@type\": \"address\", \"locality\": \"Nampally\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Blocked\", \"endpoint_url\": \"https://677e6fd9-57cc-466c-80f6-ae0462762872.mock.pstmn.io\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"da192c1e-5ad4-47bc-b425-de4f7bbc9bd0\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f7c0e759-bec3-431b-8c4f-6b294d103a74\" ], \"osid\": \"68c5deca-8299-4feb-b441-923bb649a9a3\", \"@type\": \"Organisation\", \"payment\": { \"ifsc_code\": \"ICICI\", \"account_number\": \"4707890099809809\", \"@type\": \"payment_details\", \"osid\": \"3a3bd68a-848a-4d52-9ec2-07a92d765fb4\" } }", Map.class);
    }

    protected Map<String,Object> getPayorDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_code\": \"new-payor-3\", \"participant_name\": \"New payor 3\", \"primary_mobile\": \"9493347003\", \"primary_email\": \"newpayor003@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"929d9a60-1fe3-49a5-bae7-4b49970cebbb\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"68a27687-b8c8-4271-97a1-0af3f53c3f3c\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"20bd4228-a87f-4175-a30a-20fb28983afb\" ], \"osid\": \"ce23ccdc-e645-4e35-97b8-0bd8fef43ecd\" }", Map.class);
    }

    protected Map<String,Object> getPayor2Details() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 2\", \"primary_mobile\": \"9493347002\", \"primary_email\": \"newpayor002@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"513aa2ef-c4d4-4202-9926-816a6d22ab8f\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"2b9fd445-053e-4f44-8ceb-bb51e3f48a86\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"a97ae3c3-bf32-438f-a63e-5b896393163f\" ], \"osid\": \"8584ba69-6c50-4535-8ad5-c02b8c3180a6\" }", Map.class);
    }

    protected Map<String,Object> getBlockedPayorDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 3\", \"primary_mobile\": \"9493347003\", \"primary_email\": \"newpayor003@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"929d9a60-1fe3-49a5-bae7-4b49970cebbb\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Blocked\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"68a27687-b8c8-4271-97a1-0af3f53c3f3c\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"20bd4228-a87f-4175-a30a-20fb28983afb\" ], \"osid\": \"ce23ccdc-e645-4e35-97b8-0bd8fef43ecd\" }", Map.class);
    }

    protected Map<String,Object> getInactivePayorDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 3\", \"primary_mobile\": \"9493347003\", \"primary_email\": \"newpayor003@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"929d9a60-1fe3-49a5-bae7-4b49970cebbb\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Inactive\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"68a27687-b8c8-4271-97a1-0af3f53c3f3c\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"20bd4228-a87f-4175-a30a-20fb28983afb\" ], \"osid\": \"ce23ccdc-e645-4e35-97b8-0bd8fef43ecd\" }", Map.class);
    }

    protected Map<String,Object> getNotallowedPayorDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 3\", \"primary_mobile\": \"9493347003\", \"primary_email\": \"newpayor003@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"929d9a60-1fe3-49a5-bae7-4b49970cebbb\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"68a27687-b8c8-4271-97a1-0af3f53c3f3c\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"20bd4228-a87f-4175-a30a-20fb28983afb\" ], \"osid\": \"ce23ccdc-e645-4e35-97b8-0bd8fef43ecd\" }", Map.class);
    }


    protected Map<String,Object> getHCXAdminDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_code\": \"1-d2d56996-1b77-4abb-b9e9-0e6e7343c72e\", \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"osid\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }", Map.class);
    }

    protected List<Map<String,Object>> getAuditLogs() throws Exception {
        return Arrays.asList(JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"x-hcx-error_details\":{},\"x-hcx-recipient_code\":\"1-2ff1f493-c4d4-4fc7-8d41-aaabb997af23\",\"x-hcx-debug_details\":{},\"auditTimeStamp\":1646736378272,\"mid\":\"54af42a9-3905-4f15-8c9d-94079594b6a6\",\"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e1\",\"updatedTimestamp\":1646736375686,\"x-hcx-status\":\"request.queued\",\"recipientRole\":[\"payor\"],\"x-hcx-timestamp\":\"2021-10-27T20:35:52.636+0530\",\"requestTimeStamp\":1646735834459,\"x-hcx-sender_code\":\"1-3a3bd68a-848a-4d52-9ec2-07a92d765fb4\",\"action\":\"/v1/coverageeligibility/check\",\"x-hcx-workflow_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5094\",\"x-hcx-api_call_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5093\",\"senderRole\":[\"provider\"]}", Map.class));
    }

    protected List<Map<String,Object>> getOnActionAuditLogs() throws Exception {
        return Arrays.asList(JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"x-hcx-error_details\":{},\"x-hcx-recipient_code\":\"1-2ff1f493-c4d4-4fc7-8d41-aaabb997af23\",\"x-hcx-debug_details\":{},\"auditTimeStamp\":1646736378272,\"mid\":\"54af42a9-3905-4f15-8c9d-94079594b6a6\",\"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e1\",\"updatedTimestamp\":1646736375686,\"x-hcx-status\":\"request.queued\",\"recipientRole\":[\"payor\"],\"x-hcx-timestamp\":\"2021-10-27T20:35:52.636+0530\",\"requestTimeStamp\":1646735834459,\"x-hcx-sender_code\":\"1-3a3bd68a-848a-4d52-9ec2-07a92d765fb4\",\"action\":\"/v1/coverageeligibility/on_check\",\"x-hcx-workflow_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5094\",\"x-hcx-api_call_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5093\",\"senderRole\":[\"provider\"]}", Map.class));
    }

    protected String getProviderToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE4MjI3MTM0NTEsImlhdCI6MTY0OTkxMzQ1MSwianRpIjoiOTA5ZGUxODAtMmEyOC00Mzg5LWJjM2MtZjgwNzRlN2RkNWNiIiwiaXNzIjoiaHR0cDovL2FlZjgxMDFjNDMyZDA0YTY1OWU2MzE3YjNlNTAzMWNmLTE2NzQ1ODYwNjguYXAtc291dGgtMS5lbGIuYW1hem9uYXdzLmNvbTo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZjdjMGU3NTktYmVjMy00MzFiLThjNGYtNmIyOTRkMTAzYTc0IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiMTBiNDM3M2UtNWExMC00YTBhLWE0NzYtOTI1ODUxOTAyZjgxIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJwcm92aWRlciIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImRoYXJtYXRlamE4ODhAZ21haWwuY29tIiwiZW50aXR5IjpbIk9yZ2FuaXNhdGlvbiJdLCJlbWFpbCI6ImRoYXJtYXRlamE4ODhAZ21haWwuY29tIn0.HZtmRMiGz5e9FA4Ix0pJs7MpMTqO8fEZ9KgafelISL30XEs7JylgjEs-FlmKiGnkIxaUmB2GdNTp3o-Q32QDMNtsrTYjG2Jo8DN4h7yNxcPFj9G-vM3ZhtnYcr1INQPRYuRcfGGqvPGc5s-5JwkS0EUe2fylJVoelTNKTczSMY5cB5PsE4qn04jxdlnc4CPaSt_SIx_CV-ABE6-1WQhCCFoge8jjQ01Hc96HUiy-tgPy90MJqu1it6St3m9yE9_sLbg_4S76dVPGPG41TqK-h34m6FM6xYW1BsiewKgHCcvsKhgNa3WFCuTrnwzx5z3TySRRww1I99ukWULP9IzxQg";
    }

    protected String getPayorToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE4MjI3MjIxNzMsImlhdCI6MTY0OTkyMjE3MywianRpIjoiZjdiOTE2YzQtZTE0OC00Y2QyLWFhNjYtNDcyZTM2MTBkODY5IiwiaXNzIjoiaHR0cDovL2FlZjgxMDFjNDMyZDA0YTY1OWU2MzE3YjNlNTAzMWNmLTE2NzQ1ODYwNjguYXAtc291dGgtMS5lbGIuYW1hem9uYXdzLmNvbTo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMjBiZDQyMjgtYTg3Zi00MTc1LWEzMGEtMjBmYjI4OTgzYWZiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiZjY0YmI1Y2QtZDdjOC00OGE3LTlhMTQtZWY0MGZjNzg5Zjg0IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJwYXlvciIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6Im5ld3BheW9yMDAzQGdtYWlsLmNvbSIsImVudGl0eSI6WyJPcmdhbmlzYXRpb24iXSwiZW1haWwiOiJuZXdwYXlvcjAwM0BnbWFpbC5jb20ifQ.ATKp9aS4awx8_FTh5PWIqTNDs95xBa9buo1zY8-MlkCW6UKFz24wwbb2D5wHemEqjCy2PxLhvux0IwZwxfV5tpUDpXKh2906wNKZKoDituak2C8xvx4Ds20PmVqiDaLtJxGjZyHIzZxVz0TATDZXYKrr_PmiaJljZP2dRpT0IaC4iWX_7KRgOBaU6XFiv7Svs3FnC-T17_7ZikZ-q0qMT9svFTO3NzvEjISX9fghmQU8ZbkhT_Mnxt4nrOSVSggI2TwHRLItYm72kNrGO-eJy_juNyNgVcQZMy70No1441kxiGSzy1_yI6iSmo2IKEMBQ_iMYXgPZnwglANLhdeZRA";
    }

    protected String getExpiredProviderToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOi0xNzgxMDU0Mjg2LCJpYXQiOjE2NDk5MTMwMTAsImp0aSI6ImMzMWUxZTNiLWFlOWQtNDFlMy1hYzFmLThkMTM0ZGY4NWI2YyIsImlzcyI6Imh0dHA6Ly9hZWY4MTAxYzQzMmQwNGE2NTllNjMxN2IzZTUwMzFjZi0xNjc0NTg2MDY4LmFwLXNvdXRoLTEuZWxiLmFtYXpvbmF3cy5jb206ODA4MC9hdXRoL3JlYWxtcy9zd2FzdGgtaGVhbHRoLWNsYWltLWV4Y2hhbmdlIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImY3YzBlNzU5LWJlYzMtNDMxYi04YzRmLTZiMjk0ZDEwM2E3NCIsInR5cCI6IkJlYXJlciIsImF6cCI6InJlZ2lzdHJ5LWZyb250ZW5kIiwic2Vzc2lvbl9zdGF0ZSI6ImQ1ZTMxYTMxLTZlOWYtNDYzMi04N2MyLWIyY2M1NmMzNWY0ZCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsicHJvdmlkZXIiLCJkZWZhdWx0LXJvbGVzLW5kZWFyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJkaGFybWF0ZWphODg4QGdtYWlsLmNvbSIsImVudGl0eSI6WyJPcmdhbmlzYXRpb24iXSwiZW1haWwiOiJkaGFybWF0ZWphODg4QGdtYWlsLmNvbSJ9.Q4w_kGv6qbtCqpWpxPZnGHuaM58R_Ruj7JN3naZhUnWAW9fegQQ3AyoNkHH0tCsBFgS8lJTuhntxJEx5wM3D_aIlb-HAw9-10JnIq9uh8dEoDFH3Q17wvxne1Qb27AqKVVhh4UxlDkOcDfUBYxpGnOqtuRbuMmSYxwuEz-F9YsB7kPHpAAcNUBMDmgvN4ZHCLXBouhO4KmjDdEUcrl_v21ZwU23s6lQzSlS6YJ3G3FG0XDM1xF-m5DjVRbWi5umuqs9BrRhwRwE2rqSAU4nlTiwBErCl9Rb3-hinDR9SizFJbW8mNh0SnPl8Pj8IMJKwtrKOv1aTW_2_dpCRmCmdUA";
    }

    protected String getHCXAdminToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE4MjMwOTQ2MTYsImlhdCI6MTY1MDI5NDYxNiwianRpIjoiOTAyZTdhZDAtYjM1NS00YjJkLTlkMjYtZTA1Y2Q0NDg3NzY0IiwiaXNzIjoiaHR0cDovL2FlZjgxMDFjNDMyZDA0YTY1OWU2MzE3YjNlNTAzMWNmLTE2NzQ1ODYwNjguYXAtc291dGgtMS5lbGIuYW1hem9uYXdzLmNvbTo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZjY5OGI1MjEtNzQwOS00MzJkLWE1ZGItZDEzZTUxZjAyOWE5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiYTA5MzZjNDQtNTA3Ny00NWIxLWIyNWYtMmRjZTk3YzFmMWQxIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJISUUvSElPLkhDWCIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeGdhdGV3YXlAZ21haWwuY29tIiwiZW50aXR5IjpbIk9yZ2FuaXNhdGlvbiJdLCJlbWFpbCI6ImhjeGdhdGV3YXlAZ21haWwuY29tIn0.e7XBiFff9zeyzZtU4GStZBlS7YZFK11y1O_Uzd7InJ7wp6x5mo5d5pV988Lyxn3hcWUE8Bsn66lQJknCCU6KhmAE5n60SOgz9lR_fnaqW1vQsV2D3oLt4eNuKKs5xbOtEyFL_J4qlMpBDAxotAAZbCiueW8i_KWfQpRjeTVJwOx5TmbmuTBa5yNYrPMhXTKu_IMI6jyTOtta7JSC3Gz7qdx7O9wbYEZgUHM5s6Ve5OpigxCf4Cra697m39yjJk6xPHVKM7_I_gHM-7U95KOWoOaQ_M7x3QsijEORwyeJfDsZVE6hOf4EE7LzQObD9H8UYwEPubjjclNQRVu1JkN2_A";
    }

    protected String getSubscriptionRequest(String topicCode,List<String> sendersList) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE,topicCode);
        obj.put(SENDER_LIST,sendersList);
        return JSONUtils.serialize(obj);
    }

    protected String getInvalidSubscriptionRequest(boolean hasTopic) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        if(hasTopic)
        obj.put(TOPIC_CODE,"topicCode");
        else obj.put(SENDER_LIST,Arrays.asList("new-payor-3"));
        return JSONUtils.serialize(obj);
    }

    protected String getInvalidSubscriptionRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(TOPIC_CODE,"topicCode");
        obj.put(SENDER_LIST,Arrays.asList("new-payor-3"));
        obj.put(NOTIFICATION_DATA,Arrays.asList("new-payor-3"));
        return JSONUtils.serialize(obj);
    }

    protected String getSubscriptionListRequest() throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(FILTERS,new HashMap<>());
        obj.put(LIMIT, 1);
        obj.put(OFFSET,0);
        return JSONUtils.serialize(obj);
    }

    protected String getSubscriptionUpdateRequest(String topicCode, int subscriptionStatus , Object isDelegated) throws JsonProcessingException {
        Map<String,Object> obj = new HashMap<>();
        obj.put(RECIPIENT_CODE,"payor01@hcx");
        obj.put(TOPIC_CODE,topicCode);
        obj.put(SENDER_CODE,"provider01@hcx");
        obj.put(SUBSCRIPTION_STATUS, subscriptionStatus);
        obj.put(IS_DELEGATED, isDelegated);
        return JSONUtils.serialize(obj);
    }

    protected String getOnSubscriptionRequest() throws JsonProcessingException {
        Map<String, Object> obj = new HashMap<>();
        obj.put(SUBSCRIPTION_ID, "subscription_id-001");
        obj.put(SUBSCRIPTION_STATUS, 1);
        return JSONUtils.serialize(obj);
    }

}
