package org.swasth.apigateway;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.swasth.apigateway.service.AuditService;
import org.swasth.apigateway.service.RegistryService;
import org.swasth.apigateway.utils.JSONUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BaseSpec {

    protected MockWebServer server =  new MockWebServer();

    @MockBean
    protected RegistryService registryService;

    @MockBean
    protected AuditService auditService;

    @MockBean
    protected JWTVerifier jwtVerifier;

    @Mock
    protected DecodedJWT decodedJWT;

    @LocalServerPort
    protected int port;

    protected WebTestClient client;

    @BeforeEach
    public void setup() throws IOException {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        server.start(InetAddress.getByName("localhost"),8080);
    }

    @AfterEach
    public void teardown() throws IOException {
        server.shutdown();
    }

    protected String getResponseErrorCode(EntityExchangeResult<Map> result){
        return (String) ((Map<String,Object>) result.getResponseBody().get("error")).get("code");
    }

    protected Map<String,Object> getRequestBody() {
        return Collections.singletonMap("payload","eyJlbmMiOiJBMjU2R0NNIiwKImFsZyI6IlJTQS1PQUVQIiwKIngtaGN4LXNlbmRlcl9jb2RlIjoiMS0zYTNiZDY4YS04NDhhLTRkNTItOWVjMi0wN2E5MmQ3NjVmYjQiLAoieC1oY3gtcmVjaXBpZW50X2NvZGUiOiIxLWNlMjNjY2RjLWU2NDUtNGUzNS05N2I4LTBiZDhmZWY0M2VjZCIsCiJ4LWhjeC1hcGlfY2FsbF9pZCI6IjI2YjEwNjBjLTFlODMtNDYwMC05NjEyLWVhMzFlMGNhNTA5MyIsCiJ4LWhjeC1jb3JyZWxhdGlvbl9pZCI6IjVlOTM0ZjkwLTExMWQtNGYwYi1iMDE2LWMyMmQ4MjA2NzRlMSIsCiJ4LWhjeC10aW1lc3RhbXAiOiIyMDIxLTEwLTI3VDIwOjM1OjUyLjYzNiswNTMwIiwKIngtaGN4LXN0YXR1cyI6InJlcXVlc3QucXVldWVkIiwKIngtaGN4LXdvcmtmbG93X2lkIjoiMjZiMTA2MGMtMWU4My00NjAwLTk2MTItZWEzMWUwY2E1MDk0IiwKIngtaGN4LWRlYnVnX2ZsYWciOiJJbmZvIiwKIngtaGN4LWRlYnVnX2RldGFpbHMiOnsiY29kZSI6IkVSUl9JTlZBTElEX0VOQ1JZUFRJT04iLCJtZXNzYWdlIjoiUmVjaXBpZW50IEludmFsaWQgRW5jcnlwdGlvbiIsInRyYWNlIjoiIn0KfQ==.6KB707dM9YTIgHtLvtgWQ8mKwboJW3of9locizkDTHzBC2IlrT1oOQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY.Mz-VPPyU4RlcuYv1IwIvzw");
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
        obj.put("x-hcx-sender_code","1-0756766c-ad43-4145-86ea-d1b17b729a3f");
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

    protected Map<String,Object> getProviderDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New Teja Hospital888\", \"primary_mobile\": \"9493347239\", \"primary_email\": \"dharmateja888@gmail.com\", \"roles\": [ \"provider\" ], \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"f901ba37-e09f-4d84-a75f-5e203f8ad4da\", \"@type\": \"address\", \"locality\": \"Nampally\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"https://677e6fd9-57cc-466c-80f6-ae0462762872.mock.pstmn.io\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"da192c1e-5ad4-47bc-b425-de4f7bbc9bd0\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f7c0e759-bec3-431b-8c4f-6b294d103a74\" ], \"osid\": \"68c5deca-8299-4feb-b441-923bb649a9a3\", \"@type\": \"Organisation\", \"payment\": { \"ifsc_code\": \"ICICI\", \"account_number\": \"4707890099809809\", \"@type\": \"payment_details\", \"osid\": \"3a3bd68a-848a-4d52-9ec2-07a92d765fb4\" } }", Map.class);
    }

    protected Map<String,Object> getPayorDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 3\", \"primary_mobile\": \"9493347003\", \"primary_email\": \"newpayor003@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"929d9a60-1fe3-49a5-bae7-4b49970cebbb\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"68a27687-b8c8-4271-97a1-0af3f53c3f3c\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"20bd4228-a87f-4175-a30a-20fb28983afb\" ], \"osid\": \"ce23ccdc-e645-4e35-97b8-0bd8fef43ecd\" }", Map.class);
    }

    protected Map<String,Object> getPayor2Details() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 2\", \"primary_mobile\": \"9493347002\", \"primary_email\": \"newpayor002@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"513aa2ef-c4d4-4202-9926-816a6d22ab8f\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"2b9fd445-053e-4f44-8ceb-bb51e3f48a86\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"a97ae3c3-bf32-438f-a63e-5b896393163f\" ], \"osid\": \"8584ba69-6c50-4535-8ad5-c02b8c3180a6\" }", Map.class);
    }

    protected Map<String,Object> getBlockedPayorDetails() throws Exception {
        return JSONUtils.deserialize("{ \"participant_name\": \"New payor 3\", \"primary_mobile\": \"9493347003\", \"primary_email\": \"newpayor003@gmail.com\", \"roles\": [ \"payor\" ], \"scheme_code\": \"Default\", \"address\": { \"plot\": \"5-4-199\", \"street\": \"road no 12\", \"landmark\": \"Jawaharlal Nehru Road\", \"village\": \"Nampally\", \"district\": \"Hyderabad\", \"state\": \"Telangana\", \"pincode\": \"500805\", \"osid\": \"929d9a60-1fe3-49a5-bae7-4b49970cebbb\" }, \"phone\": [ \"040-387658992\" ], \"status\": \"Blocked\", \"endpoint_url\": \"http://a07c089412c1b46f2b49946c59267d03-2070772031.ap-south-1.elb.amazonaws.com:8080\", \"payment_details\": { \"account_number\": \"4707890099809809\", \"ifsc_code\": \"ICICI\", \"osid\": \"68a27687-b8c8-4271-97a1-0af3f53c3f3c\" }, \"signing_cert_path\": \"urn:isbn:0-476-27557-4\", \"linked_registry_codes\": [ \"22344\" ], \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"20bd4228-a87f-4175-a30a-20fb28983afb\" ], \"osid\": \"ce23ccdc-e645-4e35-97b8-0bd8fef43ecd\" }", Map.class);
    }

    protected List<Map<String,Object>> getAuditLogs() throws Exception {
        return Arrays.asList(JSONUtils.deserialize("{\"eid\":\"AUDIT\",\"x-hcx-error_details\":{},\"x-hcx-recipient_code\":\"1-2ff1f493-c4d4-4fc7-8d41-aaabb997af23\",\"x-hcx-debug_details\":{},\"auditTimeStamp\":1646736378272,\"mid\":\"54af42a9-3905-4f15-8c9d-94079594b6a6\",\"x-hcx-correlation_id\":\"5e934f90-111d-4f0b-b016-c22d820674e1\",\"updatedTimestamp\":1646736375686,\"x-hcx-status\":\"request.queued\",\"recipientRole\":[\"payor\"],\"x-hcx-timestamp\":\"2021-10-27T20:35:52.636+0530\",\"requestTimeStamp\":1646735834459,\"x-hcx-sender_code\":\"1-3a3bd68a-848a-4d52-9ec2-07a92d765fb4\",\"action\":\"/v1/coverageeligibility/check\",\"x-hcx-workflow_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5094\",\"x-hcx-api_call_id\":\"26b1060c-1e83-4600-9612-ea31e0ca5093\",\"senderRole\":[\"provider\"]}", Map.class));
    }

    protected String getProviderToken() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3Q1l0Z2VYMzA2NEQ3VUU0czdCQWlJZmUzN3hxczBtNEVSQnpmdzVuMzdNIn0.eyJleHAiOjE2NDg5NzI3NTAsImlhdCI6MTY0ODEwODc1MCwianRpIjoiNzA4ZTVjYjYtNGMxZC00NDg5LWEzNWItMzMyOGEyMDgyMTgyIiwiaXNzIjoiaHR0cDovL2FlZjgxMDFjNDMyZDA0YTY1OWU2MzE3YjNlNTAzMWNmLTE2NzQ1ODYwNjguYXAtc291dGgtMS5lbGIuYW1hem9uYXdzLmNvbTo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiZThkOTZlMDAtOTNkMS00MDJhLWFmOTYtMjk2MGM5OTJjNGEzIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiOTU4Zjg1YjItNDUzYy00MWNlLTljNjEtZDQzOTY0ZGMyNDc5IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJwcm92aWRlciIsImRlZmF1bHQtcm9sZXMtbmRlYXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImRoYXJtYXRlamE4ODFAZ21haWwuY29tIiwiZW50aXR5IjpbIk9yZ2FuaXNhdGlvbiJdLCJlbWFpbCI6ImRoYXJtYXRlamE4ODFAZ21haWwuY29tIn0.gMiqG1M5OrVrZgGP7IbOP2salrrduaDCe6yFiXIoMe6GI42tYahnmAMo7QojKS54N_LBxR-8F31S7QLSvcupE5gyh_itFJGgFdOYDG7aei2tDseNd36s6g2zOEgpljaVV9-e7jgxlMBEklVgAIXK1yI-pup7qpo2_87pWZgVkd2v8hrg5Gm45mlWXFaQPn4imHmcNO_SBCg1di1EHh3fB3h57BHhV9Rs4W2Kjd6TF91RptOWbP4w9OaRrCzpVtIRO_ift0YngwCAEW_Q7mEl6qoMeILWC49MF5YF7isLFtmYLmdUGl7ErgRDT_VF7mBWkVT-YrnZu3wmw3l2mWao2Q";
    }

}
