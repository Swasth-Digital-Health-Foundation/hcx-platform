package org.swasth.hcx.controllers.v1;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.common.utils.Constants;
import org.swasth.hcx.controllers.BaseSpec;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class JWTControllerTests extends BaseSpec {


    private MockWebServer keycloakServer = new MockWebServer();

    private MockWebServer registryServer = new MockWebServer();


    @BeforeEach
    public void start() throws IOException {
        keycloakServer.start(InetAddress.getByName("localhost"), 8080);
        registryServer.start(InetAddress.getByName("localhost"),8082);
    }

    @AfterEach
    public void teardown() throws IOException, InterruptedException {
        keycloakServer.shutdown();
        registryServer.shutdown();
        Thread.sleep(2000);
    }



    @Test
     void participant_generate_token_success() throws Exception {
        keycloakServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"access_token\": \"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwZTgzMGI5YS00ZGFmLTQ3NDEtOGYwMi1jNDdhMzM3MTc5YWUiLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwOlwvXC9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwXC9hdXRoXC9yZWFsbXNcL3N3YXN0aC1oY3gtcGFydGljaXBhbnRzIiwidHlwIjoiQmVhcmVyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHByb3ZpZGVyMUBzd2FzdGhtb2NrLmNvbSIsImF1ZCI6ImFjY291bnQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbInByb3ZpZGVyIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJwYXJ0aWNpcGFudF9jb2RlIjoidGVzdHByb3ZpZGVyMS5zd2FzdGhtb2NrQHN3YXN0aC1oY3gtc3RhZ2luZyIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImV4cCI6MTY5MDUyODM5NCwic2Vzc2lvbl9zdGF0ZSI6IjBjNjZjYTgwLTVjOTgtNGI2Mi1hYTZiLTQzNjYwNWU3M2U4MSIsImlhdCI6MTY4NzkzNjM5NCwianRpIjoiMzAxMDNhOGItZDcyYS00NjhlLTkyOGItZGM2NjY0MDc0YjFkIiwiZW50aXR5IjpbIk9yZ2FuaXNhdGlvbiJdLCJlbWFpbCI6InRlc3Rwcm92aWRlcjFAc3dhc3RobW9jay5jb20ifQ.VXRjzexufP-BFK1rjlzOWgXfuJTlvsnIYayqV6HQ3MAITP7e0lFD4sUsWs1GTfn9lSBLp3p0_61V_TKDjnJSXhPIzGN5bI4YwVTg0QhyIiMleHTHcWZ4qSeJ1wOWpe8EO5UcH867FfV0Vb6R16ok2H_sCdopluSlunB0rYMy4DsVOAO8JAkPfaoh2MKr9qms02_86qBmFCnp8TjJC8ZGhcfRDMt8ZYrzx2Tvb6pfAyWoXeprePDVcuSVWplHemO6A1cIdd5PQTomVSFp_CFK0I-HFL82G5kiBQ01gD2c34VyCM6CVeFtC5Ra85d7gGSuiUi_a8BaA63H-6YPoqZ4jg\", \"refresh_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIxYmVkOTcxNC00ZmY3LTRlMjItOGI4Mi0wODY5OWU5Yjk0NjUifQ.eyJleHAiOjE2ODc5MzY5OTQsImlhdCI6MTY4NzkzNjM5NCwianRpIjoiMDY4NDIwM2YtOTYzNS00YjYxLTlhOWItYzFhZjZjMTMzMjE4IiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLmtleWNsb2FrLnN2Yy5jbHVzdGVyLmxvY2FsOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhjeC1wYXJ0aWNpcGFudHMiLCJhdWQiOiJodHRwOi8va2V5Y2xvYWsua2V5Y2xvYWsuc3ZjLmNsdXN0ZXIubG9jYWw6ODA4MC9hdXRoL3JlYWxtcy9zd2FzdGgtaGN4LXBhcnRpY2lwYW50cyIsInN1YiI6IjBlODMwYjlhLTRkYWYtNDc0MS04ZjAyLWM0N2EzMzcxNzlhZSIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiIwYzY2Y2E4MC01Yzk4LTRiNjItYWE2Yi00MzY2MDVlNzNlODEiLCJzY29wZSI6InByb2ZpbGUgZW1haWwifQ.tadCV6yLUi38tbSB5sQVcjvCy6LiZ5XzE7k9L6Atr6w\", \"refresh_expires_in\": \"600\", \"not-before-policy\": \"1607576887\" , \"scope\": \"profile email\", \"token_type\": \"Bearer\", \"session_state\": \"0c66ca80-5c98-4b62-aa6b-436605e73e81\", \"expires_in\":  \"2592000\" }"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_GENERATE_TOKEN)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .param("username","hcxgateway@gmail.com")
                        .param("password","Opensaber@123"))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);

    }

    @Test
     void participant_generate_invalid_token() throws Exception {
        keycloakServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"access_token\": \"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwZTgzMGI5YS00ZGFmLTQ3NDEtOGYwMi1jNDdhMzM3MTc5YWUiLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwOlwvXC9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwXC9hdXRoXC9yZWFsbXNcL3N3YXN0aC1oY3gtcGFydGljaXBhbnRzIiwidHlwIjoiQmVhcmVyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdHByb3ZpZGVyMUBzd2FzdGhtb2NrLmNvbSIsImF1ZCI6ImFjY291bnQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbInByb3ZpZGVyIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJwYXJ0aWNpcGFudF9jb2RlIjoidGVzdHByb3ZpZGVyMS5zd2FzdGhtb2NrQHN3YXN0aC1oY3gtc3RhZ2luZyIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImV4cCI6MTY5MDUyODM5NCwic2Vzc2lvbl9zdGF0ZSI6IjBjNjZjYTgwLTVjOTgtNGI2Mi1hYTZiLTQzNjYwNWU3M2U4MSIsImlhdCI6MTY4NzkzNjM5NCwianRpIjoiMzAxMDNhOGItZDcyYS00NjhlLTkyOGItZGM2NjY0MDc0YjFkIiwiZW50aXR5IjpbIk9yZ2FuaXNhdGlvbiJdLCJlbWFpbCI6InRlc3Rwcm92aWRlcjFAc3dhc3RobW9jay5jb20ifQ.VXRjzexufP-BFK1rjlzOWgXfuJTlvsnIYayqV6HQ3MAITP7e0lFD4sUsWs1GTfn9lSBLp3p0_61V_TKDjnJSXhPIzGN5bI4YwVTg0QhyIiMleHTHcWZ4qSeJ1wOWpe8EO5UcH867FfV0Vb6R16ok2H_sCdopluSlunB0rYMy4DsVOAO8JAkPfaoh2MKr9qms02_86qBmFCnp8TjJC8ZGhcfRDMt8ZYrzx2Tvb6pfAyWoXeprePDVcuSVWplHemO6A1cIdd5PQTomVSFp_CFK0I-HFL82G5kiBQ01gD2c34VyCM6CVeFtC5Ra85d7gGSuiUi_a8BaA63H-6YPoqZ4jg\", \"refresh_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIxYmVkOTcxNC00ZmY3LTRlMjItOGI4Mi0wODY5OWU5Yjk0NjUifQ.eyJleHAiOjE2ODc5MzY5OTQsImlhdCI6MTY4NzkzNjM5NCwianRpIjoiMDY4NDIwM2YtOTYzNS00YjYxLTlhOWItYzFhZjZjMTMzMjE4IiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLmtleWNsb2FrLnN2Yy5jbHVzdGVyLmxvY2FsOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhjeC1wYXJ0aWNpcGFudHMiLCJhdWQiOiJodHRwOi8va2V5Y2xvYWsua2V5Y2xvYWsuc3ZjLmNsdXN0ZXIubG9jYWw6ODA4MC9hdXRoL3JlYWxtcy9zd2FzdGgtaGN4LXBhcnRpY2lwYW50cyIsInN1YiI6IjBlODMwYjlhLTRkYWYtNDc0MS04ZjAyLWM0N2EzMzcxNzlhZSIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiIwYzY2Y2E4MC01Yzk4LTRiNjItYWE2Yi00MzY2MDVlNzNlODEiLCJzY29wZSI6InByb2ZpbGUgZW1haWwifQ.tadCV6yLUi38tbSB5sQVcjvCy6LiZ5XzE7k9L6Atr6w\", \"refresh_expires_in\": \"600\", \"not-before-policy\": \"1607576887\" , \"scope\": \"profile email\", \"token_type\": \"Bearer\", \"session_state\": \"0c66ca80-5c98-4b62-aa6b-436605e73e81\", \"expires_in\":  \"2592000\" }"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"HCX Gateway\", \"primary_mobile\": \"\", \"primary_email\": \"hcxgateway@gmail.com\", \"roles\": [ \"HIE/HIO.HCX\" ], \"status\": \"Created\", \"endpoint_url\": \"http://a54c5bc648f1a41b8871b77ac01060ed-1840123973.ap-south-1.elb.amazonaws.com:8080\", \"encryption_cert\": \"urn:isbn:0-4234\", \"osOwner\": [ \"f698b521-7409-432d-a5db-d13e51f029a9\" ], \"participant_code\": \"d2d56996-1b77-4abb-b9e9-0e6e7343c72e\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_GENERATE_TOKEN)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .param("username","hcx-email@yopmail.com")
                        .param("password","Opensaber@123"))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);

    }

    @Test
     void user_generate_token_success() throws Exception {
        keycloakServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"access_token\": \"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI2ZWIzYzliOS0zOGRmLTQ0ZjMtOGY3Yi01OTNjMmQwZWQ4YzgiLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwOlwvXC9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwXC9hdXRoXC9yZWFsbXNcL3N3YXN0aC1oY3gtdXNlcnMiLCJ0eXAiOiJCZWFyZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0dXNlci1hYmhpLTFAeW9wbWFpbC5jb20iLCJhdWQiOiJhY2NvdW50IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLXN3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UtdXNlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXSwidGVuYW50X3JvbGVzIjpbeyJyb2xlIjoiYWRtaW4iLCJwYXJ0aWNpcGFudF9jb2RlIjoidGVzdHVzZXItYWJoaS0xLnlvcG1haWxAc3dhc3RoLWhjeCJ9XX0sInVzZXJfaWQiOiJ0ZXN0dXNlci1hYmhpLTFAeW9wbWFpbC5jb20iLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsImV4cCI6MTY4Nzk4NDQzNSwic2Vzc2lvbl9zdGF0ZSI6ImUwZjA3ZjE1LWYwNDAtNDhlMy05NjZmLTUwNzMxMDY1Y2E3MiIsImlhdCI6MTY4Nzk0ODQzNSwianRpIjoiMmZhNDZlOTItMGZlYS00MWRiLTk3NDYtYjQxNTMzNGJiNjRkIiwiZW1haWwiOiJ0ZXN0dXNlci1hYmhpLTFAeW9wbWFpbC5jb20iLCJlbnRpdHkiOlsiVXNlciJdfQ.n9qA_yQH4FSGODEIh14VPh40ISw6aBH_wilYu2-RGjxYjpMHq4fqo3V7KN1vSy-bQaMwdO5t5c5CR03kGtBj3vMzhWvktLZS-wrkf-yqoMrwx8c-YSGfGf7kHg_tp1JrU1inl2JVjQgqhPE3YX7y_boTaBkYUQkidtxbLk_RMszs8WNR_uVrh41mNwTMZb45cPNN2nGq8d2tUtXFcBtSlVgA1gSQX56m_xOutRznlFtgDqQNHSBtdV9wZ0QdEUhIjGsBoNufkP5Ai6lyyqsRwPSMLqO3wzxa_u1HRdwneQB20BInUkCzQOGdwYAB5-3O76rdadJRFFjVBckK5aeDvw\", \"refresh_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmMDM4NGRhMy05OTIwLTRmOTgtOWFlYi04MGJjZjIyZjgyNjUifQ.eyJleHAiOjE2ODc5ODQ0MzUsImlhdCI6MTY4Nzk0ODQzNSwianRpIjoiMGEyZGFlMDMtNTk1NS00MDViLWE1YjMtZmY1ZTY4YzI3M2Q0IiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLmtleWNsb2FrLnN2Yy5jbHVzdGVyLmxvY2FsOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhjeC11c2VycyIsImF1ZCI6Imh0dHA6Ly9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oY3gtdXNlcnMiLCJzdWIiOiI2ZWIzYzliOS0zOGRmLTQ0ZjMtOGY3Yi01OTNjMmQwZWQ4YzgiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiZTBmMDdmMTUtZjA0MC00OGUzLTk2NmYtNTA3MzEwNjVjYTcyIiwic2NvcGUiOiJlbWFpbCBwcm9maWxlIn0.tYqVWa51byEMIuEbM1QlvdQjlCtXVPaayueL4ikCwN8\", \"refresh_expires_in\": \"36000\", \"not-before-policy\": \"0\" , \"scope\": \"email profile\", \"token_type\": \"Bearer\", \"session_state\": \"e0f07f15-f040-48e3-966f-50731065ca72\", \"expires_in\":  \"36000\" }"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"testuser-abhi-1@yopmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_GENERATE_TOKEN)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .param("username","testuser-abhi-1@yopmail.com")
                        .param("password","Opensaber@123"))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);

    }

    @Test
     void user_generate_token_invalid() throws Exception {
        keycloakServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"access_token\": \"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI2ZWIzYzliOS0zOGRmLTQ0ZjMtOGY3Yi01OTNjMmQwZWQ4YzgiLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwOlwvXC9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwXC9hdXRoXC9yZWFsbXNcL3N3YXN0aC1oY3gtdXNlcnMiLCJ0eXAiOiJCZWFyZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0ZXN0dXNlci1hYmhpLTFAeW9wbWFpbC5jb20iLCJhdWQiOiJhY2NvdW50IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLXN3YXN0aC1oZWFsdGgtY2xhaW0tZXhjaGFuZ2UtdXNlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXSwidGVuYW50X3JvbGVzIjpbeyJyb2xlIjoiYWRtaW4iLCJwYXJ0aWNpcGFudF9jb2RlIjoidGVzdHVzZXItYWJoaS0xLnlvcG1haWxAc3dhc3RoLWhjeCJ9XX0sInVzZXJfaWQiOiJ0ZXN0dXNlci1hYmhpLTFAeW9wbWFpbC5jb20iLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsImV4cCI6MTY4Nzk4NDQzNSwic2Vzc2lvbl9zdGF0ZSI6ImUwZjA3ZjE1LWYwNDAtNDhlMy05NjZmLTUwNzMxMDY1Y2E3MiIsImlhdCI6MTY4Nzk0ODQzNSwianRpIjoiMmZhNDZlOTItMGZlYS00MWRiLTk3NDYtYjQxNTMzNGJiNjRkIiwiZW1haWwiOiJ0ZXN0dXNlci1hYmhpLTFAeW9wbWFpbC5jb20iLCJlbnRpdHkiOlsiVXNlciJdfQ.n9qA_yQH4FSGODEIh14VPh40ISw6aBH_wilYu2-RGjxYjpMHq4fqo3V7KN1vSy-bQaMwdO5t5c5CR03kGtBj3vMzhWvktLZS-wrkf-yqoMrwx8c-YSGfGf7kHg_tp1JrU1inl2JVjQgqhPE3YX7y_boTaBkYUQkidtxbLk_RMszs8WNR_uVrh41mNwTMZb45cPNN2nGq8d2tUtXFcBtSlVgA1gSQX56m_xOutRznlFtgDqQNHSBtdV9wZ0QdEUhIjGsBoNufkP5Ai6lyyqsRwPSMLqO3wzxa_u1HRdwneQB20BInUkCzQOGdwYAB5-3O76rdadJRFFjVBckK5aeDvw\", \"refresh_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmMDM4NGRhMy05OTIwLTRmOTgtOWFlYi04MGJjZjIyZjgyNjUifQ.eyJleHAiOjE2ODc5ODQ0MzUsImlhdCI6MTY4Nzk0ODQzNSwianRpIjoiMGEyZGFlMDMtNTk1NS00MDViLWE1YjMtZmY1ZTY4YzI3M2Q0IiwiaXNzIjoiaHR0cDovL2tleWNsb2FrLmtleWNsb2FrLnN2Yy5jbHVzdGVyLmxvY2FsOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhjeC11c2VycyIsImF1ZCI6Imh0dHA6Ly9rZXljbG9hay5rZXljbG9hay5zdmMuY2x1c3Rlci5sb2NhbDo4MDgwL2F1dGgvcmVhbG1zL3N3YXN0aC1oY3gtdXNlcnMiLCJzdWIiOiI2ZWIzYzliOS0zOGRmLTQ0ZjMtOGY3Yi01OTNjMmQwZWQ4YzgiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoicmVnaXN0cnktZnJvbnRlbmQiLCJzZXNzaW9uX3N0YXRlIjoiZTBmMDdmMTUtZjA0MC00OGUzLTk2NmYtNTA3MzEwNjVjYTcyIiwic2NvcGUiOiJlbWFpbCBwcm9maWxlIn0.tYqVWa51byEMIuEbM1QlvdQjlCtXVPaayueL4ikCwN8\", \"refresh_expires_in\": \"36000\", \"not-before-policy\": \"0\" , \"scope\": \"email profile\", \"token_type\": \"Bearer\", \"session_state\": \"e0f07f15-f040-48e3-966f-50731065ca72\", \"expires_in\":  \"36000\" }"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"user_name\": \"test-user-89\",\"created_by\":\"test-participant-code-4@swasth\", \"mobile\": \"9620499129\", \"email\": \"testuser-abhi-1@yopmail.com\", \"tenant_roles\": [ {\"participant_code\":\"test-participant-code-4@swasth\",\"roles\":[\"admin\"]}], \"osOwner\": [ \"d3a64f93-7c0e-4b10-8f32-eb26ee65400f\" ], \"user_id\": \"test-user-89.gmail@swasth-hcx\",\"osid\":\"916d667b-6e39-4750-95eb-f3dc5061ab63\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.USER_GENERATE_TOKEN)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .param("username","hcx-user@yopmail.com")
                        .param("password","Opensaber@123"))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(500, status);

    }

    @Test
    void api_access_token_generate_success() throws Exception {
        keycloakServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{ \"access_token\": \"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI0YzY0YTEwNC02NmE4LTQ2M2YtYWFhNC0wNzg3Y2I0MTQ1ZTkiLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJpc3MiOiJodHRwOlwvXC9kZXYtaGN4LnN3YXN0aC5hcHBcL2F1dGhcL3JlYWxtc1wvYXBpLWFjY2VzcyIsInR5cCI6IkJlYXJlciIsInByZWZlcnJlZF91c2VybmFtZSI6ImhjeHRlc3Q2MDM0LnlvcG1haWxAc3dhc3RoLWhjeC1kZXY6aGN4dGVzdDYwMzRAeW9wbWFpbC5jb20iLCJhdWQiOiJhY2NvdW50IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJwYXJ0aWNpcGFudF9yb2xlcyI6WyJwYXlvciJdLCJ1c2VyX3JvbGVzIjpbImFkbWluIiwiY29uZmlnLW1hbmFnZXIiXX0sInVzZXJfaWQiOiJoY3h0ZXN0NjAzNEB5b3BtYWlsLmNvbSIsImF6cCI6InJlZ2lzdHJ5IiwicGFydGljaXBhbnRfY29kZSI6ImhjeHRlc3Q2MDM0LnlvcG1haWxAc3dhc3RoLWhjeC1kZXYiLCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJleHAiOjIxMjMxNDQxNjYsInNlc3Npb25fc3RhdGUiOiI1MGVkZTA2YS03YTE5LTQ5YTctYmRkYS03MjJlOGQ0Mzk4OGQiLCJpYXQiOjE2OTExNDQxNjYsImp0aSI6ImQyZGZhMDM2LTE1ZmQtNDAyNi1hNjEwLWYwZjc5ZDMwZDU2NSIsImVudGl0eSI6WyJhcGktYWNjZXNzIl19.OsTURI6-V8zggwxsT69zaX0xqrfNUsegZQzDgncHvcuix-kNURKK_csqoX0QyzPKL5P-7QUbIUxqh_MIKR1ASqinmfDDpw2SXbaslNWqiGeUHGjVrrsH6e9XWo_OahD8D_-LmRYXQjhPq4HTpGa19iFET0M3YhiwSns8KvrEqVQOuWRs8iAdLno1trLE0JZRL1wT09jOJVcX_mnLP374DtyZ7r7pW_mLxwV_YUFX--u3BwyCf3xzJ6zooOUsDqqWD_VyqiRlSTpcmSytCJmOSXhLv62zbDp7nIHaPj79Hd19tt668_QfEs-CjmRPXimp9IHe92FWo2OpPDvF33IM8g\", \"refresh_expires_in\": \"600\", \"not-before-policy\": \"1607576887\" , \"scope\": \"profile email\", \"token_type\": \"Bearer\", \"session_state\": \"0c66ca80-5c98-4b62-aa6b-436605e73e81\", \"expires_in\":  \"2592000\" }"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"participant_name\": \"test hcx\", \"primary_email\": \"hcxtest6034@yopmail.com\", \"primary_mobile\": \"8522875773\", \"roles\": [ \"payor\" ], \"endpoint_url\": \"http://testurl/v0.7\", \"encryption_cert\": \"https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/public-key.pem\", \"status\": \"Created\", \"scheme_code\": \"default\", \"participant_code\": \"hcxtest6034.yopmail@swasth-hcx-dev\", \"encryption_cert_expiry\": 1840270798000, \"osOwner\": [ \"4c64a104-66a8-463f-aaa4-0787cb4145e9\" ], \"osCreatedAt\": \"2023-06-24T04:27:28.427Z\", \"osUpdatedAt\": \"2023-06-24T04:27:28.427Z\", \"osid\": \"8fe5197e-cbc3-44f0-9b5d-b8c19a1cc436\" }]")
                .addHeader("Content-Type", "application/json"));
        registryServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{ \"email\": \"hcxtest6034@yopmail.com\", \"mobile\": \"8522875773\", \"user_id\": \"hcxtest6034@yopmail.com\", \"tenant_roles\": [ { \"role\": \"admin\", \"participant_code\": \"hcxtest6034.yopmail@swasth-hcx-dev\", \"osCreatedAt\": \"2023-06-24T04:27:30.636Z\", \"osUpdatedAt\": \"2023-06-24T04:27:30.636Z\", \"osid\": \"96229f1b-4e82-477a-9fc0-fede4214b47c\" }, { \"role\": \"config-manager\", \"participant_code\": \"hcxtest6034.yopmail@swasth-hcx-dev\", \"osCreatedAt\": \"2023-06-24T04:27:30.636Z\", \"osUpdatedAt\": \"2023-06-24T04:27:30.636Z\", \"osid\": \"e1cbe8f9-07eb-4b09-b4d7-44bf4ff8d3ae\" } ], \"created_by\": \"hcxtest6034.yopmail@swasth-hcx-dev\", \"user_name\": \"test hcx Admin\", \"osOwner\": [ \"58ab5fe0-ccf8-46a1-9808-f6e58e6fc5da\" ], \"osCreatedAt\": \"2023-06-24T04:27:30.636Z\", \"osUpdatedAt\": \"2023-06-24T04:27:30.636Z\", \"osid\": \"48b21f40-f916-4b24-b6bd-d0e8890d2a0f\" }]")
                .addHeader("Content-Type", "application/json"));
        MvcResult mvcResult = mockMvc.perform(post(Constants.VERSION_PREFIX + Constants.PARTICIPANT_GENERATE_TOKEN)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .param("username","hcxtest6034@yopmail.com")
                        .param("password","Test@12345")
                        .param("participant_code", "hcxtest6034.yopmail@swasth-hcx-dev"))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(200, status);
    }
    
}
